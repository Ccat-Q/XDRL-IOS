package www.xdyl.hygge.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    unlockThread: Boolean, onUnlockChange: (Boolean) -> Unit,
    useLocalCsv: Boolean, onLocalCsvChange: (Boolean) -> Unit,
    useJson: Boolean, onUseJsonChange: (Boolean) -> Unit,
    devMode: Boolean, onDevModeChange: (Boolean) -> Unit,
    csvFiles: List<String>, csvFileName: String, onCsvSelected: (String) -> Unit,
    onReset: () -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF)) }
            Text("扩展功能", color = Color(0xFFA0C4FF), fontSize = 19.sp)
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
            // 玻璃卡片：日志操作
            Box(Modifier.fillMaxWidth().background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp)).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Column {
                    Text("日志操作", color = Color(0xFFA0C4FF), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton({ Logger.clear(); Logger.i("App", "日志已清除") }, Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("清除日志", fontSize = 13.sp) }
                        OutlinedButton({ val ok = writeToDocuments("nebula_log.txt", Logger.getRaw()); Logger.i("App", if (ok) "日志已导出" else "导出失败") }, Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))) { Text("导出日志", fontSize = 13.sp) }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            SwitchRow("使用 JSON Manifest", useJson, onUseJsonChange)
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(8.dp))

            SwitchRow("开发者模式", devMode, onDevModeChange)
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(8.dp))

            SwitchRow("解锁线程限制 (最大1024)", unlockThread, onUnlockChange)
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(8.dp))

            SwitchRow("使用本地 CSV 文件列表", useLocalCsv, onLocalCsvChange)
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(8.dp))

            if (useLocalCsv) {
                Text("选择 Documents 中的 CSV 文件:", color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                if (csvFiles.isEmpty()) {
                    Text("Documents 中没有 CSV 文件，请通过文件 App 上传", color = Color(0xFFFFAAAA), fontSize = 12.sp)
                } else {
                    csvFiles.forEach { f ->
                        Row(Modifier.fillMaxWidth().clickable { onCsvSelected(f) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (f == csvFileName) "● " else "○ ", color = if (f == csvFileName) Color(0xFFA0C4FF) else Color.Gray)
                            Text(f, color = if (f == csvFileName) Color.White else Color.LightGray, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(24.dp))

            Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF553333), contentColor = Color(0xFFFFAAAA))
            ) { Text("重置所有设置", fontSize = 16.sp) }
            Spacer(Modifier.height(8.dp))
            Text("扩展功能为高级选项，请谨慎使用", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA0C4FF), checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)))
    }
}