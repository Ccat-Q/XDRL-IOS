package www.xdyl.hygge.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsView(
    versionName: String, onVersionChange: (String) -> Unit,
    threadCount: String, onThreadChange: (String) -> Unit,
    serverUrl: String, onServerUrlChange: (String) -> Unit,
    cleanOrphan: Boolean, onCleanOrphanChange: (Boolean) -> Unit,
    onClearLog: () -> Unit, onAbout: () -> Unit, onErrorCodes: () -> Unit,
    onExtension: () -> Unit, onBack: () -> Unit, onNetwork: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF)) }
            Text("设置", color = Color(0xFFA0C4FF), fontSize = 19.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onExtension) { Icon(Icons.Default.Extension, "扩展", tint = Color(0xFFA0C4FF)) }
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("版本文件夹名称", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(versionName, onVersionChange, singleLine = true, modifier = Modifier.fillMaxWidth().height(48.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
            Spacer(Modifier.height(12.dp))

            Text("下载线程数 (20-1024)", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(threadCount, { v -> v.toIntOrNull()?.let { onThreadChange(it.coerceIn(20, 1024).toString()) } }, singleLine = true, modifier = Modifier.fillMaxWidth().height(48.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
            Spacer(Modifier.height(12.dp))

            Text("下载服务器地址", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(serverUrl, onServerUrlChange, singleLine = true, placeholder = { Text("留空则使用默认地址", color = Color.Gray, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth().height(48.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("更新后自动清理多余文件", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(cleanOrphan, onCheckedChange = onCleanOrphanChange, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA0C4FF), checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)))
            }
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClearLog, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("清除日志", fontSize = 14.sp) }
                OutlinedButton(onClick = onAbout, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("关于", fontSize = 14.sp) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onErrorCodes, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("ERROR 错误代码", fontSize = 16.sp) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onNetwork, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("网络调试", fontSize = 16.sp) }
        }
    }
}

