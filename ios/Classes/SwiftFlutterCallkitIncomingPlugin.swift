import Flutter
import UIKit
import CallKit
import NotificationCenter

@available(iOS 10.0, *)
public class SwiftFlutterCallkitIncomingPlugin: NSObject, FlutterPlugin {
    
    
    private var channel: FlutterMethodChannel? = nil
    private var eventChannel: FlutterEventChannel? = nil
    
    
    static var sharedProvider: CXProvider? = nil
    
    
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let instance = SwiftFlutterCallkitIncomingPlugin()
    instance.channel = FlutterMethodChannel(name: "flutter_callkit_incoming", binaryMessenger: registrar.messenger())
    instance.eventChannel = FlutterEventChannel(name: "flutter_incoming_call_events", binaryMessenger: registrar.messenger())
    registrar.addMethodCallDelegate(instance, channel: instance.channel!)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "showCallkitIncoming":
        guard let args = call.arguments else {
            result("OK")
            return
        }

        showCallkitIncoming([:])
        result("OK")
        break
    default:
        result(FlutterMethodNotImplemented)
    }
  }
    
    
    func showCallkitIncoming(_ data: [String: Any?]) {
        print("showCallkitIncoming")
        
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(15000)) {
            //self.callEndTimeout(callData)
        }
        
        
        var handle: CXHandle?
        handle = CXHandle(type: CXHandle.HandleType.generic, value: "Hello ABC")
        
        let callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = handle
        callUpdate.supportsDTMF = false
        callUpdate.supportsHolding = false
        callUpdate.supportsGrouping = false
        callUpdate.supportsUngrouping = false
        callUpdate.hasVideo = false
        callUpdate.localizedCallerName = "Hello Anh Oi"
        
        initCallkitProvider()
        
        SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportNewIncomingCall(with: UUID(), update: callUpdate) { error in
            print(error)
            if(error == nil) {
                
            }
        }
        
        
    }
    
    func initCallkitProvider() {
        if(SwiftFlutterCallkitIncomingPlugin.sharedProvider == nil){
            SwiftFlutterCallkitIncomingPlugin.sharedProvider = CXProvider(configuration: createConfiguration([:]))
        }
    }
    
    func createConfiguration(_ data: [String: Any?]) -> CXProviderConfiguration {
        let configuration = CXProviderConfiguration(localizedName: "Hello XXX")
        
        return configuration
    }
    
    
    
    
    
}
