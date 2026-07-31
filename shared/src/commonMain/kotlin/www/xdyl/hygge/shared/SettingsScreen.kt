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
fun SettingsView(
    versionName: String, onVersionChange: (String) -> Unit,
    threadCount: String, onThreadChange: (String) -> Unit,
    serverUrl: String, onServerUrlChange: (String) -> Unit,
    cleanOrphan: Boolean, onCleanOrphanChange: (Boolean) -> Unit,
    onClearLog: () -> Unit, onAbout: () -> Unit, onErrorCodes: () -> Unit,
    onExtension: () -> Unit, onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF)) }
            Text("设置", color = Color(0xFFA0C4FF), fontSize = 19.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onExtension) { Icon(Icons.Default.Extension, "扩展", tint = Color(0xFFA0C4FF)) }
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
            // 版本文件夹
            SettingsLabel("版本文件夹名称")
            SettingsTextField(versionName, onVersionChange)
            SettingsDivider()

            // 线程数
            SettingsLabel("下载线程数 (20-1024)")
            SettingsTextField(threadCount) { v -> v.toIntOrNull()?.let { onThreadChange(it.coerceIn(20, 1024).toString()) } }
            SettingsDivider()

            // 自定义服务器地址
            SettingsLabel("下载服务器地址")
            OutlinedTextField(
                value = serverUrl, onValueChange = onServerUrlChange, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF))
            )
            SettingsDivider()

            // 自动清理
            SwitchRow("更新后自动清理多余文件", cleanOrphan, onCleanOrphanChange)
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(20.dp))

            SettingsButton("清除日志", onClearLog)
            SettingsButton("关于") { onAbout() }
            SettingsButton("ERROR 错误代码") { onErrorCodes() }
        }
    }
}

@Composable
private fun SettingsLabel(text: String) {
    Text(text, color = Color.Gray, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun SettingsTextField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
}

@Composable
private fun SettingsDivider() {
    Spacer(Modifier.height(16.dp)); HorizontalDivider(color = Color(0xFF3A3A3A)); Spacer(Modifier.height(16.dp))
}

@Composable
private fun SettingsButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0C4FF))
    ) { Text(text, fontSize = 16.sp) }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA0C4FF), checkedTrackColor = Color(0xFFA0C4FF).copy(alpha = 0.5f)))
    }
}
