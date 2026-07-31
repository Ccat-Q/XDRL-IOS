package www.xdyl.hygge.shared

expect fun getLauncherRoot(): String
expect fun getMinecraftDir(start: String): String?
expect fun installResourcePack(prefs: Preferences)
expect fun getDocumentsDir(): String
expect fun writeToDocuments(filename: String, content: String): Boolean
