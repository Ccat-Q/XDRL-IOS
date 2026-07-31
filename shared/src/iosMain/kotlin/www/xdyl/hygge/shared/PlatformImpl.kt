@file:OptIn(ExperimentalForeignApi::class)
package www.xdyl.hygge.shared

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

actual fun getLauncherRoot(): String = NSHomeDirectory()

actual fun getMinecraftDir(start: String): String? {
    val mcDir = (start as NSString).stringByAppendingPathComponent(".minecraft")
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(mcDir)) return mcDir
    val alt = (start as NSString).stringByAppendingPathComponent("minecraft")
    if (fileManager.fileExistsAtPath(alt)) return alt
    return null
}

actual fun installResourcePack(prefs: Preferences) {
    writeToDocuments("resource_pack.txt", "installResourcePack called")
}

actual fun getDocumentsDir(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    return (paths.first() as String)
}

actual fun writeToDocuments(filename: String, content: String): Boolean {
    return try {
        val docsDir = getDocumentsDir()
        val filePath = (docsDir as NSString).stringByAppendingPathComponent(filename)
        (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        Logger.i("FileIO", "写入成功: $filePath")
        true
    } catch (e: Exception) {
        Logger.e("FileIO", "写入失败: ${e.message}")
        false
    }
}

actual class Preferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, default: String?): String? {
        return userDefaults.stringForKey(key) ?: default
    }
    actual fun getBoolean(key: String, default: Boolean): Boolean {
        return if (userDefaults.objectForKey(key) != null) userDefaults.boolForKey(key) else default
    }
    actual fun getInt(key: String, default: Int): Int {
        return if (userDefaults.objectForKey(key) != null) userDefaults.integerForKey(key).toInt() else default
    }
    actual fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
    }
    actual fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, forKey = key)
    }
    actual fun putInt(key: String, value: Int) {
        userDefaults.setInteger(value.toLong(), forKey = key)
    }
    actual fun clear() {
        val domain = NSBundle.mainBundle.bundleIdentifier ?: ""
        userDefaults.removePersistentDomainForName(domain)
    }
}

