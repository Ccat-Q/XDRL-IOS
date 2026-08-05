package www.xdyl.hygge.shared

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/** 清单中的单个文件条目。 */
data class ModFile(
    val name: String,          // 纯文件名，无 "./" 前缀
    val size: Long? = null,    // 字节数（CSV 第 3 列）
    val md5: String? = null,   // 小写 hex
    val sha256: String? = null // 小写 hex
)

/** 解析 CSV 清单：name,显示大小,size,md5,sha256。列值可能带双引号。 */
fun parseCsv(s: String): List<ModFile> = buildList {
    for (line in s.lines()) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        val p = trimmed.split(",")
        if (p.isEmpty()) continue
        val name = p[0].trim('"').removePrefix("./")
        if (name.isBlank()) continue
        val size = if (p.size >= 3) p[2].trim('"').toLongOrNull() else null
        val md5 = if (p.size >= 4) p[3].trim('"').lowercase() else null
        val sha256 = if (p.size >= 5) p[4].trim('"').lowercase() else null
        add(ModFile(name = name, size = size, md5 = md5, sha256 = sha256))
    }
}

/** 解析 JSON manifest，只取 name 字段（项目无 JSON 库，手写解析）。 */
fun parseManifest(json: String): List<ModFile> {
    val result = mutableListOf<ModFile>()
    val q = (34.toChar()).toString()
    val key = q + "name" + q
    var pos = 0
    while (true) {
        val keyIdx = json.indexOf(key, pos)
        if (keyIdx == -1) break
        val colon = json.indexOf(':', keyIdx + key.length)
        if (colon == -1) break
        val qStart = json.indexOf('"', colon + 1)
        if (qStart == -1) break
        val qEnd = json.indexOf('"', qStart + 1)
        if (qEnd == -1) break
        result.add(ModFile(name = json.substring(qStart + 1, qEnd)))
        pos = qEnd + 1
    }
    return result
}

/**
 * 并发下载引擎：
 *  - 并发限流（Semaphore(threads)）
 *  - 增量跳过（本地文件哈希与清单任一列匹配则跳过）
 *  - 下载失败自动重试（最多 5 次，间隔 1000*attempt）
 *  - 下载后 MD5/SHA256 校验，不匹配则删除文件并计失败
 *  - 完成后按需清理孤儿 .jar（不在清单 ∪ whitelist 的文件）
 */
class DownloadEngine(
    private val threads: Int,          // 并发上限（>=1）
    private val cleanOrphans: Boolean, // 完成后是否清理多余 .jar
    private val whitelist: List<String>, // 孤儿清理豁免文件名列表
    private val baseUrl: String,       // 服务器根，如 http://x/mods/
    private val destDir: String,       // 目标目录（不含文件名）
    private val onStatus: (String) -> Unit,   // 状态文字（如 "[2/5] name.jar"）
    private val onProgress: (Float) -> Unit,   // 0..1 总进度
    private val onFinish: (ok: Int, skipped: Int, fail: Int) -> Unit
) {
    private enum class FileResult { OK, SKIPPED, FAIL }

    @Volatile
    private var currentFile: String = ""

    @Volatile
    private var doneCount: Int = 0

    private val counterMutex = Mutex()
    private var okCount = 0
    private var skipCount = 0
    private var failCount = 0
    private var total = 0

    /** 异步启动，不阻塞调用线程。 */
    fun start(mods: List<ModFile>) {
        if (mods.isEmpty()) {
            onFinish(0, 0, 0)
            return
        }
        total = mods.size
        okCount = 0; skipCount = 0; failCount = 0
        doneCount = 0
        currentFile = ""

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val semaphore = Semaphore(threads.coerceAtLeast(1))

        scope.launch {
            val jobs = mods.map { m ->
                launch {
                    semaphore.withPermit {
                        val result = processFile(m)
                        counterMutex.withLock {
                            when (result) {
                                FileResult.OK -> okCount++
                                FileResult.SKIPPED -> skipCount++
                                FileResult.FAIL -> failCount++
                            }
                            doneCount = okCount + skipCount + failCount
                        }
                        onProgress(doneCount.toFloat() / total)
                    }
                }
            }
            jobs.joinAll()

            if (cleanOrphans) {
                cleanOrphans(mods)
            }
            onFinish(okCount, skipCount, failCount)
            scope.cancel()
        }
    }

    /** 清理 destDir 中不在（清单 name ∪ whitelist）的 .jar。 */
    private fun cleanOrphans(mods: List<ModFile>) {
        val keep = (mods.map { it.name.removePrefix("./") } + whitelist.map { it.removePrefix("./") }).toSet()
        for (jar in listJarsInDir(destDir)) {
            if (jar !in keep) {
                val ok = deleteFile("$destDir/$jar")
                Logger.i("DL", if (ok) "孤儿清理: 删除 $jar" else "孤儿清理: 删除失败 $jar")
            }
        }
    }

    private suspend fun processFile(m: ModFile): FileResult = try {
        processFileInner(m)
    } catch (t: Throwable) {
        Logger.e("DL", "[异常] ${m.name}: $t")
        FileResult.FAIL
    }

    private suspend fun processFileInner(m: ModFile): FileResult {
        val destPath = "$destDir/${m.name}"
        currentFile = m.name
        onStatus("[${doneCount}/${total}] ${currentFile}")

        val hasHash = m.md5 != null || m.sha256 != null

        // 增量跳过：有哈希时，本地文件存在且任一哈希匹配则跳过
        if (hasHash) {
            val local = readFileBytes(destPath)
            if (local != null && matches(m, local)) {
                Logger.i("DL", "[跳过] ${m.name}")
                return FileResult.SKIPPED
            }
        }

        // 下载 + 重试（第 1 次 + 4 次重试，间隔 1000*attempt 毫秒）
        val url = "${baseUrl.trimEnd('/')}/${m.name}"
        var attempt = 0
        var lastErr = "下载失败"
        while (attempt < 5) {
            attempt++
            if (attempt > 1) delay(1000L * (attempt - 1))
            Logger.i("DL", "GET $url (第${attempt}次) ${m.name}")
            if (downloadToFile(url, destPath)) {
                if (hasHash) {
                    val data = readFileBytes(destPath)
                    if (data != null && matches(m, data)) {
                        return FileResult.OK
                    }
                    // 校验失败 → 删除该文件并计失败
                    Logger.e("DL", "校验失败 ${m.name}")
                    deleteFile(destPath)
                    lastErr = "校验失败"
                    return FileResult.FAIL
                }
                return FileResult.OK
            }
            lastErr = "下载失败"
        }
        Logger.e("DL", "[失败] ${m.name}: $lastErr")
        return FileResult.FAIL
    }

    /** 任一哈希列匹配即可。 */
    private fun matches(m: ModFile, data: ByteArray): Boolean {
        val md5Ok = m.md5 != null && Hash.md5(data) == m.md5
        val shaOk = m.sha256 != null && Hash.sha256(data) == m.sha256
        return md5Ok || shaOk
    }

    /** 用 suspendCancellableCoroutine 包装回调式 downloadFile。 */
    private suspend fun downloadToFile(url: String, destPath: String): Boolean =
        suspendCancellableCoroutine { cont ->
            downloadFile(url, destPath, onProgress = { }, onComplete = { good, _ ->
                if (cont.isActive) {
                    if (good) cont.resume(true) else cont.resume(false)
                }
            })
        }
}
