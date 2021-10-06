import Flutter
import UIKit
import CallKit
import AVFoundation

@available(iOS 10.0, *)
public class SwiftFlutterCallkitIncomingPlugin: NSObject, FlutterPlugin {
    
    
    static let ACTION_CALL_INCOMING = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
    static let ACTION_CALL_ACCEPT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
    static let ACTION_CALL_DECLINE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
    static let ACTION_CALL_ENDED = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
    static let ACTION_CALL_TIMEOUT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
    
    
    private var channel: FlutterMethodChannel? = nil
    private var eventChannel: FlutterEventChannel? = nil
    private var callManager: CallManager? = nil
    
    static var sharedProvider: CXProvider? = nil
    
    
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let instance = SwiftFlutterCallkitIncomingPlugin()
    instance.channel = FlutterMethodChannel(name: "flutter_callkit_incoming", binaryMessenger: registrar.messenger())
    instance.eventChannel = FlutterEventChannel(name: "flutter_incoming_call_events", binaryMessenger: registrar.messenger())
    instance.callManager = CallManager()
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
        callUpdate.supportsDTMF = true
        callUpdate.supportsHolding = true
        callUpdate.supportsGrouping = false
        callUpdate.supportsUngrouping = false
        callUpdate.hasVideo = true
        callUpdate.localizedCallerName = "Hello Anh Oi"
        
        initCallkitProvider()
        
        let uuid = UUID()
        
        SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportNewIncomingCall(with: uuid, update: callUpdate) { error in
            print(error)
            if(error == nil) {
                let call = Call(uuid: uuid)
                call.handle = "Hello ABC"
                self.callManager?.addCall(call)
            }
        }
        
        
    }
    
    
    func endCall(_ uuid: String, _ reason: Int) {
        switch reason {
        case 1:
            SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.failed)
            break
        case 2, 6:
            SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.remoteEnded)
            break
        case 3:
            SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.unanswered)
            break
        case 4:
            SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.answeredElsewhere)
            break
        case 5:
            SwiftFlutterCallkitIncomingPlugin.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.declinedElsewhere)
            break
        default:
            break
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
    
    
    
    func senddefaultAudioInterruptionNofificationToStartAudioResource(){
        var userInfo : [AnyHashable : Any] = [:]
        let intrepEndeRaw = AVAudioSession.InterruptionType.ended.rawValue
        userInfo[AVAudioSessionInterruptionTypeKey] = intrepEndeRaw
        NotificationCenter.default.post(name: AVAudioSession.interruptionNotification, object: self, userInfo: userInfo)
    }
    func configurAudioSession(){
        let session = AVAudioSession.sharedInstance()
        do{
            try session.setCategory(AVAudioSession.Category.playAndRecord, mode: .default)
            try session.setActive(true)
            try session.setMode(AVAudioSession.Mode.voiceChat)
            try session.setPreferredSampleRate(44100.0)
            try session.setPreferredIOBufferDuration(0.005)
        }catch{
            print(error)
        }
    }
    
    
    
    
//    func providerDidReset(_ provider: CXProvider) {
//        for call in self.callManager?.calls{
//            call.endCall()
//        }
//        self.callManager?.removeAllCalls()
//    }
//
//    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
//        let call = Call(uuid: action.callUUID, isOutgoing: true)
//        call.handle = action.handle.value
//        configurAudioSession()
//        call.hasStartedConnectDidChange = { [weak self] in
//            self?.provider.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectData)
//        }
//        call.hasConnectDidChange = { [weak self] in
//            self?.provider.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectedData)
//        }
//        self.outgoingCall = call;
//        action.fulfill()
//    }
    
//    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
//        guard let call = callManager.callWithUUID(uuid: action.callUUID) else{
//            action.fail()
//            return
//        }
//        configurAudioSession()
//        self.answerCall = call
//        action.fulfill()
//    }
//    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
//        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
//            action.fail()
//            return
//        }
//        call.endCall()
//        action.fulfill()
//        callManager.removeCall(call)
//    }
//    func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
//        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
//            action.fail()
//            return
//        }
//        call.isOnHild = action.isOnHold
//        call.isMuted = action.isOnHold
//        action.fulfill()
//    }
//    func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
//        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
//            action.fail()
//            return
//        }
//        call.isMuted = action.isMuted
//        action.fulfill()
//    }
//    func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
//        print("Timed out Action")
//    }
//    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
//        print("Receive \(#function)")
//        if(answerCall?.hasConnected ?? false){
//            senddefaultAudioInterruptionNofificationToStartAudioResource()
//            return
//        }
//        if(outgoingCall?.hasConnected ?? false){
//            senddefaultAudioInterruptionNofificationToStartAudioResource()
//            return
//        }
//        outgoingCall?.startCall(withAudioSession: audioSession) {success in
//            if success {
//                self.callManager.addCall(self.outgoingCall!)
//                self.outgoingCall?.startAudio()
//            }
//        }
//        answerCall?.ansCall(withAudioSession: audioSession) { success in
//            if success{
//                self.answerCall?.startAudio()
//            }
//        }
//    }
//    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
//        print("Deactivate \(#function)")
//        if outgoingCall?.isOnHild ?? false || answerCall?.isOnHild ?? false{
//            print("Call is on hold")
//            return
//        }
//        outgoingCall?.endCall()
//        if(outgoingCall != nil){
//            outgoingCall = nil
//        }
//        answerCall?.endCall()
//        if(answerCall != nil){
//            answerCall = nil
//        }
//        callManager.removeAllCalls()
//    }
    
    
    
}
