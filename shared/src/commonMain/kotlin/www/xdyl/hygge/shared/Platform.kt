package www.xdyl.hygge.shared

expect fun getLauncherRoot(): String
expect fun getMinecraftDir(start: String): String?
expect fun installResourcePack(prefs: Preferences)
expect fun getDocumentsDir(): String
expect fun writeToDocuments(filename: String, content: String): Boolean
expect fun readFromDocuments(filename: String): String?
expect fun listDocumentsDir(): List<String>
expect fun downloadFile(url: String, destPath: String, onProgress: (Float) -> Unit, onComplete: (Boolean, String) -> Unit)
expect fun openUrl(url: String)
expect fun fetchManifest(url: String, onComplete: (Boolean, String) -> Unit)
expect fun fetchWithHeaders(url: String, headers: Map<String, String>, onComplete: (Boolean, String) -> Unit)
expect fun pingServer(url: String, onResult: (Boolean, String) -> Unit)
expect fun fileSize(path: String): Long?
expect fun readFileBytes(path: String): ByteArray?
expect fun listJarsInDir(dir: String): List<String>
expect fun deleteFile(path: String): Boolean
expect fun currentTimeMillis(): Long
