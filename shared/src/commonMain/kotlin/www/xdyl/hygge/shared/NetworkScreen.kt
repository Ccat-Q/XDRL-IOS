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
    var testUrl by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFFA0C4FF)) }
            Text("网络调试", color = Color(0xFFA0C4FF), fontSize = 19.sp)
        }
        HorizontalDivider(color = Color(0xFF3A3A3A))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // 按钮1: 测试下载服务器
            Button({
                testing = true; result = "正在连接下载服务器..."
                Logger.i("Net", "测试下载服务器: $srv")
                pingServer(srv) { ok, msg ->
                    testing = false; result = if (ok) "服务器连通: $msg" else "服务器不通: $msg"
                    Logger.i("Net", result)
                }
            }, enabled = !testing, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)
            ) { Text(if (testing && testUrl.isEmpty()) "测试中..." else "测试下载服务器连接", fontSize = 16.sp) }
            Spacer(Modifier.height(10.dp))

            // 按钮2: 测试公网连接
            Button({
                testing = true; result = "正在连接公网..."
                Logger.i("Net", "测试公网: bilibili.com")
                pingServer("https://www.bilibili.com") { ok, msg ->
                    testing = false; result = if (ok) "公网连通: $msg" else "公网不通: $msg"
                    Logger.i("Net", result)
                }
            }, enabled = !testing, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3377AA), contentColor = Color.White)
            ) { Text("测试公网连接 (B站)", fontSize = 16.sp) }
            Spacer(Modifier.height(16.dp))

            HorizontalDivider(color = Color(0xFF3A3A3A))
            Spacer(Modifier.height(12.dp))

            // 自定义网址输入框
            Text("自定义测试网址", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(testUrl, { testUrl = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入网址如 http://example.com", color = Color.Gray, fontSize = 13.sp) },
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA0C4FF), unfocusedBorderColor = Color(0xFF3A3A3A), cursorColor = Color(0xFFA0C4FF)))
            Spacer(Modifier.height(10.dp))

            // 按钮3: 测试自定义网址
            Button({
                if (testUrl.isBlank()) { result = "请输入网址"; return@Button }
                testing = true; result = "正在连接 $testUrl ..."
                Logger.i("Net", "测试自定义: $testUrl")
                pingServer(testUrl) { ok, msg ->
                    testing = false; result = if (ok) "连通: $msg" else "不通: $msg"
                    Logger.i("Net", result)
                }
            }, enabled = !testing, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55AA55), contentColor = Color.White)
            ) { Text(if (testing && testUrl.isNotEmpty()) "测试中..." else "测试自定义网址", fontSize = 16.sp) }
            Spacer(Modifier.height(16.dp))

            // 结果显示
            if (result.isNotEmpty()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))) {
                    Text(result, Modifier.padding(12.dp),
                        color = if (result.startsWith("服务器连通") || result.startsWith("公网连通") || result.startsWith("连通")) Color(0xFF80FF80)
                                else if (result.contains("不通") || result.contains("失败")) Color(0xFFFF8080)
                                else Color.White,
                        fontSize = 14.sp)
                }
            }
        }
    }
}
