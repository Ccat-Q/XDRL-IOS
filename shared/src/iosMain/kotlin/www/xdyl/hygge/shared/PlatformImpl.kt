@file:OptIn(ExperimentalForeignApi::class)
package www.xdyl.hygge.shared

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.darwin.NSObject

actual fun getLauncherRoot(): String = NSHomeDirectory()

actual fun getMinecraftDir(start: String): String? {
    val mcDir = (start as NSString).stringByAppendingPathComponent(".minecraft")
    if (NSFileManager.defaultManager.fileExistsAtPath(mcDir)) return mcDir
    val alt = (start as NSString).stringByAppendingPathComponent("minecraft")
    if (NSFileManager.defaultManager.fileExistsAtPath(alt)) return alt
    return null
}

actual fun installResourcePack(prefs: Preferences) {
    Logger.i("Platform", "installResourcePack")
}

actual fun getDocumentsDir(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    return (paths.first() as String)
}

actual fun writeToDocuments(filename: String, content: String): Boolean {
    return try {
        val docsDir = getDocumentsDir()
        val filePath = (docsDir as NSString).stringByAppendingPathComponent(filename)
        val dir = (filePath as NSString).stringByDeletingLastPathComponent
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        Logger.i("FileIO", "写入: $filePath")
        true
    } catch (e: Exception) {
        Logger.e("FileIO", "写入失败: ${e.message}")
        false
    }
}

actual fun downloadFile(url: String, destPath: String, onProgress: (Float) -> Unit, onComplete: (Boolean, String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url) ?: run {
        onComplete(false, "无效URL: $url")
        return
    }
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.setValue("bytes=0-", forHTTPHeaderField = "Range")

    val session = NSURLSession.sessionWithConfiguration(NSURLSessionConfiguration.defaultSessionConfiguration())
    val task = session.dataTaskWithRequest(request) { data, response, error ->
        if (error != null) {
            onComplete(false, "下载失败: ${error.localizedDescription}")
            return@dataTaskWithRequest
        }
        val httpResponse = response as? NSHTTPURLResponse
        if (httpResponse == null || httpResponse.statusCode !in 200L..299L) {
            onComplete(false, "HTTP ${httpResponse?.statusCode}")
            return@dataTaskWithRequest
        }
        if (data == null) {
            onComplete(false, "空响应")
            return@dataTaskWithRequest
        }
        try {
            val dir = (destPath as NSString).stringByDeletingLastPathComponent
            NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
            data.writeToFile(destPath, atomically = true)
            onProgress(1.0f)
            onComplete(true, destPath)
        } catch (e: Exception) {
            onComplete(false, "写入失败: ${e.message}")
        }
    }
    task.resume()
}

actual class Preferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, default: String?): String? =
        userDefaults.stringForKey(key) ?: default
    actual fun getBoolean(key: String, default: Boolean): Boolean =
        if (userDefaults.objectForKey(key) != null) userDefaults.boolForKey(key) else default
    actual fun getInt(key: String, default: Int): Int =
        if (userDefaults.objectForKey(key) != null) userDefaults.integerForKey(key).toInt() else default
    actual fun putString(key: String, value: String) { userDefaults.setObject(value, forKey = key) }
    actual fun putBoolean(key: String, value: Boolean) { userDefaults.setBool(value, forKey = key) }
    actual fun putInt(key: String, value: Int) { userDefaults.setInteger(value.toLong(), forKey = key) }
    actual fun clear() {
        val domain = NSBundle.mainBundle.bundleIdentifier ?: ""
        userDefaults.removePersistentDomainForName(domain)
    }
}
