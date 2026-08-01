# Nebula Updater-NU 星云更新器 iOS

> 快速方便下载 Minecraft 模组服务器文件的 iOS 客户端

[![iOS CI](https://github.com/Ccat-Q/XDRL-IOS/actions/workflows/ios.yml/badge.svg)](https://github.com/Ccat-Q/XDRL-IOS/actions)

## ✨ 功能

- 📥 **一键下载** — 自动从服务器下载模组文件到设备
- 📋 **CSV 文件列表** — 支持通过 iOS 文件 App 导入自定义模组列表
- 🔧 **灵活设置** — 自定义下载服务器、线程数、版本文件夹
- 🌐 **网络调试** — 内置 Ping 测试，快速诊断网络问题
- 🔒 **开发者模式** — 日志查看、服务器地址显示等高级功能
- 🧩 **扩展功能** — 解锁线程限制、本地 CSV 等

## 📱 使用

1. 通过 iOS **文件 App** → XDYL 文件夹上传 `file_list.csv`
2. 打开 App，点击「开始下载」
3. 下载完成后在文件 App 中查看文件

## 🛠 技术栈

- **Kotlin Multiplatform** + **Compose Multiplatform**
- **SwiftUI** 入口层
- **NSURLSession** 网络下载
- **GitHub Actions** CI/CD (macOS)

## 👨‍💻 开发者

- iOS 版：**Ccat_Q**
- Android & Windows 版：**UNSA-Studio**

## 📄 开源协议

MIT License
