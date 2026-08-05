package www.xdyl.hygge.shared

/**
 * 云端版本 API（与 Android 参考实现同一套接口）。
 * 服务器: unsa-fdws.cc.cd，X-API-Key 鉴权。
 */
private const val API_BASE_URL = "https://unsa-fdws.cc.cd/api/download/"
private const val API_KEY = "US.Kx9Qm2p"

/** 单个模组变更（updated 列表元素）。 */
data class ModChange(val name: String, val oldVersion: String?, val newVersion: String?)

/** CSV 版本差异：新增 / 移除 / 更新。 */
data class VersionDiff(
    val version: String,
    val added: List<String>,
    val removed: List<String>,
    val updated: List<ModChange>
)

/**
 * 远程版本检查：拉取 {baseUrl}version.json（形如 {"version":"2.1","note":"..."}），
 * 与本地已应用版本比较。手写 JSON 解析，不依赖任何 JSON 库。
 *
 * 返回 true 时第二参为 "发现新版本 vX: note"；false 时第二参为空字符串或错误说明。
 * 拉取失败静默返回 false。
 */
fun checkForUpdate(baseUrl: String, currentVersion: String, onResult: (Boolean, String) -> Unit) {
    val url = "${baseUrl.trimEnd('/')}/version.json"
    fetchManifest(url) { ok, body ->
        if (!ok || body.isEmpty()) {
            onResult(false, "")
            return@fetchManifest
        }
        val version = extractJsonString(body, "version")
        if (version.isNullOrEmpty()) {
            onResult(false, "")
            return@fetchManifest
        }
        if (version != currentVersion) {
            val note = extractJsonString(body, "note").orEmpty()
            val suffix = if (note.isNotEmpty()) ": $note" else ""
            onResult(true, "发现新版本 v$version$suffix")
        } else {
            onResult(false, "")
        }
    }
}

/**
 * 云端 CSV 差异检查：拉取 Version_difference.json（带 X-API-Key），
 * 与本地 local_version 字符串比较。有更新时通过 [onResult] 返回 VersionDiff。
 * 失败时 ok=false，msg 为错误说明。
 */
fun checkVersionDifference(onResult: (Boolean, VersionDiff?, String) -> Unit) {
    fetchWithHeaders("${API_BASE_URL}Version_difference.json", mapOf("X-API-Key" to API_KEY)) { ok, body ->
        if (!ok || body.isEmpty()) {
            onResult(false, null, "获取版本信息失败")
            return@fetchWithHeaders
        }
        val version = extractJsonString(body, "version")
        if (version.isNullOrEmpty()) {
            onResult(false, null, "版本信息格式错误")
            return@fetchWithHeaders
        }
        val updNames = extractArrayField(body, "updated", "name")
        val updOld = extractArrayField(body, "updated", "old_version")
        val updNew = extractArrayField(body, "updated", "new_version")
        val diff = VersionDiff(
            version = version,
            added = extractArrayField(body, "added", "name"),
            removed = extractArrayField(body, "removed", "name"),
            updated = updNames.mapIndexed { i, n -> ModChange(n, updOld.getOrNull(i), updNew.getOrNull(i)) }
        )
        val local = Preferences().getString("local_version", "0.0") ?: "0.0"
        onResult(version > local, diff, "")
    }
}

/**
 * 下载最新 file_list.csv（带 X-API-Key），写入 Documents 并更新 local_version。
 * 完整性检查：至少 80 行，否则不保存。
 * version 为空字符串时只更新文件、不写 local_version（用于手动更新）。
 */
fun downloadNewCsv(version: String, onResult: (Boolean, String) -> Unit) {
    fetchWithHeaders("${API_BASE_URL}file_list.csv", mapOf("X-API-Key" to API_KEY)) { ok, csv ->
        if (!ok || csv.isEmpty()) {
            onResult(false, "下载失败")
            return@fetchWithHeaders
        }
        if (csv.lines().size < 80) {
            onResult(false, "CSV 不完整（${csv.lines().size} 行）")
            return@fetchWithHeaders
        }
        val saved = writeToDocuments("file_list.csv", csv)
        if (saved) {
            if (version.isNotEmpty()) {
                Preferences().putString("local_version", version)
                Logger.i("CSV", "已更新 file_list.csv 至 v$version")
            } else {
                Logger.i("CSV", "已更新 file_list.csv")
            }
            onResult(true, "")
        } else {
            onResult(false, "保存失败")
        }
    }
}

/**
 * 手动更新 CSV：拉取版本号后强制下载最新 file_list.csv。
 * 用于设置页"手动更新 CSV"按钮，绕过版本差异检查。
 */
fun downloadLatestCsv(onResult: (Boolean, String) -> Unit) {
    fetchWithHeaders("${API_BASE_URL}Version_difference.json", mapOf("X-API-Key" to API_KEY)) { ok, body ->
        val version = if (ok) extractJsonString(body, "version") else null
        downloadNewCsv(version ?: "", onResult)
    }
}

/**
 * 手写提取 JSON 中指定键的字符串值。
 * 找到第一个 "key" 后冒号后的首个双引号字符串。找不到或非字符串返回 null。
 */
private fun extractJsonString(json: String, key: String): String? {
    val kq = "\"$key\""
    var pos = 0
    while (true) {
        val keyIdx = json.indexOf(kq, pos)
        if (keyIdx == -1) return null
        val colon = json.indexOf(':', keyIdx + kq.length)
        if (colon == -1) return null
        var i = colon + 1
        while (i < json.length && json[i].isWhitespace()) i++
        if (i < json.length && json[i] == '"') {
            val qEnd = json.indexOf('"', i + 1)
            if (qEnd != -1) return json.substring(i + 1, qEnd)
        }
        pos = keyIdx + kq.length
    }
}

/**
 * 手写提取 JSON 数组中指定对象的字符串字段值列表。
 * 定位 "arrayKey": [...] 区间，收集其中所有 "field":"..." 值。
 */
private fun extractArrayField(json: String, arrayKey: String, field: String): List<String> {
    val kq = "\"$arrayKey\""
    val keyIdx = json.indexOf(kq)
    if (keyIdx == -1) return emptyList()
    val arrStart = json.indexOf('[', keyIdx)
    if (arrStart == -1) return emptyList()
    val arrEnd = json.indexOf(']', arrStart)
    if (arrEnd == -1) return emptyList()
    val body = json.substring(arrStart, arrEnd)
    val fq = "\"$field\""
    val result = mutableListOf<String>()
    var pos = 0
    while (true) {
        val fi = body.indexOf(fq, pos)
        if (fi == -1) break
        val colon = body.indexOf(':', fi + fq.length)
        if (colon == -1) break
        var i = colon + 1
        while (i < body.length && body[i].isWhitespace()) i++
        if (i < body.length && body[i] == '"') {
            val qEnd = body.indexOf('"', i + 1)
            if (qEnd != -1) result.add(body.substring(i + 1, qEnd))
            pos = qEnd + 1
        } else pos = colon + 1
    }
    return result
}
