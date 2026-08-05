package www.xdyl.hygge.shared

/**
 * 远程版本检查：拉取 {baseUrl}version.json（形如 {"version":"2.1","note":"..."}），
 * 与本地已应用版本比较。手写 JSON 解析，不依赖任何 JSON 库。
 *
 * 返回 true 时第二参为 "发现新版本 vX: note"；false 时第二参为空字符串或错误说明。
 * 拉取失败静默返回 false。
 */
fun checkForUpdate(baseUrl: String, currentVersion: String, onResult: (Boolean, String) -> Unit) {
    val url = "${baseUrl.trimEnd('/')}/version.json"
    fetchManifest(url) { ok, body ->
        if (!ok || body.isEmpty()) {
            onResult(false, "")
            return@fetchManifest
        }
        val version = extractJsonString(body, "version")
        if (version.isNullOrEmpty()) {
            onResult(false, "")
            return@fetchManifest
        }
        if (version != currentVersion) {
            val note = extractJsonString(body, "note").orEmpty()
            val suffix = if (note.isNotEmpty()) ": $note" else ""
            onResult(true, "发现新版本 v$version$suffix")
        } else {
            onResult(false, "")
        }
    }
}

/**
 * 手写提取 JSON 中指定键的字符串值。
 * 找到第一个 "key" 后冒号后的首个双引号字符串。找不到或非字符串返回 null。
 */
private fun extractJsonString(json: String, key: String): String? {
    val kq = "\"$key\""
    var pos = 0
    while (true) {
        val keyIdx = json.indexOf(kq, pos)
        if (keyIdx == -1) return null
        val colon = json.indexOf(':', keyIdx + kq.length)
        if (colon == -1) return null
        var i = colon + 1
        while (i < json.length && json[i].isWhitespace()) i++
        if (i < json.length && json[i] == '"') {
            val qEnd = json.indexOf('"', i + 1)
            if (qEnd != -1) return json.substring(i + 1, qEnd)
        }
        pos = keyIdx + kq.length
    }
}
