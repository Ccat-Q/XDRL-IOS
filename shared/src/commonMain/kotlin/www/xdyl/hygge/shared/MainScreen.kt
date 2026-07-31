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
    var targetModsDir by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }
    val logText by Logger.logs.collectAsState()

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater 启动")
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        AnimatedContent(targetState = currentScreen, transitionSpec = {
            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
        }) { screen ->
            when (screen) {
                "main" -> MainView(
                    targetModsDir = targetModsDir,
                    onSelectDir = { Logger.i("UI", "选择目录"); statusText = "选择游戏目录..." },
                    onStartDownload = { downloading = true; Logger.i("UI", "开始下载"); statusText = "下载中..." },
                    downloading = downloading,
                    logText = logText,
                    progress = progress,
                    statusText = statusText,
                    onSettings = { currentScreen = "settings" }
                )
                "settings" -> SettingsView(
                    onBack = { currentScreen = "main" }
                )
            }
        }
    }
}

@Composable
private fun MainView(
    targetModsDir: String,
    onSelectDir: () -> Unit,
    onStartDownload: () -> Unit,
    downloading: Boolean,
    logText: String,
    progress: Float,
    statusText: String,
    onSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 22.sp)
                Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 14.sp)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSelectDir,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)
        ) { Text("选择游戏目录", fontSize = 16.sp) }
        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onStartDownload,
            enabled = !downloading,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)
        ) { Text(if (downloading) "下载中..." else "开始下载", fontSize = 16.sp) }
        Spacer(Modifier.height(12.dp))

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

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val scroll = rememberScrollState()
            Text(
                logText.ifEmpty { "日志将显示在这里..." },
                modifier = Modifier.verticalScroll(scroll).padding(4.dp).fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip
            )
        }
    }
}
