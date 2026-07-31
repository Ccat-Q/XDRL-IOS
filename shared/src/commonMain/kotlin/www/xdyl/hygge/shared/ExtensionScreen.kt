package www.xdyl.hygge.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExtensionView(onBack: () -> Unit) {
    var unlockThread by remember { mutableStateOf(false) }
    var useLocalCsv by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { Logger.i("Extension", "返回"); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFFA0C4FF))
            }
            Text("扩展功能", color = Color(0xFFA0C4FF), fontSize = 19.sp)
        }
        HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("解锁线程限制 (最大1024)", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = unlockThread,
                    onCheckedChange = { unlockThread = it; Logger.i("Extension", "解锁线程: $it") },
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
                Text("使用本地 CSV 文件列表", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = useLocalCsv,
                    onCheckedChange = { useLocalCsv = it; Logger.i("Extension", "本地CSV: $it") },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFA0C4FF),
                        checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)
                    )
                )
            }
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // 重置按钮
            Button(
                onClick = {
                    Logger.w("Extension", "重置所有设置")
                    Logger.clear()
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF553333),
                    contentColor = Color(0xFFFFAAAA)
                )
            ) {
                Text("重置所有设置", fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "扩展功能为高级选项，请谨慎使用",
                color = Color.Gray, fontSize = 12.sp
            )
        }
    }
}
