@file:OptIn(ExperimentalForeignApi::class)
package www.xdyl.hygge.shared

import kotlinx.cinterop.*
import platform.Foundation.*

actual fun getLauncherRoot(): String = NSHomeDirectory()

actual fun getMinecraftDir(start: String): String? {
    val fm = NSFileManager.defaultManager
    val mcDir = (start as NSString).stringByAppendingPathComponent(".minecraft")
    if (fm.fileExistsAtPath(mcDir)) return mcDir
    val alt = (start as NSString).stringByAppendingPathComponent("minecraft")
    return if (fm.fileExistsAtPath(alt)) alt else null
}

actual fun installResourcePack(prefs: Preferences) {}

actual fun getDocumentsDir(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    return (paths.first() as String)
}

actual fun writeToDocuments(filename: String, content: String): Boolean {
    val docsDir = getDocumentsDir()
    val path = (docsDir as NSString).stringByAppendingPathComponent(filename)
    val parent = (path as NSString).stringByDeletingLastPathComponent
    NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)
    val ok = (content as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    val exists = NSFileManager.defaultManager.fileExistsAtPath(path)
    if (ok && exists) { Logger.i("FileIO", "写入: $path"); return true }
    Logger.e("FileIO", "写入失败 path=$path")
    return false
}

actual fun readFromDocuments(filename: String): String? {
    val path = (getDocumentsDir() as NSString).stringByAppendingPathComponent(filename)
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
        Logger.w("FileIO", "文件不存在: $path")
        return null
    }
    return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as? String
}

actual fun listDocumentsDir(): List<String> {
    val fm = NSFileManager.defaultManager
    val contents = fm.contentsOfDirectoryAtPath(getDocumentsDir(), error = null) ?: return emptyList()
    return (contents as List<String>).filter { it.endsWith(".csv", ignoreCase = true) }
}

actual fun downloadFile(url: String, destPath: String, onProgress: (Float) -> Unit, onComplete: (Boolean, String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) { onComplete(false, "无效URL"); return }
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    val session = NSURLSession.sessionWithConfiguration(NSURLSessionConfiguration.defaultSessionConfiguration())
    val task = session.dataTaskWithRequest(request) { data, response, error ->
        if (error != null) { onComplete(false, error.localizedDescription); return@dataTaskWithRequest }
        val httpResponse = response as? NSHTTPURLResponse
        if (httpResponse == null || httpResponse.statusCode !in 200L..299L) { onComplete(false, "HTTP ${httpResponse?.statusCode}"); return@dataTaskWithRequest }
        val body = data ?: run { onComplete(false, "空响应"); return@dataTaskWithRequest }
        val parent = (destPath as NSString).stringByDeletingLastPathComponent
        NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)
        val ok = body.writeToFile(destPath, atomically = true)
        val exists = NSFileManager.defaultManager.fileExistsAtPath(destPath)
        if (ok && exists) onComplete(true, destPath)
        else onComplete(false, "写入失败")
    }
    task.resume()
}

actual fun pingServer(url: String, onResult: (Boolean, String) -> Unit) {
    val start = NSDate.timeIntervalSinceReferenceDate
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) { onResult(false, "无效URL"); return }
    val req = NSMutableURLRequest.requestWithURL(nsUrl)
    req.setValue("HEAD", forHTTPHeaderField = "")
    val session = NSURLSession.sessionWithConfiguration(NSURLSessionConfiguration.defaultSessionConfiguration())
    session.dataTaskWithRequest(req) { _, response, error ->
        val elapsed = ((NSDate.timeIntervalSinceReferenceDate - start) * 1000).toInt()
        if (error != null) onResult(false, "失败: ${error.localizedDescription}")
        else { val hr = response as? NSHTTPURLResponse; onResult(true, "HTTP ${hr?.statusCode ?: 0} (${elapsed}ms)") }
    }.resume()
}

actual class Preferences {
    private val ud = NSUserDefaults.standardUserDefaults
    actual fun getString(key: String, default: String?): String? = ud.stringForKey(key) ?: default
    actual fun getBoolean(key: String, default: Boolean): Boolean = if (ud.objectForKey(key) != null) ud.boolForKey(key) else default
    actual fun getInt(key: String, default: Int): Int = if (ud.objectForKey(key) != null) ud.integerForKey(key).toInt() else default
    actual fun putString(key: String, value: String) { ud.setObject(value, forKey = key); ud.synchronize() }
    actual fun putBoolean(key: String, value: Boolean) { ud.setBool(value, forKey = key); ud.synchronize() }
    actual fun putInt(key: String, value: Int) { ud.setInteger(value.toLong(), forKey = key); ud.synchronize() }
    actual fun clear() { val d = NSBundle.mainBundle.bundleIdentifier ?: ""; ud.removePersistentDomainForName(d); ud.synchronize() }
}


