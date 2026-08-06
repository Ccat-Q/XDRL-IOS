import XCTest

/// XDYL iOS 全界面 UI 测试：主界面 / 设置 / 扩展 / 网络调试 / 开始下载
/// Compose Multiplatform 1.6.0 将语义树映射到 iOS 可访问性树，XCUITest 可按文本查询控件。
/// CI 中通过 xcodebuild test -resultBundlePath 产生 .xcresult，内含自动录屏/截图附件。
final class XDRLUITests: XCTestCase {

    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        // 等待 Compose 首帧渲染（模拟器首次启动较慢），跨元素类型查询以提高健壮性
        XCTAssertNotNil(waitForAny("Nebula updater-NU", timeout: 60), "主界面标题未出现")
        dismissUpdateDialogIfPresent()
    }

    override func tearDownWithError() throws {
        app.terminate()
        app = nil
    }

    // MARK: - 工具方法

    /// 关闭可能弹出的「发现新版本」更新弹窗（由启动时的网络检查触发，服务端可达时才会出现）
    private func dismissUpdateDialogIfPresent() {
        let skip = app.buttons["暂不"]
        if skip.waitForExistence(timeout: 8) {
            skip.tap()
        }
    }

    /// 截图并附加到测试结果（通过 xcresulttool export attachments 导出）
    private func attachScreenshot(_ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    /// 在常见元素类型中查找指定文本的控件。
    /// Compose 语义节点映射到 iOS 后的元素类型不完全固定，因此逐类尝试。
    private func element(_ label: String, timeout: TimeInterval = 10) -> XCUIElement? {
        let queries: [XCUIElementQuery] = [
            app.buttons, app.staticTexts, app.otherElements, app.switches, app.textFields
        ]
        let perQuery = min(timeout, 3.0)
        for query in queries {
            let el = query[label]
            if el.waitForExistence(timeout: perQuery) { return el }
        }
        return nil
    }

    /// 在总超时预算内轮询所有元素类型，直到找到目标文本（用于首帧等待等长超时场景）
    private func waitForAny(_ label: String, timeout: TimeInterval) -> XCUIElement? {
        let queries: [XCUIElementQuery] = [
            app.buttons, app.staticTexts, app.otherElements, app.switches, app.textFields
        ]
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            for query in queries {
                let el = query[label]
                if el.exists { return el }
            }
            RunLoop.current.run(until: Date().addingTimeInterval(0.5))
        }
        return nil
    }

    private func tap(_ label: String, file: StaticString = #filePath, line: UInt = #line) {
        let el = element(label)
        XCTAssertNotNil(el, "找不到元素: \(label)", file: file, line: line)
        el?.tap()
    }

    private func assertExists(_ label: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertNotNil(element(label), "找不到元素: \(label)", file: file, line: line)
    }

    // MARK: - 用例

    /// 主界面：标题、副标题、设置入口、下载按钮
    func testMainScreenElements() throws {
        assertExists("Nebula updater-NU")
        assertExists("星云更新器-IOS")
        assertExists("设置")              // 右上角齿轮
        assertExists("开始下载 (CSV)")     // 默认 CSV 模式
        attachScreenshot("01-main-screen")
    }

    /// 设置页：各配置项 + 「关于」「ERROR 错误代码」弹窗 + 返回
    func testSettingsScreenAndDialogs() throws {
        tap("设置")
        assertExists("版本文件夹名称")
        assertExists("下载线程数 (20-128)")
        assertExists("下载服务器地址")
        assertExists("更新后自动清理多余文件")
        assertExists("清除日志")
        assertExists("关于")
        assertExists("ERROR 错误代码")
        assertExists("手动更新 CSV")
        assertExists("网络调试")
        attachScreenshot("02-settings-screen")

        // 关于弹窗
        tap("关于")
        assertExists("本软件为开源项目")
        attachScreenshot("03-about-dialog")
        tap("确定")

        // ERROR 错误代码弹窗
        tap("ERROR 错误代码")
        assertExists("关闭")
        attachScreenshot("04-error-dialog")
        tap("关闭")

        // 返回主界面
        tap("返回")
        assertExists("开始下载 (CSV)")
    }

    /// 扩展页：各开关与功能项；打开开发者模式验证主界面日志区后恢复
    func testExtensionScreen() throws {
        tap("设置")
        tap("扩展")
        assertExists("扩展功能")
        assertExists("日志操作")
        assertExists("模组白名单 (孤儿清理豁免)")
        assertExists("使用 JSON Manifest")
        assertExists("开发者模式")
        assertExists("解锁线程限制 (最大1024)")
        assertExists("使用本地 CSV 文件列表")
        assertExists("重置所有设置")
        attachScreenshot("05-extension-screen")

        // 打开「开发者模式」→ 主界面应出现日志区 → 回到扩展页关闭，恢复初始状态
        let devSwitch = app.switches.element(boundBy: 1) // 0=JSON Manifest, 1=开发者模式
        if devSwitch.waitForExistence(timeout: 8) {
            devSwitch.tap()
            tap("返回")
            tap("返回")
            let logText = app.descendants(matching: .any)
                .matching(NSPredicate(format: "label CONTAINS %@", "Nebula Updater iOS")).firstMatch
            XCTAssertTrue(logText.waitForExistence(timeout: 10), "开发者模式下主界面未显示日志")
            attachScreenshot("06-main-devmode")
            tap("设置")
            tap("扩展")
            devSwitch.tap()
        }
        tap("返回")
        tap("返回")
        assertExists("开始下载 (CSV)")
    }

    /// 网络调试页：三个测试入口 + 公网连通性实测
    func testNetworkDebugScreen() throws {
        tap("设置")
        tap("网络调试")
        assertExists("测试下载服务器连接")
        assertExists("测试公网连接 (B站)")
        assertExists("自定义测试网址")
        assertExists("测试自定义网址")
        attachScreenshot("07-network-screen")

        // 公网测试：无论连通与否都会显示以「公网」开头的结果文案
        tap("测试公网连接 (B站)")
        let result = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "公网")).firstMatch
        XCTAssertTrue(result.waitForExistence(timeout: 60), "公网测试未返回结果")
        attachScreenshot("08-network-result")

        tap("返回")
        tap("返回")
        assertExists("开始下载 (CSV)")
    }

    /// 开始下载：点击后同步进入「下载中...」；等待下载过程供录屏记录（不强制断言下载结果，依赖外网服务器）
    func testStartDownloadFlow() throws {
        dismissUpdateDialogIfPresent()
        tap("开始下载 (CSV)")
        // 点击后同步进入「下载中...」状态
        XCTAssertNotNil(waitForAny("下载中...", timeout: 10), "点击开始下载后未进入下载中状态")
        attachScreenshot("09-downloading")
        sleep(20)
        attachScreenshot("10-download-progress")
        // App 仍然存活即可
        XCTAssertNotNil(waitForAny("Nebula updater-NU", timeout: 5))
    }
}
