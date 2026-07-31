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
    var customPath by remember { mutableStateOf(prefs.getString("custom_path", docsDir) ?: docsDir) }
    var cleanOrphan by remember { mutableStateOf(prefs.getBoolean("clean_orphan_files", true)) }
    var unlockThread by remember { mutableStateOf(prefs.getBoolean("unlock_thread_limit", false)) }
    var useLocalCsv by remember { mutableStateOf(prefs.getBoolean("use_local_csv", false)) }

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater iOS v1.0")
        Logger.i("App", "下载路径: $customPath")
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
                    Text(customPath, color = Color.LightGray, fontSize = 11.sp)
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
                    for (err in listOf("ERROR01: 找不到游戏目录","ERROR02: 没有文件读写权限","ERROR03: 网络连接超时","ERROR05: 模组文件校验失败","ERROR06: 版本文件夹不匹配","ERROR07: NeoForge版本过低","ERROR08: 无法获取文件列表","ERROR10: 未知错误")) { Text(err, color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(2.dp)) }
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
                    customPath = customPath,
                    onStartDownload = {
                        downloading = true; statusText = "下载中..."
                        Logger.i("UI", "开始下载 -> $customPath/$versionName")
                        val ok = writeFile("$customPath/test_download.txt", "Nebula Updater 测试下载")
                        if (ok) { Logger.i("Download", "测试文件已创建"); statusText = "下载完成" }
                        else Logger.e("Download", "写入失败")
                        progress = 1f; downloading = false
                    },
                    downloading = downloading, logText = logText, progress = progress, statusText = statusText,
                    onSettings = { currentScreen = "settings" },
                    onClearLog = { Logger.clear(); Logger.i("App", "日志已清除") },
                    onExportLog = {
                        if (Logger.exportTo("$customPath/nebula_log.txt")) Logger.i("App", "日志已导出")
                        else Logger.e("App", "导出失败")
                    }
                )
                "settings" -> SettingsView(
                    versionName = versionName, onVersionChange = { versionName = it; prefs.putString("version_folder", it) },
                    threadCount = threadCount, onThreadChange = { threadCount = it; prefs.putInt("thread_limit", it.toIntOrNull() ?: 256) },
                    customPath = customPath, onCustomPathChange = { customPath = it; prefs.putString("custom_path", it) },
                    cleanOrphan = cleanOrphan, onCleanOrphanChange = { cleanOrphan = it; prefs.putBoolean("clean_orphan_files", it) },
                    onClearLog = { Logger.clear(); Logger.i("App", "日志已清除") },
                    onAbout = { showAbout = true }, onErrorCodes = { showErrorCodes = true },
                    onExtension = { currentScreen = "extension" }, onBack = { currentScreen = "main" }
                )
                "extension" -> ExtensionView(
                    unlockThread = unlockThread, onUnlockChange = { unlockThread = it; prefs.putBoolean("unlock_thread_limit", it) },
                    useLocalCsv = useLocalCsv, onLocalCsvChange = { useLocalCsv = it; prefs.putBoolean("use_local_csv", it) },
                    onReset = { prefs.clear(); Logger.clear(); Logger.i("App", "所有设置已重置") },
                    onBack = { currentScreen = "settings" }
                )
            }
        }
    }
}

@Composable
private fun MainView(
    customPath: String, onStartDownload: () -> Unit, downloading: Boolean,
    logText: String, progress: Float, statusText: String,
    onSettings: () -> Unit, onClearLog: () -> Unit, onExportLog: () -> Unit
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
                Icon(Icons.Default.Settings, null, tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("下载到: $customPath", color = Color.Gray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))

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
            Text(logText.ifEmpty { "日志将显示在这里..." },
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(4.dp).fillMaxWidth(),
                fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onClearLog) { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
            TextButton(onClick = onExportLog) { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
        }
    }
}
