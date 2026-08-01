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
fun NetworkView(srv: String, onBack: () -> Unit) {
    var result by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }
    var testUrl by remember { mutableStateOf(srv) }

    Column(Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF)) }
            Text("网络调试", color = Color(0xFFA0C4FF), fontSize = 19.sp)
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(Modifier.weight(1f).padding(16.dp)) {
            Text("测试地址", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(testUrl, { testUrl = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ testing = true; result = "测试中..."; Logger.i("Net", "PING: $testUrl"); pingServer(testUrl) { ok, msg -> testing = false; result = if (ok) "OK: $msg" else "FAIL: $msg"; Logger.i("Net", result) } }, enabled = !testing, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)) { Text(if (testing) "测试中..." else "PING 测试", fontSize = 16.sp) }
            }
            Spacer(Modifier.height(12.dp))

            if (result.isNotEmpty()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))) {
                    Text(result, Modifier.padding(12.dp), color = if (result.startsWith("OK")) Color(0xFF80FF80) else Color(0xFFFF8080), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(12.dp))
            Text("常用测试地址", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            listOf(
                srv to "下载服务器",
                "http://82.157.155.86:5551/" to "服务器根路径",
                "https://www.baidu.com" to "百度 (外网)"
            ).forEach { (url, label) ->
                TextButton({ testUrl = url; Logger.i("Net", "切换: $label") }, Modifier.fillMaxWidth()) {
                    Text("$label: $url", color = Color(0xFFA0C4FF).copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}
