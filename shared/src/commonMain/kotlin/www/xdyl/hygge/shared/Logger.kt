package www.xdyl.hygge.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Logger {
    private val _logs = MutableStateFlow("")
    val logs: StateFlow<String> = _logs.asStateFlow()

    fun d(tag: String, msg: String) = log("DEBUG", tag, msg)
    fun i(tag: String, msg: String) = log("INFO", tag, msg)
    fun w(tag: String, msg: String) = log("WARN", tag, msg)
    fun e(tag: String, msg: String) = log("ERROR", tag, msg)

    private fun log(level: String, tag: String, msg: String) {
        val entry = "[$level] $tag: $msg"
        _logs.value = if (_logs.value.isEmpty()) entry else _logs.value + "\n" + entry
    }

    fun clear() { _logs.value = "" }

    fun getRaw(): String = _logs.value
}
