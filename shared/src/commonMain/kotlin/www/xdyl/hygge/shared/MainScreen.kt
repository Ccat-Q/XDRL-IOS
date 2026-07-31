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
    val logText by Logger.logs.collectAsState()
    val docsDir = remember { getDocumentsDir() }

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater iOS 启动")
        Logger.i("App", "Documents: $docsDir")
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        AnimatedContent(targetState = currentScreen, transitionSpec = {
            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
        }) { screen ->
            when (screen) {
                "main" -> MainView(
                    docsDir = docsDir,
                    onStartDownload = {
                        downloading = true
                        Logger.i("UI", "开始下载到 $docsDir")
                        statusText = "下载中..."
                    },
                    downloading = downloading,
                    logText = logText,
                    progress = progress,
                    statusText = statusText,
                    onSettings = { currentScreen = "settings" }
                )
                "settings" -> SettingsView(
                    onBack = { currentScreen = "main" },
                    onExtension = { currentScreen = "extension" }
                )
                "extension" -> ExtensionView(
                    onBack = { currentScreen = "settings" }
                )
            }
        }
    }
}

@Composable
private fun MainView(
    docsDir: String,
    onStartDownload: () -> Unit,
    downloading: Boolean,
    logText: String,
    progress: Float,
    statusText: String,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        // 标题行 + 设置按钮
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp)
                Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(12.dp))

        // 下载按钮
        Button(
            onClick = onStartDownload,
            enabled = !downloading,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)
        ) { Text(if (downloading) "下载中..." else "开始下载", fontSize = 16.sp) }
        Spacer(Modifier.height(10.dp))

        // 进度条
        if (progress > 0f) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFA0C4FF),
                trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(4.dp))
        }
        if (statusText.isNotEmpty()) {
            Text(statusText, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
        }

        // 日志
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val scroll = rememberScrollState()
            Text(
                logText.ifEmpty { "日志将显示在这里..." },
                modifier = Modifier.verticalScroll(scroll).padding(4.dp).fillMaxWidth(),
                fontSize = 12.sp, color = Color.LightGray,
                maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip
            )
        }
    }
}
