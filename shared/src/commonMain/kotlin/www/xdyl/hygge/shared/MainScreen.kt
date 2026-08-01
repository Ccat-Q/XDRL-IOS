package www.xdyl.hygge.shared

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

// Download from manifest.json
private data class ModFile(val name: String)

private fun parseManifest(json: String): List<ModFile> {
    val result = mutableListOf<ModFile>()
    val key = ""name""
    var pos = 0
    while (true) {
        val keyIdx = json.indexOf(key, pos)
        if (keyIdx == -1) break
        val colon = json.indexOf(':', keyIdx + key.length)
        val qStart = json.indexOf('"', colon + 1)
        val qEnd = json.indexOf('"', qStart + 1)
        if (qStart == -1 || qEnd == -1) break
        result.add(ModFile(json.substring(qStart + 1, qEnd)))
        pos = qEnd + 1
    }
    return result
}

@Composable
fun MainScreen() {
    var screen by remember { mutableStateOf("main") }
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("") }
    var down by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    val logText by Logger.logs.collectAsState()
    val prefs = remember { Preferences() }
    val csvFiles by remember { derivedStateOf { listDocumentsDir() } }

    var ver by remember { mutableStateOf(prefs.getString("ver", "1.21.1-NeoForge") ?: "1.21.1-NeoForge") }
    var threads by remember { mutableStateOf(prefs.getInt("threads", 20).toString()) }
    var srv by remember { mutableStateOf(prefs.getString("srv", "") ?: "") }
    val effectiveSrv = if (srv.isEmpty()) "http://82.157.155.86:5551/mods/" else srv
    var clean by remember { mutableStateOf(prefs.getBoolean("clean", true)) }
    var unlock by remember { mutableStateOf(prefs.getBoolean("unlock", false)) }
    var localCsv by remember { mutableStateOf(prefs.getBoolean("localcsv", false)) }
    var csvFileName by remember { mutableStateOf("files.csv") }
    var devMode by remember { mutableStateOf(prefs.getBoolean("devmode", false)) }

    LaunchedEffect(Unit) {
        Logger.i("App", "===== Nebula Updater iOS =====")
        writeToDocuments("file_list.csv", SAMPLE_CSV)
        Logger.i("App", "Documents: ${getDocumentsDir()}")
    }

    fun downloadNext(mods: List<ModFile>, i: Int, ok: Int, fail: Int, dir: String) {
        if (i >= mods.size) { down = false; progress = 1f; status = "$ok 成功, $fail 失败"; return }
        val m = mods[i]
        progress = i.toFloat() / mods.size
        status = "[${i + 1}/${mods.size}] ${m.name}"
        Logger.i("DL", "GET ${effectiveSrv.trimEnd('/')}/${m.name}")
        downloadFile("${effectiveSrv.trimEnd('/')}/${m.name}", "$dir/${m.name}", { p -> progress = i.toFloat() / mods.size + p / mods.size }) { good, msg ->
            if (good) { Logger.i("DL", "OK"); downloadNext(mods, i + 1, ok + 1, fail, dir) }
            else { Logger.e("DL", "FAIL: $msg"); downloadNext(mods, i + 1, ok, fail + 1, dir) }
        }
    }

    fun startDownload() {
        if (down) return
        val manifestUrl = "${effectiveSrv.trimEnd('/')}/manifest.json"
        status = "获取文件列表..."
        fetchManifest(manifestUrl) { ok, json ->
            if (!ok || json.isEmpty()) { down = false; status = "无法获取列表"; return@fetchManifest }
        val mods = parseManifest(json)
        if (mods.isEmpty()) { status = "列表为空"; return }
        down = true; progress = 0f
        Logger.i("DL", "===== ${mods.size} files =====")
        val dir = "${getDocumentsDir()}/$ver/mods"
        downloadNext(mods, 0, 0, 0, dir)
    }

    if (showAbout) AlertDialog({ showAbout = false }, title = { Text("关于", color = Color(0xFFA0C4FF)) }, text = { Column { Text("Nebula Updater iOS v1.0", color = Color.White); Spacer(Modifier.height(4.dp)); Text("路径: ${getDocumentsDir()}", color = Color.LightGray, fontSize = 11.sp); Text("CSV: $csvFiles", color = Color.LightGray, fontSize = 11.sp) } }, confirmButton = { TextButton({ showAbout = false }) { Text("确定", color = Color(0xFFA0C4FF)) } }, containerColor = Color(0xFF2A2A2A))

    if (showErrors) AlertDialog({ showErrors = false }, title = { Text("ERROR", color = Color(0xFFA0C4FF)) }, text = { Column { listOf("ERROR01 找不到目录","ERROR02 无权限","ERROR03 网络超时","ERROR05 校验失败","ERROR08 无法获取列表","ERROR10 未知错误").forEach { Text(it, color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(2.dp)) } } }, confirmButton = { TextButton({ showErrors = false }) { Text("关闭", color = Color(0xFFA0C4FF)) } }, containerColor = Color(0xFF2A2A2A))

    Box(Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        when (screen) {
            "main" -> Column(Modifier.fillMaxSize().padding(16.dp).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp); Text("星云更新器-IOS", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
                    IconButton({ screen = "settings" }, Modifier.size(36.dp)) { Icon(Icons.Default.Settings, "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp)) }
                }
                Spacer(Modifier.height(12.dp))
                Button({ startDownload() }, enabled = !down, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)) { Text(if (down) "下载中..." else "开始下载", fontSize = 16.sp) }
                Spacer(Modifier.height(8.dp))
                if (down || progress > 0f) { LinearProgressIndicator({ progress.coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFFA0C4FF), trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f)); if (status.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(status, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) } }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.weight(1f).fillMaxWidth()) { if (devMode) Text(logText.ifEmpty { "通过文件App上传CSV到XDYL文件夹" }, Modifier.verticalScroll(rememberScrollState()).padding(4.dp).fillMaxWidth(), fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton({ Logger.clear() }) { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                    TextButton({ val ok = writeToDocuments("nebula_log.txt", Logger.getRaw()); status = if (ok) "已导出" else "失败" }) { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                }
            }
            "network" -> NetworkView(srv) { screen = "settings" }
            "settings" -> SettingsView(ver, { v -> ver = v; prefs.putString("ver", v) }, threads, { t -> threads = t; prefs.putInt("threads", t.toIntOrNull() ?: 20) }, srv, { s -> srv = s; prefs.putString("srv", s) }, clean, { c -> clean = c; prefs.putBoolean("clean", c) }, { Logger.clear() }, { showAbout = true }, { showErrors = true }, { screen = "extension" }, { screen = "main" }, { screen = "network" })
            "extension" -> ExtensionView(unlock, { u -> unlock = u; prefs.putBoolean("unlock", u) }, localCsv, { l -> localCsv = l; prefs.putBoolean("localcsv", l) }, devMode, { d -> devMode = d; prefs.putBoolean("devmode", d) }, { prefs.clear(); Logger.clear(); ver = "1.21.1-NeoForge"; threads = "20"; srv = ""; clean = true; unlock = false; localCsv = false; devMode = false; Logger.i("App", "已重置") }, { screen = "settings" })
        }
    }
}


