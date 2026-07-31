package www.xdyl.hygge.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

// 默认 CSV（与服务端同步的模组列表）
private val DEFAULT_CSV = """
./Applied-Mekanistics-1.6.3.jar,147K,149709,0ef21d62aaa1e318f2f93adabe6c56a2,8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773
./AppliedFlux-1.21-2.1.5-neoforge.jar,338K,345117,aced1a1af01d7411772634aa13826a18,57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b
./Jade-1.21.1-NeoForge-15.10.5.jar,709K,725742,80f9186d25b02ebbfa5773416f5da410,067bb4b007e1d6f6b79f0afe99c91252aa825472b99a76d33a60d24442f9e92d
./ImmediatelyFast-NeoForge-1.6.11+1.21.1.jar,354K,361795,f432a12463accb05290ea7de52fccc43,336df12f099d1a441a3e06850bea86e9c2d0c8bc022d3d9a201870a201562a04
""".trimIndent()

private data class ModFile(val name: String, val size: Long, val md5: String, val sha256: String)

private fun parseCsv(csv: String): List<ModFile> {
    return csv.lines().filter { it.isNotBlank() }.mapNotNull { line ->
        val parts = line.split(",")
        if (parts.size >= 5) {
            val name = parts[0].trim('"').removePrefix("./")
            val size = parts[2].toLongOrNull() ?: return@mapNotNull null
            val md5 = parts[3].trim('"')
            val sha256 = parts[4].trim('"')
            ModFile(name, size, md5, sha256)
        } else null
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("main") }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showErrorCodes by remember { mutableStateOf(false) }
    val logText by Logger.logs.collectAsState()
    val docsDir = remember { getDocumentsDir() }
    val prefs = remember { Preferences() }
    val scope = rememberCoroutineScope()

    // 持久化状态
    var versionName by remember { mutableStateOf(prefs.getString("version_folder", "1.21.1-NeoForge") ?: "1.21.1-NeoForge") }
    var threadCount by remember { mutableStateOf(prefs.getInt("thread_limit", 256).toString()) }
    var serverUrl by remember { mutableStateOf(prefs.getString("server_url", "http://82.157.155.86:5551/mods/") ?: "http://82.157.155.86:5551/mods/") }
    var cleanOrphan by remember { mutableStateOf(prefs.getBoolean("clean_orphan_files", true)) }
    var unlockThread by remember { mutableStateOf(prefs.getBoolean("unlock_thread_limit", false)) }
    var useLocalCsv by remember { mutableStateOf(prefs.getBoolean("use_local_csv", false)) }
    // 强制刷新的key
    var resetKey by remember { mutableStateOf(0) }

    fun reloadPrefs() {
        versionName = prefs.getString("version_folder", "1.21.1-NeoForge") ?: "1.21.1-NeoForge"
        threadCount = prefs.getInt("thread_limit", 256).toString()
        serverUrl = prefs.getString("server_url", "http://82.157.155.86:5551/mods/") ?: "http://82.157.155.86:5551/mods/"
        cleanOrphan = prefs.getBoolean("clean_orphan_files", true)
        unlockThread = prefs.getBoolean("unlock_thread_limit", false)
        useLocalCsv = prefs.getBoolean("use_local_csv", false)
        resetKey++
    }

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater iOS v1.0")
        Logger.i("App", "Documents: $docsDir")
        Logger.i("App", "服务器: $serverUrl")
    }

    // 下载逻辑
    fun startDownload() {
        downloading = true
        progress = 0f
        statusText = "解析文件列表..."
        val mods = parseCsv(DEFAULT_CSV)
        Logger.i("Download", "共 ${mods.size} 个文件")
        if (mods.isEmpty()) {
            downloading = false
            statusText = "文件列表为空"
            return
        }
        scope.launch {
            val modsDir = "$docsDir/$versionName/mods"
            writeToDocuments("$versionName/mods/.info", "服务器: $serverUrl")
            var done = 0
            var failed = 0
            for ((i, mod) in mods.withIndex()) {
                val url = serverUrl.trimEnd('/') + "/" + mod.name
                val dest = "$modsDir/${mod.name}"
                statusText = "[${i + 1}/${mods.size}] ${mod.name}"
                Logger.i("Download", "[$i/${mods.size}] $url")
                downloadFile(url, dest, { p ->
                    progress = ((done + p) / mods.size)
                }) { ok, msg ->
                    if (ok) {
                        done++
                        Logger.i("Download", "OK: ${mod.name}")
                    } else {
                        failed++
                        Logger.e("Download", "FAIL: ${mod.name} - $msg")
                    }
                    progress = done.toFloat() / mods.size
                    if (done + failed >= mods.size) {
                        downloading = false
                        statusText = "完成: $done 成功, $failed 失败"
                        Logger.i("Download", "完成: $done/$done+$failed")
                    }
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Nebula Updater-NU", color = Color(0xFFA0C4FF)) },
            text = {
                Column {
                    Text("星云更新器 iOS v1.0", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("下载路径:", color = Color.Gray, fontSize = 12.sp)
                    Text(docsDir, color = Color.LightGray, fontSize = 11.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("确定", color = Color(0xFFA0C4FF)) } },
            containerColor = Color(0xFF2A2A2A)
        )
    }

    if (showErrorCodes) {
        AlertDialog(
            onDismissRequest = { showErrorCodes = false },
            title = { Text("ERROR 错误代码", color = Color(0xFFA0C4FF)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    for ((code, desc) in listOf(
                        "ERROR01" to "找不到游戏目录", "ERROR02" to "没有文件读写权限",
                        "ERROR03" to "网络连接超时", "ERROR05" to "模组文件校验失败",
                        "ERROR08" to "无法获取文件列表", "ERROR10" to "未知错误"
                    )) {
                        Text("$code: $desc", color = Color.White, fontSize = 13.sp)
                        Spacer(Modifier.height(2.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showErrorCodes = false }) { Text("关闭", color = Color(0xFFA0C4FF)) } },
            containerColor = Color(0xFF2A2A2A)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        AnimatedContent(targetState = currentScreen, transitionSpec = {
            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
        }) { screen ->
            when (screen) {
                "main" -> MainView(
                    onStartDownload = { startDownload() },
                    downloading = downloading,
                    logText = logText,
                    progress = progress,
                    statusText = statusText,
                    onSettings = { currentScreen = "settings" },
                    onExportLog = {
                        val ok = writeToDocuments("nebula_log.txt", Logger.getRaw())
                        if (ok) { Logger.i("App", "日志已导出"); statusText = "日志已导出到文件App" }
                        else Logger.e("App", "导出失败")
                    }
                )
                "settings" -> SettingsView(
                    versionName = versionName,
                    onVersionChange = { versionName = it; prefs.putString("version_folder", it) },
                    threadCount = threadCount,
                    onThreadChange = { threadCount = it; prefs.putInt("thread_limit", it.toIntOrNull() ?: 256) },
                    serverUrl = serverUrl,
                    onServerUrlChange = { serverUrl = it; prefs.putString("server_url", it) },
                    cleanOrphan = cleanOrphan,
                    onCleanOrphanChange = { cleanOrphan = it; prefs.putBoolean("clean_orphan_files", it) },
                    onClearLog = { Logger.clear(); Logger.i("App", "日志已清除") },
                    onAbout = { showAbout = true },
                    onErrorCodes = { showErrorCodes = true },
                    onExtension = { currentScreen = "extension" },
                    onBack = { currentScreen = "main" }
                )
                "extension" -> ExtensionView(
                    unlockThread = unlockThread,
                    onUnlockChange = { unlockThread = it; prefs.putBoolean("unlock_thread_limit", it) },
                    useLocalCsv = useLocalCsv,
                    onLocalCsvChange = { useLocalCsv = it; prefs.putBoolean("use_local_csv", it) },
                    onReset = {
                        prefs.clear()
                        Logger.clear()
                        reloadPrefs()
                        Logger.i("App", "所有设置已重置")
                    },
                    onBack = { currentScreen = "settings" }
                )
            }
        }
    }
}

@Composable
private fun MainView(
    onStartDownload: () -> Unit, downloading: Boolean, logText: String,
    progress: Float, statusText: String, onSettings: () -> Unit, onExportLog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp)
                Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(12.dp))

        Button(onClick = onStartDownload, enabled = !downloading,
            modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)
        ) { Text(if (downloading) "下载中..." else "开始下载", fontSize = 16.sp) }
        Spacer(Modifier.height(10.dp))

        if (progress > 0f) {
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFA0C4FF), trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f))
            Spacer(Modifier.height(4.dp))
        }
        if (statusText.isNotEmpty()) {
            Text(statusText, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val scroll = rememberScrollState()
            Text(logText.ifEmpty { "点击开始下载..." },
                modifier = Modifier.verticalScroll(scroll).padding(4.dp).fillMaxWidth(),
                fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { Logger.clear(); Logger.i("App", "日志已清除") })
                { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
            TextButton(onClick = onExportLog)
                { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
        }
    }
}
