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
fun ExtensionView(
    unlockThread: Boolean,
    onUnlockChange: (Boolean) -> Unit,
    useLocalCsv: Boolean,
    onLocalCsvChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
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
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF))
            }
            Text("扩展功能", color = Color(0xFFA0C4FF), fontSize = 19.sp)
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SwitchRow("解锁线程限制 (最大1024)", unlockThread, onUnlockChange)
            HorizontalDivider(color = Color(0xFF3A3A3A)); Spacer(Modifier.height(8.dp))
            SwitchRow("使用本地 CSV 文件列表", useLocalCsv, onLocalCsvChange)
            HorizontalDivider(color = Color(0xFF3A3A3A)); Spacer(Modifier.height(24.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF553333), contentColor = Color(0xFFFFAAAA))
            ) { Text("重置所有设置", fontSize = 16.sp) }
            Spacer(Modifier.height(8.dp))
            Text("扩展功能为高级选项，请谨慎使用", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA0C4FF), checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)))
    }
}
