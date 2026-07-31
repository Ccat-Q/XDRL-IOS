package www.xdyl.hygge.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    var targetModsDir by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }

    val logText by Logger.logs.collectAsState()

    LaunchedEffect(Unit) {
        Logger.i("App", "Nebula Updater 启动")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 22.sp)
            Text("星云更新器", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                Logger.i("UI", "点击选择游戏目录")
                statusText = "请选择游戏目录"
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA0C4FF),
                contentColor = Color.Black
            )
        ) {
            Text("选择游戏目录", fontSize = 17.sp)
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                Logger.i("UI", "点击开始下载")
                downloading = true
                statusText = "正在下载..."
            },
            enabled = !downloading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA0C4FF),
                contentColor = Color.Black
            )
        ) {
            Text("开始下载", fontSize = 17.sp)
        }
        Spacer(Modifier.height(16.dp))

        if (progress > 0f) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFA0C4FF),
                trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(4.dp))
        }

        if (statusText.isNotEmpty()) {
            Text(statusText, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val scrollState = rememberScrollState()
            Text(
                logText.ifEmpty { "日志将显示在这里..." },
                modifier = Modifier.verticalScroll(scrollState).padding(6.dp).fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = {
                Logger.clear()
                Logger.i("App", "日志已清除")
            }) {
                Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 14.sp)
            }
        }
    }
}
