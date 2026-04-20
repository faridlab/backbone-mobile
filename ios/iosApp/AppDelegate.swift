import UIKit
import SwiftUI
import shared

@main
struct AppDelegate: App {
    @UIApplicationDelegateAdaptor(AppDelegateBridge.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegateBridge: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Initialize Koin DI
        Main_iosKt.initKoinIOS(sentryDsn: "")
        return true
    }

    func application(
        _ application: UIApplication,
        continue userActivity: NSUserActivity,
        restorationHandler: @escaping ([UIUserActivityRestoring]) -> Void
    ) -> Bool {
        // Handle universal links if needed
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        // Handle OAuth deep links
        if url.scheme == "mobileapp" && url.host == "oauth_redirect" {
            // Notify the app about OAuth callback
            NotificationCenter.default.post(
                name: NSNotification.Name("OAuthCallback"),
                object: url
            )
            return true
        }
        return true
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
            .onOpenURL { url in
                // Handle incoming URLs (OAuth callbacks, etc.)
                if url.scheme == "mobileapp" && url.host == "oauth_redirect" {
                    NotificationCenter.default.post(
                        name: NSNotification.Name("OAuthCallback"),
                        object: url
                    )
                }
            }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return Main_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
