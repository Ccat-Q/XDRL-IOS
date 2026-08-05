package www.xdyl.hygge.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val SAMPLE_CSV = """
./Jade-1.21.1-NeoForge-15.10.5.jar,709K,725742,80f9186d25b02ebbfa5773416f5da410,067bb4b007e1d6f6b79f0afe99c91252aa825472b99a76d33a60d24442f9e92d
./ImmediatelyFast-NeoForge-1.6.11+1.21.1.jar,354K,361795,f432a12463accb05290ea7de52fccc43,336df12f099d1a441a3e06850bea86e9c2d0c8bc022d3d9a201870a201562a04
./Applied-Mekanistics-1.6.3.jar,147K,149709,0ef21d62aaa1e318f2f93adabe6c56a2,8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773
./AppliedFlux-1.21-2.1.5-neoforge.jar,338K,345117,aced1a1af01d7411772634aa13826a18,57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b
""".trimIndent()

private val SCREEN_ORDER = listOf("main", "settings", "extension", "network")

@Composable
fun MainScreen() {
    var screen by remember { mutableStateOf("main") }
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("") }
    var down by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var csvDiff by remember { mutableStateOf<VersionDiff?>(null) }
    var updateResult by remember { mutableStateOf<String?>(null) }
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
    var useJson by remember { mutableStateOf(prefs.getBoolean("usejson", false)) }
    var devMode by remember { mutableStateOf(prefs.getBoolean("devmode", false)) }
    var csvFileName by remember { mutableStateOf("files.csv") }
    var whitelist by remember { mutableStateOf(prefs.getStringList("mod_whitelist")) }

    LaunchedEffect(Unit) {
        Logger.i("App", "===== Nebula Updater iOS =====")
        if (readFromDocuments("file_list.csv") == null) writeToDocuments("file_list.csv", SAMPLE_CSV)
        Logger.i("App", "模式: ${if (useJson) "JSON Manifest" else "CSV文件"}")
        checkVersionDifference { ok, diff, _ -> if (ok && diff != null) csvDiff = diff }
    }

    fun runEngine(mods: List<ModFile>, threadCount: Int) {
        val dir = "${getDocumentsDir()}/$ver/mods"
        DownloadEngine(
            threads = threadCount,
            cleanOrphans = clean,
            whitelist = whitelist,
            baseUrl = effectiveSrv,
            destDir = dir,
            onStatus = { status = it },
            onProgress = { progress = it },
            onFinish = { ok, skipped, fail ->
                down = false; progress = 1f
                status = "$ok 成功, $skipped 跳过, $fail 失败"
                Logger.i("DL", "完成: $ok 成功, $skipped 跳过, $fail 失败")
            }
        ).start(mods)
    }

    fun startDownload() {
        if (down) return
        val threadCount = threads.toIntOrNull()?.coerceIn(20, if (unlock) 1024 else 128) ?: 20
        if (useJson) {
            val manifestUrl = "${effectiveSrv.trimEnd('/')}/manifest.json"
            status = "获取JSON列表..."
            Logger.i("DL", "Fetching: $manifestUrl")
            fetchManifest(manifestUrl) { ok, json ->
                if (!ok || json.isEmpty()) { down = false; status = "无法获取manifest"; return@fetchManifest }
                val mods = parseManifest(json)
                if (mods.isEmpty()) { down = false; status = "列表为空"; return@fetchManifest }
                down = true; progress = 0f
                Logger.i("DL", "===== ${mods.size} files (JSON) =====")
                runEngine(mods, threadCount)
            }
        } else {
            val csvContent: String
            if (localCsv && csvFiles.contains(csvFileName)) {
                csvContent = readFromDocuments(csvFileName) ?: run { status = "CSV不存在"; return }
            } else {
                csvContent = readFromDocuments("file_list.csv") ?: run { status = "无CSV"; return }
            }
            val mods = parseCsv(csvContent)
            if (mods.isEmpty()) { status = "列表为空"; return }
            down = true; progress = 0f
            Logger.i("DL", "===== ${mods.size} files (CSV) =====")
            runEngine(mods, threadCount)
        }
    }

    if (showAbout) GlassDialog(onDismiss = { showAbout = false }, title = "关于", confirmText = "确定", onConfirm = { showAbout = false }) { Column { Text("快速方便下载服务器模组", color = Color.LightGray, fontSize = 14.sp); Spacer(Modifier.height(6.dp)); Text("本软件为开源项目", color = Color.LightGray, fontSize = 14.sp); Spacer(Modifier.height(4.dp)); TextButton({ openUrl("https://github.com/Ccat-Q/XDRL-IOS") }) { Text("GitHub: Ccat-Q/XDRL-IOS", color = Color(0xFFA0C4FF), fontSize = 14.sp) }; Spacer(Modifier.height(6.dp)); Text("开发者：Ccat_Q", color = Color.LightGray, fontSize = 14.sp); Text("Android版: UNSA-Studio", color = Color.LightGray, fontSize = 14.sp) } }

    if (showErrors) GlassDialog(onDismiss = { showErrors = false }, title = "ERROR 错误代码", confirmText = "关闭", onConfirm = { showErrors = false }) { Column { Constants.errorDescriptions.forEach { (code, desc) -> Text("$code: $desc", color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(6.dp)) } } }

    if (csvDiff != null) GlassDialog(onDismiss = { csvDiff = null }, title = "发现新版本 v${csvDiff!!.version}", confirmText = "更新", onConfirm = { downloadNewCsv(csvDiff!!.version) { ok, msg -> csvDiff = null; updateResult = if (ok) "CSV 已更新，重启生效" else "更新失败: $msg" } }, dismissText = "暂不") { Column {
        Text("【新增】", color = Color(0xFF80FF80), fontSize = 14.sp)
        if (csvDiff!!.added.isEmpty()) Text("无", color = Color.Gray, fontSize = 13.sp) else csvDiff!!.added.forEach { Text(it, color = Color.White, fontSize = 13.sp) }
        Spacer(Modifier.height(8.dp))
        Text("【移除】", color = Color(0xFFFF8080), fontSize = 14.sp)
        if (csvDiff!!.removed.isEmpty()) Text("无", color = Color.Gray, fontSize = 13.sp) else csvDiff!!.removed.forEach { Text(it, color = Color.White, fontSize = 13.sp) }
        Spacer(Modifier.height(8.dp))
        Text("【更新】", color = Color(0xFFFFD280), fontSize = 14.sp)
        if (csvDiff!!.updated.isEmpty()) Text("无", color = Color.Gray, fontSize = 13.sp) else csvDiff!!.updated.forEach { c -> Text(if (c.oldVersion != null || c.newVersion != null) "${c.name} (${c.oldVersion ?: "?"} → ${c.newVersion ?: "?"})" else c.name, color = Color.White, fontSize = 13.sp) }
    } }

    Box(Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val from = SCREEN_ORDER.indexOf(initialState).coerceAtLeast(0)
                val to = SCREEN_ORDER.indexOf(targetState).coerceAtLeast(0)
                if (to >= from) {
                    (slideInHorizontally { it } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally { -it } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally { it / 3 } + fadeOut(tween(200)))
                }
            },
            label = "screen"
        ) { s ->
            when (s) {
            "main" -> Column(Modifier.fillMaxSize().padding(16.dp).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Nebula updater-NU", color = Color(0xFFA0C4FF), fontSize = 20.sp); Text("星云更新器-IOS", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) }
                    IconButton({ screen = "settings" }, Modifier.size(36.dp)) { Icon(Icons.Default.Settings, "设置", tint = Color(0xFFA0C4FF), modifier = Modifier.size(22.dp)) }
                }
                Spacer(Modifier.height(12.dp))
                val quote = remember { todayQuote() }
                Box(Modifier.fillMaxWidth().liquidGlass().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Column {
                        Text("「${quote.textZh}」", color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${quote.textEn} — ${quote.author}", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button({ startDownload() }, enabled = !down, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0C4FF), contentColor = Color.Black)) { Text(if (down) "下载中..." else "开始下载 (${if (useJson) "JSON" else "CSV"})", fontSize = 16.sp) }
                if (updateResult != null) { Spacer(Modifier.height(4.dp)); Text(updateResult ?: "", color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 12.sp) }
                Spacer(Modifier.height(8.dp))
                if (down || progress > 0f) { LinearProgressIndicator({ progress.coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFFA0C4FF), trackColor = Color(0xFFA0C4FF).copy(alpha = 0.2f)); if (status.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(status, color = Color(0xFFA0C4FF).copy(alpha = 0.8f), fontSize = 13.sp) } }
                Spacer(Modifier.height(8.dp))
                if (devMode) {
                    Box(Modifier.weight(1f).fillMaxWidth()) { Text(logText.ifEmpty { "日志..." }, Modifier.verticalScroll(rememberScrollState()).padding(4.dp).fillMaxWidth(), fontSize = 12.sp, color = Color.LightGray, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton({ Logger.clear() }) { Text("清除日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                        TextButton({ val ok = writeToDocuments("nebula_log.txt", Logger.getRaw()); status = if (ok) "已导出" else "失败" }) { Text("导出日志", color = Color(0xFFA0C4FF), fontSize = 13.sp) }
                    }
                }
            }
            "network" -> NetworkView(srv) { screen = "settings" }
            "settings" -> SettingsView(ver, { v -> ver = v; prefs.putString("ver", v) }, threads, { t -> threads = t; prefs.putInt("threads", t.toIntOrNull() ?: 20) }, srv, { s -> srv = s; prefs.putString("srv", s) }, clean, { c -> clean = c; prefs.putBoolean("clean", c) }, unlock, { Logger.clear() }, { showAbout = true }, { showErrors = true }, { screen = "extension" }, { screen = "main" }, { screen = "network" })
            "extension" -> ExtensionView(unlock, { u -> unlock = u; prefs.putBoolean("unlock", u) }, localCsv, { l -> localCsv = l; prefs.putBoolean("localcsv", l) }, useJson, { j -> useJson = j; prefs.putBoolean("usejson", j) }, devMode, { d -> devMode = d; prefs.putBoolean("devmode", d) }, csvFiles, csvFileName, { f -> csvFileName = f }, whitelist, { w -> whitelist = w; prefs.putStringList("mod_whitelist", w) }, { prefs.clear(); Logger.clear(); ver = "1.21.1-NeoForge"; threads = "20"; srv = ""; clean = true; unlock = false; localCsv = false; useJson = false; devMode = false; Logger.i("App", "已重置") }, { screen = "settings" })
            }
        }
    }
}

@Composable
private fun GlassDialog(
    onDismiss: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize()
                .background(Color(0x66000000))
                .clickable(enabled = true, interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                exit = scaleOut(targetScale = 0.9f, animationSpec = tween(180)) + fadeOut(tween(150)),
                modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.88f)
                        .liquidGlass()
                        .clickable(enabled = true, interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                        .padding(20.dp)
                ) {
                    Column {
                        Text(title, color = Color(0xFFA0C4FF), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { content() }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (dismissText != null) TextButton({ onDismiss() }) { Text(dismissText, color = Color(0xFFA0C4FF)) }
                            if (confirmText != null && onConfirm != null) TextButton({ visible = false; onConfirm() }) { Text(confirmText, color = Color(0xFFA0C4FF)) }
                        }
                    }
                }
            }
        }
    }
}

internal fun Modifier.liquidGlass(): Modifier = this
    .clip(RoundedCornerShape(20.dp))
    .background(
        Brush.verticalGradient(
            listOf(Color(0x66FFFFFF), Color(0x24FFFFFF), Color(0x0EFFFFFF)),
            startY = 0f, endY = 1200f
        ),
        RoundedCornerShape(20.dp)
    )
    .border(
        1.dp,
        Brush.verticalGradient(listOf(Color(0xCCFFFFFF), Color(0x40FFFFFF))),
        RoundedCornerShape(20.dp)
    )
    .drawWithContent {
        drawContent()
        // 顶部高光：内部 1.5dp 白色发光条
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0x99FFFFFF), Color(0x00FFFFFF))),
            topLeft = Offset(size.width * 0.08f, size.height * 0.02f),
            size = Size(size.width * 0.84f, size.height * 0.05f),
            cornerRadius = CornerRadius(20.dp.toPx() * 2, 20.dp.toPx() * 2)
        )
    }