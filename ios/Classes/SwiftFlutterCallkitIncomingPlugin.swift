import Flutter
import UIKit
import CallKit
import AVFoundation

@available(iOS 10.0, *)
public class SwiftFlutterCallkitIncomingPlugin: NSObject, FlutterPlugin, CXProviderDelegate {
    
    
    static let ACTION_CALL_INCOMING = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
    static let ACTION_CALL_ACCEPT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
    static let ACTION_CALL_DECLINE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
    static let ACTION_CALL_ENDED = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
    static let ACTION_CALL_TIMEOUT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
    
    
    private var channel: FlutterMethodChannel? = nil
    private var eventChannel: FlutterEventChannel? = nil
    private var callManager: CallManager? = nil
    
    private var eventCallbackHandler: EventCallbackHandler?
    private var sharedProvider: CXProvider? = nil
    
    private var outgoingCall : Call?
    private var answerCall : Call?
    
    private func sendEvent(_ event: String, _ body: [String : Any]) {
        eventCallbackHandler?.send(event, body)
    }
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let instance = SwiftFlutterCallkitIncomingPlugin()
    instance.channel = FlutterMethodChannel(name: "flutter_callkit_incoming", binaryMessenger: registrar.messenger())
    instance.eventChannel = FlutterEventChannel(name: "flutter_incoming_call_events", binaryMessenger: registrar.messenger())
    instance.callManager = CallManager()
    instance.eventCallbackHandler = EventCallbackHandler()
    instance.eventChannel?.setStreamHandler(instance.eventCallbackHandler as? FlutterStreamHandler & NSObjectProtocol)
    registrar.addMethodCallDelegate(instance, channel: instance.channel!)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "showCallkitIncoming":
        guard let args = call.arguments else {
            result("OK")
            return
        }
        if let appArgs = args as? [String: Any] {
            let data = Data(args: appArgs)
            showCallkitIncoming(data, fromPushKit: false)
        }
        result("OK")
        break
    default:
        result(FlutterMethodNotImplemented)
    }
  }
    
    
    func showCallkitIncoming(_ data: Data, fromPushKit: Bool) {
        print("showCallkitIncoming")
        
        self.endCallNotExist(data)
        
        var handle: CXHandle?
        handle = CXHandle(type: self.getHandleType(data.handleType), value: data.number ?? "")
        
        let callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = handle
        callUpdate.supportsDTMF = data.supportsDTMF
        callUpdate.supportsHolding = data.supportsHolding
        callUpdate.supportsGrouping = data.supportsGrouping
        callUpdate.supportsUngrouping = data.supportsUngrouping
        callUpdate.hasVideo = true//data.type > 0 ? true : false
        callUpdate.localizedCallerName = data.nameCaller
        
        initCallkitProvider()
        
        let uuid = UUID(uuidString: data.uuid)
        
        self.sharedProvider?.reportNewIncomingCall(with: uuid!, update: callUpdate) { error in
            print("reportNewIncomingCall \(String(describing: error))")
            if(error == nil) {
                let call = Call(uuid: uuid!)
                call.handle = data.number ?? ""
                self.callManager?.addCall(call)
                self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_INCOMING, [:])
            }
        }
        
        
    }
    
    
    func endCall(_ uuid: String, _ reason: Int) {
        switch reason {
        case 1:
            self.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.failed)
            break
        case 2, 6:
            self.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.remoteEnded)
            break
        case 3:
            self.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.unanswered)
            break
        case 4:
            self.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.answeredElsewhere)
            break
        case 5:
            self.sharedProvider?.reportCall(with: UUID(uuidString: uuid)!, endedAt: Date(), reason: CXCallEndedReason.declinedElsewhere)
            break
        default:
            break
        }
    }
    
    
    func endCallNotExist(_ data: Data) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(data.duration)) {
            let call = self.callManager?.callWithUUID(uuid: UUID(uuidString: data.uuid)!)
            if (call != nil) {
                self.callEndTimeout(data)
            }
        }
    }

    
    
    func callEndTimeout(_ data: Data) {
        self.endCall(data.uuid, 3)
            
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TIMEOUT, [:])
    }
    
    func getHandleType(_ handleType: String?) -> CXHandle.HandleType {
        var typeDefault = CXHandle.HandleType.generic
        switch handleType {
        case "number":
            typeDefault = CXHandle.HandleType.phoneNumber
            break
        case "email":
            typeDefault = CXHandle.HandleType.emailAddress
        default:
            typeDefault = CXHandle.HandleType.generic
        }
        return typeDefault
    }
    
    func initCallkitProvider() {
        if(self.sharedProvider == nil){
            self.sharedProvider = CXProvider(configuration: createConfiguration([:]))
            self.sharedProvider?.setDelegate(self, queue: nil)
        }
    }
    
    func createConfiguration(_ data: [String: Any?]) -> CXProviderConfiguration {
        let configuration = CXProviderConfiguration(localizedName: "Hello XXX")
        configuration.supportsVideo = true
        
        configuration.supportedHandleTypes = [
                    CXHandle.HandleType.generic,
                    CXHandle.HandleType.emailAddress,
                    CXHandle.HandleType.phoneNumber
                ]
        if #available(iOS 11.0, *) {
            configuration.includesCallsInRecents = true
        }
        
        if let image = UIImage(named: "AppIcon40x40") {
            configuration.iconTemplateImageData = image.pngData()
        } else {
            print("Unable to load flutter_incoming_call icon AppIcon40x40.");
        }
        
        
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
    
    
    
    
    
    
    
    
    
    public func providerDidReset(_ provider: CXProvider) {
        print("providerDidReset")
        if(self.callManager == nil){ return }
        for call in self.callManager!.calls{
            call.endCall()
        }
        self.callManager?.removeAllCalls()
    }

    public func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        print("CXStartCallAction")
        let call = Call(uuid: action.callUUID, isOutGoing: true)
        call.handle = action.handle.value
        configurAudioSession()
        call.hasStartedConnectDidChange = { [weak self] in
            self?.sharedProvider?.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectData)
        }
        call.hasConnectDidChange = { [weak self] in
            self?.sharedProvider?.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectedData)
        }
        self.outgoingCall = call;
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        print("CXAnswerCallAction")
        guard let call = self.callManager?.callWithUUID(uuid: action.callUUID) else{
            action.fail()
            return
        }
        configurAudioSession()
        self.answerCall = call
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        print("CXEndCallAction")
        guard let call = self.callManager?.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.endCall()
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ENDED, [:])
        action.fulfill()
        self.callManager?.removeCall(call)
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        print("CXSetHeldCallAction")
        guard let call = self.callManager?.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.isOnHold = action.isOnHold
        call.isMuted = action.isOnHold
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        print("CXSetMutedCallAction")
        guard let call = self.callManager?.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.isMuted = action.isMuted
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        print("Timed out Action")
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TIMEOUT, [:])
    }
    
    public func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        print("Receive \(#function)")
        if(self.answerCall?.hasConnected ?? false){
            senddefaultAudioInterruptionNofificationToStartAudioResource()
            return
        }
        if(self.outgoingCall?.hasConnected ?? false){
            senddefaultAudioInterruptionNofificationToStartAudioResource()
            return
        }
        self.outgoingCall?.startCall(withAudioSession: audioSession) {success in
            if success {
                self.callManager?.addCall(self.outgoingCall!)
                self.outgoingCall?.startAudio()
            }
        }
        self.answerCall?.ansCall(withAudioSession: audioSession) { success in
            if success{
                self.answerCall?.startAudio()
            }
        }
    }
    
    public func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        print("Deactivate \(#function)")
        if self.outgoingCall?.isOnHold ?? false || self.answerCall?.isOnHold ?? false{
            print("Call is on hold")
            return
        }
        self.outgoingCall?.endCall()
        if(self.outgoingCall != nil){
            self.outgoingCall = nil
        }
        self.answerCall?.endCall()
        if(self.answerCall != nil){
            self.answerCall = nil
        }
        self.callManager?.removeAllCalls()
    }
    
    
}

class EventCallbackHandler: FlutterStreamHandler {
    
    private var eventSink: FlutterEventSink?
    
    public func send(_ event: String, _ body: [String: Any]) {
        let data: [String : Any] = [
            "event": event,
            "body": body
        ]
        eventSink?(data)
    }
    
    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        return nil
    }
    
    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.eventSink = nil
        return nil
    }
}
