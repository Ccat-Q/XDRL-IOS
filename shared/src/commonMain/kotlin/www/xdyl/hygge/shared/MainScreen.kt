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

private val DEFAULT_CSV = """
./Applied-Mekanistics-1.6.3.jar,147K,149709,0ef21d62aaa1e318f2f93adabe6c56a2,8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773
./AppliedFlux-1.21-2.1.5-neoforge.jar,338K,345117,aced1a1af01d7411772634aa13826a18,57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b
./Jade-1.21.1-NeoForge-15.10.5.jar,709K,725742,80f9186d25b02ebbfa5773416f5da410,067bb4b007e1d6f6b79f0afe99c91252aa825472b99a76d33a60d24442f9e92d
./ImmediatelyFast-NeoForge-1.6.11+1.21.1.jar,354K,361795,f432a12463accb05290ea7de52fccc43,336df12f099d1a441a3e06850bea86e9c2d0c8bc022d3d9a201870a201562a04
""".trimIndent()

private data class ModFile(val name: String, val size: Long, val md5: String, val sha256: String)

private fun parseCsv(csv: String): List<ModFile> = csv.lines().filter { it.isNotBlank() }.mapNotNull { line ->
    val parts = line.split(",")
    if (parts.size >= 5) ModFile(parts[0].trim('"').removePrefix("./"), parts[2].toLongOrNull() ?: return@mapNotNull null, parts[3].trim('"'), parts[4].trim('"')) else null
}

// 主线程安全的状态更新包装
private fun onMain(block: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) { block() }
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

    var versionName by remember { mutableStateOf(prefs.getString("version_folder", "1.21.1-NeoForge") ?: "1.21.1-NeoForge") }
    var threadCount by remember { mutableStateOf(prefs.getInt("thread_limit", 256).toString()) }
    var serverUrl by remember { mutableStateOf(prefs.getString("server_url", "http://82.157.155.86:5551/mods/") ?: "http://82.157.155.86:5551/mods/") }
    var cleanOrphan by remember { mutableStateOf(prefs.getBoolean("clean_orphan_files", true)) }
    var unlockThread by remember { mutableStateOf(prefs.getBoolean("unlock_thread_limit", false)) }
    var useLocalCsv by remember { mutableStateOf(prefs.getBoolean("use_local_csv", false)) }

    fun reloadPrefs() {
        versionName = prefs.getString("version_folder", "1.21.1-NeoForge") ?: "1.21.1-NeoForge"
        threadCount = prefs.getInt("thread_limit", 256).toString()
        serverUrl = prefs.getString("server_url", "http://82.157.155.86:5551/mods/") ?: "http://82.157.155.86:5551/mods/"
        cleanOrphan = prefs.getBoolean("clean_orphan_files", true)
        unlockThread = prefs.getBoolean("unlock_thread_limit", false)
        useLocalCsv = prefs.getBoolean("use_local_csv", false)
    }

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater iOS v1.0")
        val ok = writeToDocuments("readme.txt", "Nebula Updater - 下载文件存放于此")
        Logger.i("App", "启动写入: $ok")
    }

    fun downloadSequential(mods: List<ModFile>, idx: Int, done: Int, failed: Int, dir: String) {
        if (idx >= mods.size) {
            onMain {
                downloading = false
                progress = 1f
                statusText = "完成: $done 成功, $failed 失败"
                Logger.i("Download", "===== $done OK, $failed FAIL =====")
            }
            return
        }
        val mod = mods[idx]
        val url = serverUrl.trimEnd('/') + "/" + mod.name
        val dest = "$dir/${mod.name}"
        onMain {
            progress = idx.toFloat() / mods.size
            statusText = "[${idx + 1}/${mods.size}] ${mod.name}"
            Logger.i("Download", "GET $url")
        }
        downloadFile(url, dest, { filePct ->
            onMain { progress = idx.toFloat() / mods.size + filePct / mods.size }
        }) { ok, msg ->
            onMain {
                if (ok) { Logger.i("Download", "OK: ${mod.name}"); downloadSequential(mods, idx + 1, done + 1, failed, dir) }
                else { Logger.e("Download", "FAIL: ${mod.name} - $msg"); downloadSequential(mods, idx + 1, done, failed + 1, dir) }
            }
        }
    }

    fun startDownload() {
        if (downloading) return
        val mods = parseCsv(DEFAULT_CSV)
        if (mods.isEmpty()) { statusText = "列表为空"; return }
        downloading = true; progress = 0f
        Logger.i("Download", "===== ${mods.size} files, server: $serverUrl =====")
        statusText = "共 ${mods.size} 个文件"
        val dir = "$docsDir/$versionName/mods"
        downloadSequential(mods, 0, 0, 0, dir)
    }

    if (showAbout) AlertDialog(onDismissRequest = { showAbout = false },
        title = { Text("Nebula Updater-NU", color = Color(0xFFA0C4FF)) },
        text = { Column { Text("星云更新器 iOS v1.0", color = Color.White); Spacer(Modifier.height(8.dp)); Text("路径:", color = Color.Gray, fontSize = 12.sp); Text(docsDir, color = Color.LightGray, fontSize = 11.sp) } },
        confirmButton = { TextButton(onClick = { showAbout = false }) { Text("确定", color = Color(0xFFA0C4FF)) } }, containerColor = Color(0xFF2A2A2A))

    if (showErrorCodes) AlertDialog(onDismissRequest = { showErrorCodes = false },
        title = { Text("ERROR 错误代码", color = Color(0xFFA0C4FF)) },
        text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { for ((c, d) in listOf("ERROR01" to "找不到游戏目录","ERROR02" to "没有文件读写权限","ERROR03" to "网络连接超时","ERROR05" to "模组文件校验失败","ERROR08" to "无法获取文件列表","ERROR10" to "未知错误")) { Text("$c: $d", color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(2.dp)) } } },
        confirmButton = { TextButton(onClick = { showErrorCodes = false }) { Text("关闭", color = Color(0xFFA0C4FF)) } }, containerColor = Color(0xFF2A2A2A))

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        AnimatedContent(targetState = currentScreen, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) }) { screen ->
            when (screen) {
                "main" -> MainView(
                    { startDownload() }, downloading, logText, progress, statusText,
                    { currentScreen = "settings" },
                    {
                        onMain {
                            val ok = writeToDocuments("nebula_log.txt", Logger.getRaw())
                            statusText = if (ok) "日志已导出" else "导出失败"
                            Logger.i("App", "导出日志: $ok")
                        }
                    })
                "settings" -> SettingsView(
                    versionName, { v -> versionName = v; prefs.putString("version_folder", v) },
                    threadCount, { t -> threadCount = t; prefs.putInt("thread_limit", t.toIntOrNull() ?: 256) },
                    serverUrl, { s -> serverUrl = s; prefs.putString("server_url", s) },
                    cleanOrphan, { c -> cleanOrphan = c; prefs.putBoolean("clean_orphan_files", c) },
                    { Logger.clear() }, { showAbout = true }, { showErrorCodes = true },
                    { currentScreen = "extension" }, { currentScreen = "main" })
                "extension" -> ExtensionView(
                    unlockThread, { u -> unlockThread = u; prefs.putBoolean("unlock_thread_limit", u) },
                    useLocalCsv, { c -> useLocalCsv = c; prefs.putBoolean("use_local_csv", c) },
                    {
                        prefs.clear()
                        Logger.clear()
                        reloadPrefs()
                        statusText = "已重置"
                        Logger.i("App", "已重置所有设置")
                    },
                    { currentScreen = "settings" })
            }
        }
    }
}

@Composable
private fun MainView(onStart: () -> Unit, down: Boolean, log: String, prog: Float, stat: String, onSet: () -> Unit, onExp: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp); Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
            IconButton(onClick = onSet, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Settings, "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp)) }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onStart, enabled = !down, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)) { Text(if (down) "下载中..." else "开始下载", fontSize = 16.sp) }
        Spacer(Modifier.height(8.dp))
        if (down || prog > 0f) {
            LinearProgressIndicator(progress = { prog.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFFA0C4FF), trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f))
            if (stat.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(stat, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Text(log.ifEmpty { "点击开始下载..." }, Modifier.verticalScroll(rememberScrollState()).padding(4.dp).fillMaxWidth(), fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { Logger.clear() }) { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
            TextButton(onClick = onExp) { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
        }
    }
}
