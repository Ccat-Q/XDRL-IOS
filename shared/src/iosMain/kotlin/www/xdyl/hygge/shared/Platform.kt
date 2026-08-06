package www.xdyl.hygge.shared

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.platform.AccessibilitySyncOptions
import androidx.compose.ui.window.ComposeUIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalComposeApi::class, ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // CMP 1.6 的 iOS 可访问性树默认只在辅助功能服务运行时才同步，
        // XCUITest 不属于系统辅助功能服务，因此 CI UI 测试查不到任何元素。
        // 仅 Debug 构建强制同步，供 XCUITest 查询；Release 保持默认行为。
        if (Platform.isDebugBinary) {
            accessibilitySyncOptions = AccessibilitySyncOptions.Always(debugLogger = null)
        }
    }
) { MainScreen() }
