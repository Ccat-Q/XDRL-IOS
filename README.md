# Nebula Updater-NU 星云更新器 iOS

> 快速方便下载 Minecraft 模组服务器文件的 iOS 客户端

## ✨ 功能

- 📥 **双模式下载** — 支持 JSON Manifest 自动获取 + CSV 文件手动导入
- 📋 **CSV 文件列表** — 支持通过 iOS 文件 App 导入自定义模组列表
- 🔧 **灵活设置** — 自定义下载服务器、线程数、版本文件夹
- 🌐 **网络调试** — 内置 Ping 测试，快速诊断网络问题
- 🔒 **开发者模式** — 日志查看、服务器地址显示等高级功能
- 🧩 **扩展功能** — JSON/CSV 切换、解锁线程限制等

## 📱 使用

1. 打开 App，点击「开始下载」
2. 默认使用 JSON Manifest 自动获取文件列表
3. 亦可通过文件 App 上传 `file_list.csv` 使用 CSV 模式
4. 下载完成后在文件 App 中查看文件

## 🛠 技术栈

- **Kotlin Multiplatform** + **Compose Multiplatform**
- **SwiftUI** 入口层
- **NSURLSession** 网络下载
- **GitHub Actions** CI/CD (macOS)

## 👨‍💻 开发者

- iOS 版：**Ccat_Q**
- Android 版：**UNSA-Studio**

## 📄 开源协议

MIT License