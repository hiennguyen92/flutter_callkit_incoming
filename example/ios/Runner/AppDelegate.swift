import UIKit
import Flutter
import flutter_callkit_incoming

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
//    DispatchQueue.main.asyncAfter(deadline: .now() + 15.0) {
//        print("Fake from Pushkit")
//
//        var info = [String: Any?]()
//        info["id"] = "44d915e1-5ff4-4bed-bf13-c423048ec97a"
//        info["nameCaller"] = "Hien Nguyen"
//        info["handle"] = "0123456789"
//        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(flutter_callkit_incoming.Data(args: info), fromPushKit: true)
//    }
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    
    override func application(_ application: UIApplication,
                              continue userActivity: NSUserActivity,
                              restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        
        if userActivity.activityType == "INStartVideoCallIntent" {
            print("Open App when click action Callkit")
        }
        return super.application(application, continue: userActivity, restorationHandler: restorationHandler)
    }
    
    
}
