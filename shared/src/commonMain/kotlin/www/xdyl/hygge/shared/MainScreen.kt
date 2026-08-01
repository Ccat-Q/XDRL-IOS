package www.xdyl.hygge.shared

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

    var downloadCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        Logger.i("App", "===== Nebula Updater iOS v1.0 =====")
        Logger.i("App", "Documents: ${getDocumentsDir()}")
        val ok = writeToDocuments("readme.txt", "测试文件写入 - ${getDocumentsDir()}")
        Logger.i("App", "启动文件写入: $ok")
    }

    // 简单下载测试
    fun testDownload() {
        if (downloading) return
        downloading = true
        downloadCount++
        progress = 0f
        statusText = "测试下载 #$downloadCount..."
        Logger.i("Test", "开始测试下载 #$downloadCount")
        val docs = getDocumentsDir()
        val url = "http://82.157.155.86:5551/mods/Jade-1.21.1-NeoForge-15.10.5.jar"
        val dest = "$docs/test_download.jar"
        Logger.i("Test", "URL: $url")
        Logger.i("Test", "Dest: $dest")
        downloadFile(url, dest, { pct ->
            progress = pct
            statusText = "进度: ${(pct * 100).toInt()}%"
        }) { ok, msg ->
            downloading = false
            progress = if (ok) 1f else 0f
            statusText = if (ok) "下载成功!" else "失败: $msg"
            Logger.i("Test", if (ok) "下载成功" else "下载失败: $msg")
        }
    }

    if (showAbout) AlertDialog(onDismissRequest = { showAbout = false },
        title = { Text("关于", color = Color(0xFFA0C4FF)) },
        text = { Text("Nebula Updater iOS v1.0\n路径: ${getDocumentsDir()}", color = Color.White) },
        confirmButton = { TextButton(onClick = { showAbout = false }) { Text("确定", color = Color(0xFFA0C4FF)) } },
        containerColor = Color(0xFF2A2A2A))

    if (showErrorCodes) AlertDialog(onDismissRequest = { showErrorCodes = false },
        title = { Text("ERROR", color = Color(0xFFA0C4FF)) },
        text = { Column {
            listOf("ERROR01 找不到目录","ERROR02 无权限","ERROR03 网络超时","ERROR05 校验失败","ERROR08 无法获取列表","ERROR10 未知错误").forEach { Text(it, color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(2.dp)) }
        }},
        confirmButton = { TextButton(onClick = { showErrorCodes = false }) { Text("关闭", color = Color(0xFFA0C4FF)) } },
        containerColor = Color(0xFF2A2A2A))

    Box(Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        when (currentScreen) {
            "main" -> Column(Modifier.fillMaxSize().padding(16.dp).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp); Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
                    IconButton(onClick = { currentScreen = "settings" }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Settings, "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp)) }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { testDownload() }, enabled = !downloading, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)) { Text(if (downloading) "下载中..." else "测试下载", fontSize = 16.sp) }
                Spacer(Modifier.height(8.dp))
                if (downloading || progress > 0f) {
                    LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFFA0C4FF), trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f))
                    if (statusText.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(statusText, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Text(logText.ifEmpty { "日志..." }, Modifier.verticalScroll(rememberScrollState()).padding(4.dp).fillMaxWidth(), fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { Logger.clear(); Logger.i("App", "日志已清除") }) { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                    TextButton(onClick = {
                        val ok = writeToDocuments("nebula_log.txt", Logger.getRaw())
                        statusText = if (ok) "日志已导出" else "导出失败"
                        Logger.i("App", "导出: $ok")
                    }) { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                }
            }
            "settings" -> SettingsView(
                "1.21.1-NeoForge", { Logger.i("Settings", "版本变更: $it") },
                "256", { Logger.i("Settings", "线程: $it") },
                "http://82.157.155.86:5551/mods/", { Logger.i("Settings", "服务器: $it") },
                true, { Logger.i("Settings", "清理: $it") },
                { Logger.clear() }, { showAbout = true }, { showErrorCodes = true },
                { currentScreen = "extension" }, { currentScreen = "main" })
            "extension" -> ExtensionView(
                false, { Logger.i("Ext", "解锁: $it") }, false, { Logger.i("Ext", "CSV: $it") },
                { Logger.clear(); Logger.i("Ext", "重置"); statusText = "已重置" },
                { currentScreen = "settings" })
        }
    }
}
