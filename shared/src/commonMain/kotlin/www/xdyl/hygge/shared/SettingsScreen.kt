package www.xdyl.hygge.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun SettingsView(onBack: () -> Unit, onExtension: () -> Unit) {
    var versionName by remember { mutableStateOf("1.21.1-NeoForge") }
    var threadCount by remember { mutableStateOf("256") }
    var neoforgeCheck by remember { mutableStateOf(true) }
    var cleanOrphan by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        // 返回栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { Logger.i("Settings", "返回"); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFFA0C4FF))
            }
            Text("设置", color = Color(0xFFA0C4FF), fontSize = 19.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { Logger.i("Settings", "扩展"); onExtension() }) {
                Icon(Icons.Default.Extension, contentDescription = "扩展", tint = Color(0xFFA0C4FF))
            }
        }
        HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("版本文件夹名称", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = versionName,
                onValueChange = { versionName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA0C4FF),
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    cursorColor = Color(0xFFA0C4FF)
                )
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            Text("下载线程数 (20-1024)", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = threadCount,
                onValueChange = { v ->
                    v.toIntOrNull()?.let { threadCount = it.coerceIn(20, 1024).toString() }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA0C4FF),
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    cursorColor = Color(0xFFA0C4FF)
                )
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("开启 NeoForge 版本检查", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = neoforgeCheck,
                    onCheckedChange = { neoforgeCheck = it; Logger.i("Settings", "NeoForge: $it") },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFA0C4FF),
                        checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)
                    )
                )
            }
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("更新后自动清理多余文件", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = cleanOrphan,
                    onCheckedChange = { cleanOrphan = it; Logger.i("Settings", "自动清理: $it") },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFA0C4FF),
                        checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)
                    )
                )
            }
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = { Logger.clear(); Logger.i("Settings", "日志已清除") },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))
            ) { Text("清除日志", fontSize = 16.sp) }
            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { Logger.i("Settings", "Nebula Updater v1.0 iOS") },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))
            ) { Text("关于", fontSize = 16.sp) }
        }
    }
}
