import SwiftUI
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeViewWrapper()
                .ignoresSafeArea(.all)
        }
    }
}

struct ComposeViewWrapper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        PlatformKt.MainViewController()
    }
    func updateUIViewController(_ vc: UIViewController, context: Context) {}
}
