import Flutter
import UIKit
import CallKit
import AVFoundation
import UserNotifications

@available(iOS 10.0, *)
public class SwiftFlutterCallkitIncomingPlugin: NSObject, FlutterPlugin, CXProviderDelegate {
    
    static let ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP = "com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP"
    
    static let ACTION_CALL_INCOMING = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
    static let ACTION_CALL_START = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_START"
    static let ACTION_CALL_ACCEPT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
    static let ACTION_CALL_DECLINE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
    static let ACTION_CALL_ENDED = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
    static let ACTION_CALL_TIMEOUT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
    static let ACTION_CALL_CALLBACK = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK"
    static let ACTION_CALL_CUSTOM = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CUSTOM"
    static let ACTION_CALL_CONNECTED = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CONNECTED"
    
    static let ACTION_CALL_TOGGLE_HOLD = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD"
    static let ACTION_CALL_TOGGLE_MUTE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE"
    static let ACTION_CALL_TOGGLE_DMTF = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF"
    static let ACTION_CALL_TOGGLE_GROUP = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP"
    static let ACTION_CALL_TOGGLE_AUDIO_SESSION = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION"
    
    @objc public private(set) static var sharedInstance: SwiftFlutterCallkitIncomingPlugin!
    
    private var streamHandlers: WeakArray<EventCallbackHandler> = WeakArray([])
    
    private var callManager: CallManager
    
    private var sharedProvider: CXProvider? = nil
    
    private var outgoingCall : Call?
    private var answerCall : Call?
    
    private var data: Data?
    private var isFromPushKit: Bool = false
    private var silenceEvents: Bool = false
    private let devicePushTokenVoIP = "DevicePushTokenVoIP"

    // Phase 8.2 v2.1 (2026-05-02) — Cold launch replay state.
    // PushKit may fire provider:didActivate: before the Flutter isolate finishes
    // booting → EventChannel listener missing at the moment of fire → event lost
    // → Dart waits up to 2s on _audioSessionActivatedCompleter then proceeds via
    // v200 swallow fallback. To eliminate that 2s blank window we cache the last
    // activation timestamp here (thread-safe via DispatchQueue.sync) and replay
    // it when Dart first invokes `replayLastActivationIfRecent` (5s window).
    //
    // Why thread-safe: didActivate fires on CallKit's internal queue; replay is
    // invoked from the Flutter platform queue. Without synchronization the read
    // can tear (TimeInterval is 8 bytes on 64-bit, atomic — but Optional wrapping
    // and getter/setter still race). DispatchQueue.sync serializes both.
    private var _lastActivationTimestamp: TimeInterval?
    private let _activationLock = DispatchQueue(label: "com.snowchat.callkit.activation")

    private var lastActivationTimestamp: TimeInterval? {
        get { _activationLock.sync { _lastActivationTimestamp } }
        set { _activationLock.sync { _lastActivationTimestamp = newValue } }
    }

    // Phase 8.2 v2.2 (2026-05-02) — ACTION_CALL_ACCEPT replay cache.
    // CallKit invokes provider:perform:CXAnswerCallAction: while the app is still
    // in the background. Plugin sendEvent → EventCallbackHandler.send → eventSink
    // — but sink may be nil (FlutterEngine paused or CallNotifier hasn't bound
    // ckm.events.listen yet). Result: first accept tap is silently dropped, user
    // forced to tap again after FG transition. Mirrors lastActivationTimestamp.
    private var _lastAcceptData: [String: Any?]?
    private var _lastAcceptTimestamp: TimeInterval?
    private let _acceptLock = DispatchQueue(label: "com.snowchat.callkit.accept")

    private var lastAcceptCache: (data: [String: Any?], ts: TimeInterval)? {
        get {
            _acceptLock.sync {
                guard let data = _lastAcceptData, let ts = _lastAcceptTimestamp else { return nil }
                return (data, ts)
            }
        }
        set {
            _acceptLock.sync {
                _lastAcceptData = newValue?.data
                _lastAcceptTimestamp = newValue?.ts
            }
        }
    }

    private func sendEvent(_ event: String, _ body: [String : Any?]?) {
        if silenceEvents {
            print(event, " silenced")
            return
        } else {
            streamHandlers.reap().forEach { handler in
                handler?.send(event, body ?? [:])
            }
        }
        
    }
    
    @objc public func sendEventCustom(_ event: String, body: NSDictionary?) {
        streamHandlers.reap().forEach { handler in
            handler?.send(event, body ?? [:])
        }
    }
    
    public static func sharePluginWithRegister(with registrar: FlutterPluginRegistrar) {
        if(sharedInstance == nil){
            sharedInstance = SwiftFlutterCallkitIncomingPlugin(messenger: registrar.messenger())
        }
        sharedInstance.shareHandlers(with: registrar)
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        sharePluginWithRegister(with: registrar)
    }
    
    private static func createMethodChannel(messenger: FlutterBinaryMessenger) -> FlutterMethodChannel {
        return FlutterMethodChannel(name: "flutter_callkit_incoming", binaryMessenger: messenger)
    }
    
    private static func createEventChannel(messenger: FlutterBinaryMessenger) -> FlutterEventChannel {
        return FlutterEventChannel(name: "flutter_callkit_incoming_events", binaryMessenger: messenger)
    }
    
    public init(messenger: FlutterBinaryMessenger) {
        callManager = CallManager()
    }
    
    private func shareHandlers(with registrar: FlutterPluginRegistrar) {
        registrar.addMethodCallDelegate(self, channel: Self.createMethodChannel(messenger: registrar.messenger()))
        let eventsHandler = EventCallbackHandler()
        self.streamHandlers.append(eventsHandler)
        Self.createEventChannel(messenger: registrar.messenger()).setStreamHandler(eventsHandler)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "showCallkitIncoming":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if let getArgs = args as? [String: Any] {
                self.data = Data(args: getArgs)
                showCallkitIncoming(self.data!, fromPushKit: false)
            }
            result(true)
            break
        case "showMissCallNotification":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if let getArgs = args as? [String: Any] {
                self.data = Data(args: getArgs)
                self.showMissedCallNotification(data!)
            }
            result(true)
            break
        case "startCall":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if let getArgs = args as? [String: Any] {
                self.data = Data(args: getArgs)
                self.startCall(self.data!, fromPushKit: false)
            }
            result(true)
            break
        case "endCall":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if(self.isFromPushKit){
                self.endCall(self.data!)
            }else{
                if let getArgs = args as? [String: Any] {
                    self.data = Data(args: getArgs)
                    self.endCall(self.data!)
                }
            }
            result(true)
            break
        case "muteCall":
            guard let args = call.arguments as? [String: Any] ,
                  let callId = args["id"] as? String,
                  let isMuted = args["isMuted"] as? Bool else {
                result(true)
                return
            }
            
            self.muteCall(callId, isMuted: isMuted)
            result(true)
            break
        case "isMuted":
            guard let args = call.arguments as? [String: Any] ,
                  let callId = args["id"] as? String else{
                result(false)
                return
            }
            guard let callUUID = UUID(uuidString: callId),
                  let call = self.callManager.callWithUUID(uuid: callUUID) else {
                result(false)
                return
            }
            result(call.isMuted)
            break
        case "holdCall":
            guard let args = call.arguments as? [String: Any] ,
                  let callId = args["id"] as? String,
                  let onHold = args["isOnHold"] as? Bool else {
                result(true)
                return
            }
            self.holdCall(callId, onHold: onHold)
            result(true)
            break
        case "callConnected":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if(self.isFromPushKit){
                self.connectedCall(self.data!)
            }else{
                if let getArgs = args as? [String: Any] {
                    self.data = Data(args: getArgs)
                    self.connectedCall(self.data!)
                }
            }
            result(true)
            break
        case "activeCalls":
            result(self.callManager.activeCalls())
            break;
        case "endAllCalls":
            self.callManager.endCallAlls()
            result(true)
            break
        case "getDevicePushTokenVoIP":
            result(self.getDevicePushTokenVoIP())
            break;
        case "silenceEvents":
            guard let silence = call.arguments as? Bool else {
                result(true)
                return
            }
            
            self.silenceEvents = silence
            result(true)
            break;
        case "requestNotificationPermission":
            guard let args = call.arguments else {
                result(true)
                return
            }
            if let getArgs = args as? [String: Any] {
                self.requestNotificationPermission(getArgs)
            }
            result(true)
            break
         case "requestFullIntentPermission": 
            result(true)
            break
         case "canUseFullScreenIntent": 
            result(true)
            break
        case "hideCallkitIncoming":
            result(true)
            break
        case "endNativeSubsystemOnly":
            result(true)
            break
        case "setAudioRoute":
            result(true)
            break
        case "replayLastActivationIfRecent":
            // Phase 8.2 v2.1 — Dart invokes this on first listener attach to
            // receive any didActivate event that fired before the EventChannel
            // was ready (cold launch via PushKit, isolate boot delay).
            self.replayLastActivationIfRecent()
            result(true)
            break
        case "replayLastAcceptIfRecent":
            // Phase 8.2 v2.2 — Same shape as activation replay, but for the
            // CXAnswerCallAction event. CallKit dispatches the answer action
            // while the app is still BG; sink may be nil → event lost. Cached
            // data + timestamp; Dart replays on listener attach (5s window).
            self.replayLastAcceptIfRecent()
            result(true)
            break
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    @objc public func setDevicePushTokenVoIP(_ deviceToken: String) {
        UserDefaults.standard.set(deviceToken, forKey: devicePushTokenVoIP)
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP, ["deviceTokenVoIP":deviceToken])
    }
    
    @objc public func getDevicePushTokenVoIP() -> String {
        return UserDefaults.standard.string(forKey: devicePushTokenVoIP) ?? ""
    }
    
    @objc public func getAcceptedCall() -> Data? {
        NSLog("Call data ids \(String(describing: data?.uuid)) \(String(describing: answerCall?.uuid.uuidString))")
        if data?.uuid.lowercased() == answerCall?.uuid.uuidString.lowercased() {
            return data
        }
        return nil
    }
    
    @objc public func showCallkitIncoming(_ data: Data, fromPushKit: Bool, onError: ((Error?) -> Void)? = nil) {
        self.isFromPushKit = fromPushKit
        if(fromPushKit){
            self.data = data
        }
        
        if(data.isShowMissedCallNotification){
            CallkitNotificationManager.shared.addNotificationCategory(data.missedNotificationCallbackText)
        }
        
        var handle: CXHandle?
        handle = CXHandle(type: self.getHandleType(data.handleType), value: data.getEncryptHandle())
        
        let callUpdate = CXCallUpdate()

        callUpdate.remoteHandle = handle
        callUpdate.supportsDTMF = data.supportsDTMF
        callUpdate.supportsHolding = data.supportsHolding
        callUpdate.supportsGrouping = data.supportsGrouping
        callUpdate.supportsUngrouping = data.supportsUngrouping
        callUpdate.hasVideo = data.type > 0 ? true : false
        callUpdate.localizedCallerName = data.nameCaller
        
        initCallkitProvider(data)
        
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        guard let uuid = UUID(uuidString: data.uuid) else {
            NSLog("[CallkitIncoming] showIncomingCall(no PushKit): invalid UUID '\(data.uuid)' — ignored")
            return
        }

        self.configureAudioSession()
        self.sharedProvider?.reportNewIncomingCall(with: uuid, update: callUpdate) { error in
            if(error == nil) {
                self.configureAudioSession()
                let call = Call(uuid: uuid, data: data)
                call.handle = data.handle
                self.callManager.addCall(call)
                self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_INCOMING, data.toJSON())
                self.endCallNotExist(data)
            } else {
                onError?(error)
            }
        }
    }

    @objc public func showCallkitIncoming(_ data: Data, fromPushKit: Bool, completion: @escaping () -> Void) {
        self.isFromPushKit = fromPushKit
        if(fromPushKit){
            self.data = data
        }
        
        if(data.isShowMissedCallNotification){
            CallkitNotificationManager.shared.addNotificationCategory(data.missedNotificationCallbackText)
        }
        
        var handle: CXHandle?
        handle = CXHandle(type: self.getHandleType(data.handleType), value: data.getEncryptHandle())
        
        let callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = handle
        callUpdate.supportsDTMF = data.supportsDTMF
        callUpdate.supportsHolding = data.supportsHolding
        callUpdate.supportsGrouping = data.supportsGrouping
        callUpdate.supportsUngrouping = data.supportsUngrouping
        callUpdate.hasVideo = data.type > 0 ? true : false
        callUpdate.localizedCallerName = data.nameCaller
        
        initCallkitProvider(data)
        
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        // PushKit call MUST report within ~5s deadline; on invalid UUID we still call
        // completion() so iOS doesn't penalize the app for the missed deadline.
        guard let uuid = UUID(uuidString: data.uuid) else {
            NSLog("[CallkitIncoming] showCallkitIncoming: invalid UUID '\(data.uuid)' — ignored")
            completion()
            return
        }

        self.sharedProvider?.reportNewIncomingCall(with: uuid, update: callUpdate) { error in
            if(error == nil) {
                self.configureAudioSession()
                let call = Call(uuid: uuid, data: data)
                call.handle = data.handle
                self.callManager.addCall(call)
                self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_INCOMING, data.toJSON())
                self.endCallNotExist(data)
            }
            completion()
        }
    }
    
    
    @objc public func startCall(_ data: Data, fromPushKit: Bool) {
        self.isFromPushKit = fromPushKit
        if(fromPushKit){
            self.data = data
        }
        initCallkitProvider(data)
        self.callManager.startCall(data)
    }
    
    @objc public func muteCall(_ callId: String, isMuted: Bool) {
        guard let callId = UUID(uuidString: callId),
              let call = self.callManager.callWithUUID(uuid: callId) else {
            return
        }
        if call.isMuted == isMuted {
            self.sendMuteEvent(callId.uuidString, isMuted)
        } else {
            self.callManager.muteCall(call: call, isMuted: isMuted)
        }
    }
    
    @objc public func holdCall(_ callId: String, onHold: Bool) {
        guard let callId = UUID(uuidString: callId),
              let call = self.callManager.callWithUUID(uuid: callId) else {
            return
        }
        if call.isOnHold == onHold {
            self.sendMuteEvent(callId.uuidString,  onHold)
        } else {
            self.callManager.holdCall(call: call, onHold: onHold)
        }
    }
    
    @objc public func endCall(_ data: Data) {
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        // PushKit branch reads uuid from self.data (stored during showCallkitIncoming);
        // non-PushKit reads from the incoming data. Either source can be invalid.
        let uuidSourceString: String
        if self.isFromPushKit {
            guard let stored = self.data else {
                NSLog("[CallkitIncoming] endCall: PushKit branch but self.data is nil — ignored")
                return
            }
            uuidSourceString = stored.uuid
            self.isFromPushKit = false
            self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ENDED, data.toJSON())
        } else {
            uuidSourceString = data.uuid
        }
        guard let uuid = UUID(uuidString: uuidSourceString) else {
            NSLog("[CallkitIncoming] endCall: invalid UUID '\(uuidSourceString)' — ignored")
            return
        }
        let call = Call(uuid: uuid, data: data)
        self.callManager.endCall(call: call)
    }

    @objc public func connectedCall(_ data: Data) {
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        let uuidSourceString: String
        if self.isFromPushKit {
            guard let stored = self.data else {
                NSLog("[CallkitIncoming] connectedCall: PushKit branch but self.data is nil — ignored")
                return
            }
            uuidSourceString = stored.uuid
            self.isFromPushKit = false
        } else {
            uuidSourceString = data.uuid
        }
        guard let uuid = UUID(uuidString: uuidSourceString) else {
            NSLog("[CallkitIncoming] connectedCall: invalid UUID '\(uuidSourceString)' — ignored")
            return
        }
        let call = Call(uuid: uuid, data: data)
        self.callManager.connectedCall(call: call)
    }
    
    @objc public func activeCalls() -> [[String: Any]] {
        return self.callManager.activeCalls()
    }
    
    @objc public func endAllCalls() {
        self.isFromPushKit = false
        self.callManager.endCallAlls()
    }
    
    public func saveEndCall(_ uuid: String, _ reason: Int) {
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        // Single guard at top covers all five branches.
        guard let callUuid = UUID(uuidString: uuid) else {
            NSLog("[CallkitIncoming] saveEndCall: invalid UUID '\(uuid)' (reason=\(reason)) — ignored")
            return
        }
        switch reason {
        case 1:
            self.sharedProvider?.reportCall(with: callUuid, endedAt: Date(), reason: CXCallEndedReason.failed)
            break
        case 2, 6:
            self.sharedProvider?.reportCall(with: callUuid, endedAt: Date(), reason: CXCallEndedReason.remoteEnded)
            break
        case 3:
            self.sharedProvider?.reportCall(with: callUuid, endedAt: Date(), reason: CXCallEndedReason.unanswered)
            break
        case 4:
            self.sharedProvider?.reportCall(with: callUuid, endedAt: Date(), reason: CXCallEndedReason.answeredElsewhere)
            break
        case 5:
            self.sharedProvider?.reportCall(with: callUuid, endedAt: Date(), reason: CXCallEndedReason.declinedElsewhere)
            break
        default:
            break
        }
    }
    
    
    func endCallNotExist(_ data: Data) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(data.duration)) {
            // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
            guard let uuid = UUID(uuidString: data.uuid) else {
                NSLog("[CallkitIncoming] endCallNotExist: invalid UUID '\(data.uuid)' — ignored")
                return
            }
            let call = self.callManager.callWithUUID(uuid: uuid)
            if (call != nil && self.answerCall == nil && self.outgoingCall == nil) {
                self.callEndTimeout(data)
            }
        }
    }



    func callEndTimeout(_ data: Data) {
        self.saveEndCall(data.uuid, 3)
        // Guard against malformed UUID — see CallManager.swift:startCall for rationale.
        guard let uuid = UUID(uuidString: data.uuid) else {
            NSLog("[CallkitIncoming] callEndTimeout: invalid UUID '\(data.uuid)' — ignored")
            return
        }
        guard let call = self.callManager.callWithUUID(uuid: uuid) else {
            return
        }
        self.showMissedCallNotification(data)
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TIMEOUT, data.toJSON())
        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.onTimeOut(call)
        }
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
    
    func initCallkitProvider(_ data: Data) {
        if(self.sharedProvider == nil){
            self.sharedProvider = CXProvider(configuration: createConfiguration(data))
            self.sharedProvider?.setDelegate(self, queue: nil)
        } else {
            self.sharedProvider?.configuration = createConfiguration(data)
        }
        self.callManager.setSharedProvider(self.sharedProvider!)
    }
    
    func createConfiguration(_ data: Data) -> CXProviderConfiguration {
        let configuration = CXProviderConfiguration(localizedName: data.appName)
        configuration.supportsVideo = data.supportsVideo
        configuration.maximumCallGroups = data.maximumCallGroups
        configuration.maximumCallsPerCallGroup = data.maximumCallsPerCallGroup
        
        configuration.supportedHandleTypes = [
            CXHandle.HandleType.generic,
            CXHandle.HandleType.emailAddress,
            CXHandle.HandleType.phoneNumber
        ]
        if #available(iOS 11.0, *) {
            configuration.includesCallsInRecents = data.includesCallsInRecents
        }
        if !data.iconName.isEmpty {
            if let image = UIImage(named: data.iconName) {
                configuration.iconTemplateImageData = image.pngData()
            } else {
                print("Unable to load icon \(data.iconName).");
            }
        }
        if !data.ringtonePath.isEmpty || data.ringtonePath != "system_ringtone_default"  {
            configuration.ringtoneSound = data.ringtonePath
        }
        return configuration
    }
    
    func sendDefaultAudioInterruptionNotificationToStartAudioResource(){
        var userInfo : [AnyHashable : Any] = [:]
        let intrepEndeRaw = AVAudioSession.InterruptionType.ended.rawValue
        userInfo[AVAudioSessionInterruptionTypeKey] = intrepEndeRaw
        userInfo[AVAudioSessionInterruptionOptionKey] = AVAudioSession.InterruptionOptions.shouldResume.rawValue
        NotificationCenter.default.post(name: AVAudioSession.interruptionNotification, object: self, userInfo: userInfo)
    }
    
    func configureAudioSession(){
        if data?.configureAudioSession != false {
            let session = AVAudioSession.sharedInstance()
            do{
                try session.setCategory(AVAudioSession.Category.playAndRecord, options: [
                    .allowBluetoothA2DP,
                    .duckOthers,
                    .allowBluetooth,
                ])
                
                try session.setMode(self.getAudioSessionMode(data?.audioSessionMode))
                try session.setActive(data?.audioSessionActive ?? true)
                try session.setPreferredSampleRate(data?.audioSessionPreferredSampleRate ?? 44100.0)
                try session.setPreferredIOBufferDuration(data?.audioSessionPreferredIOBufferDuration ?? 0.005)
            }catch{
                print(error)
            }
        }
    }
    
    // Phase 8.2 v2.1 — Cold launch event replay.
    // didActivate may fire before Dart EventChannel listener is attached (PushKit
    // cold launch). Dart invokes this on first attach; we replay if within 5s.
    @objc public func replayLastActivationIfRecent() {
        guard let ts = lastActivationTimestamp else { return }
        let elapsed = Date().timeIntervalSince1970 - ts
        guard elapsed < 5.0 else {
            NSLog("[FlutterCallkitIncoming] replay skipped (stale: \(elapsed)s ago)")
            return
        }
        NSLog("[FlutterCallkitIncoming] Replaying didActivate event (\(elapsed)s ago)")
        sendEvent(
            "com.hiennv.flutter_callkit_incoming.AUDIO_SESSION_ACTIVATED_REPLAY",
            ["ts": ts, "elapsed": elapsed]
        )
    }

    // Phase 8.2 v2.2 — Cold-BG accept event replay.
    // CXAnswerCallAction is dispatched on the OS's CallKit queue while the app
    // is still background-suspended. sendEvent → eventSink may drop because the
    // FlutterEngine isolate is paused or CallNotifier hasn't bound the listener
    // yet. Cache the accept payload; Dart calls replayLastAcceptIfRecent on
    // first listener attach. The replayed event reuses ACTION_CALL_ACCEPT so
    // CallKitManager._mapAction routes it identically — Dart-side guards (state
    // machine status check) keep the call idempotent if the original sendEvent
    // happened to land too.
    @objc public func replayLastAcceptIfRecent() {
        guard let cache = lastAcceptCache else { return }
        let elapsed = Date().timeIntervalSince1970 - cache.ts
        guard elapsed < 5.0 else {
            NSLog("[FlutterCallkitIncoming] accept replay skipped (stale: \(elapsed)s ago)")
            // Stale entries shouldn't linger across calls — clear so a future
            // accept doesn't get a phantom replay from a previous session.
            lastAcceptCache = nil
            return
        }
        NSLog("[FlutterCallkitIncoming] Replaying actionCallAccept (\(elapsed)s ago)")
        sendEvent(
            SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ACCEPT,
            cache.data
        )
        // One-shot replay — clear so subsequent calls to this method are no-ops
        // and the next genuine accept owns the cache slot.
        lastAcceptCache = nil
    }

    func getAudioSessionMode(_ audioSessionMode: String?) -> AVAudioSession.Mode {
        var mode = AVAudioSession.Mode.default
        switch audioSessionMode {
        case "gameChat":
            mode = AVAudioSession.Mode.gameChat
            break
        case "measurement":
            mode = AVAudioSession.Mode.measurement
            break
        case "moviePlayback":
            mode = AVAudioSession.Mode.moviePlayback
            break
        case "spokenAudio":
            mode = AVAudioSession.Mode.spokenAudio
            break
        case "videoChat":
            mode = AVAudioSession.Mode.videoChat
            break
        case "videoRecording":
            mode = AVAudioSession.Mode.videoRecording
            break
        case "voiceChat":
            mode = AVAudioSession.Mode.voiceChat
            break
        case "voicePrompt":
            if #available(iOS 12.0, *) {
                mode = AVAudioSession.Mode.voicePrompt
            } else {
                // Fallback on earlier versions
            }
            break
        default:
            mode = AVAudioSession.Mode.default
        }
        return mode
    }
    
    public func providerDidReset(_ provider: CXProvider) {
        NSLog("[FlutterCallkitIncoming] providerDidReset (system reset — airplane mode toggle, callservicesd restart)")
        for call in self.callManager.calls {
            call.endCall()
        }
        self.callManager.removeAllCalls()

        // Phase 8.2 v2.1 — Notify Dart so CallService can _cleanup dangling state.
        sendEvent("com.hiennv.flutter_callkit_incoming.PROVIDER_DID_RESET", [:])
        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.providerDidReset()
        }

        // Clear cold-launch replay cache — the prior activation is no longer valid.
        self.lastActivationTimestamp = nil
    }
    
    public func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        let call = Call(uuid: action.callUUID, data: self.data!, isOutGoing: true)
        call.handle = action.handle.value
        configureAudioSession()
        call.hasStartedConnectDidChange = { [weak self] in
            self?.sharedProvider?.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectData)
        }
        call.hasConnectDidChange = { [weak self] in
            self?.sharedProvider?.reportOutgoingCall(with: call.uuid, connectedAt: call.connectedData)
        }
        self.outgoingCall = call;
        self.callManager.addCall(call)
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_START, self.data?.toJSON())
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        guard let call = self.callManager.callWithUUID(uuid: action.callUUID) else{
            action.fail()
            return
        }

        // Phase 8.2 v2.1 surgery — Apple WWDC 2018 707 권장 패턴.
        //
        // 변경 전 (race source):
        //   self.configureAudioSession()                                         // ❌ 즉시
        //   DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(1200)) {
        //       self.configureAudioSession()                                      // ❌ 1.2s 후 또
        //   }
        //   → configureAudioSession() 안에서 setActive(true) 호출 → CallKit 이 아직
        //     ClientPriority=PhoneCall 부여 전 → callservicesd Ringtone interrupt 거부
        //     → -12983 "Insufficient priority" → audio activate fail → 첫 accept 무시.
        //
        // 변경 후 (정공법):
        //   - category/mode/sampleRate/IOBuffer 만 setup
        //   - setActive(true) 절대 금지 — CallKit 이 priority=PhoneCall 부여 후 자동 호출
        //   - 그 결과 provider:didActivate: 콜백이 정상 발사되고, Dart 가 그 시점에
        //     WebRTC peer connection 시작
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(
                .playAndRecord,
                mode: .voiceChat,
                options: [.allowBluetooth, .allowBluetoothA2DP, .duckOthers]
            )
            try session.setPreferredSampleRate(48_000)
            try session.setPreferredIOBufferDuration(0.005)
        } catch {
            NSLog("[FlutterCallkitIncoming] performAnswer audio setup error: \(error)")
            // 실패해도 진행 — CallKit 이 default 로 처리. v200 swallow 가 보호.
        }

        call.hasConnectDidChange = { [weak self] in
            self?.sharedProvider?.reportOutgoingCall(with: call.uuid, connectedAt: call.connectedData)
        }
        self.data?.isAccepted = true
        self.answerCall = call
        let acceptPayload = self.data?.toJSON()
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ACCEPT, acceptPayload)

        // Phase 8.2 v2.2 — Cache for replay (5s window). If the FlutterEngine
        // isolate was paused or CallNotifier hadn't bound the listener at the
        // moment of the sendEvent above, the event was silently dropped at
        // EventCallbackHandler.send (eventSink == nil). Dart invokes
        // replayLastAcceptIfRecent right after attaching, recovering the lost
        // tap so the user doesn't have to tap twice.
        if let payload = acceptPayload {
            lastAcceptCache = (data: payload, ts: Date().timeIntervalSince1970)
        }

        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.onAccept(call, action)
        }else {
            action.fulfill()
        }
    }
    
//    private func checkUnlockedAndFulfill(action: CXAnswerCallAction, counter: Int) {
//        if UIApplication.shared.isProtectedDataAvailable {
//            action.fulfill()
//        } else if counter > 180 { // fail if waiting for more then 3 minutes
//            action.fail()
//        } else {
//            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
//                self.checkUnlockedAndFulfill(action: action, counter: counter + 1)
//            }
//        }
//    }
    
    
    public func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        guard let call = self.callManager.callWithUUID(uuid: action.callUUID) else {
            if(self.answerCall == nil && self.outgoingCall == nil){
                sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TIMEOUT, self.data?.toJSON())
            } else {
                sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ENDED, self.data?.toJSON())
            }
            action.fail()
            return
        }
        call.endCall()
        self.callManager.removeCall(call)
        if (self.answerCall == nil && self.outgoingCall == nil) {
            sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_DECLINE, self.data?.toJSON())
            if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
                appDelegate.onDecline(call, action)
            } else {
                action.fulfill()
            }
        }else {
            self.answerCall = nil
            sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_ENDED, call.data.toJSON())
            if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
                appDelegate.onEnd(call, action)
            } else {
                action.fulfill()
            }
        }
    }
    
    
    public func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        guard let call = self.callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.isOnHold = action.isOnHold
        call.isMuted = action.isOnHold
        self.callManager.setHold(call: call, onHold: action.isOnHold)
        sendHoldEvent(action.callUUID.uuidString, action.isOnHold)
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        guard let call = self.callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.isMuted = action.isMuted
        sendMuteEvent(action.callUUID.uuidString, action.isMuted)
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetGroupCallAction) {
        guard (self.callManager.callWithUUID(uuid: action.callUUID)) != nil else {
            action.fail()
            return
        }
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_GROUP, [ "id": action.callUUID.uuidString, "callUUIDToGroupWith" : action.callUUIDToGroupWith?.uuidString])
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXPlayDTMFCallAction) {
        guard (self.callManager.callWithUUID(uuid: action.callUUID)) != nil else {
            action.fail()
            return
        }
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_DMTF, [ "id": action.callUUID.uuidString, "digits": action.digits, "type": action.type.rawValue ])
        action.fulfill()
    }
    
    
    public func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        guard let call = self.callManager.callWithUUID(uuid: action.uuid) else {
            action.fail()
            return
        }
        sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TIMEOUT, self.data?.toJSON())
        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.onTimeOut(call)
        }
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        NSLog("[FlutterCallkitIncoming] didActivate sample_rate=\(audioSession.sampleRate)")

        // Phase 8.2 v2.1 — Cache timestamp for cold-launch replay (5s window).
        // PushKit 가 process launch 한 직후엔 Dart isolate 가 아직 boot 안 끝나서
        // EventChannel listener 가 등록 안 됨 → sendEvent 가 sink 없어 lost.
        // 캐시해뒀다가 Dart 가 첫 attach 시 replayLastActivationIfRecent 호출 → replay.
        self.lastActivationTimestamp = Date().timeIntervalSince1970

        // Phase 8.2 v2.1 — Notify Dart that audio session is now active at PhoneCall
        // priority. CallService awaits this event (via _audioSessionActivatedCompleter)
        // before starting WebRTC peer connection — avoids -12983 priority race in
        // flutter_webrtc native path.
        sendEvent(
            "com.hiennv.flutter_callkit_incoming.AUDIO_SESSION_ACTIVATED",
            [
                "sampleRate": audioSession.sampleRate,
                "ts": lastActivationTimestamp ?? 0,
            ]
        )

        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.didActivateAudioSession(audioSession)
        }

        if(self.answerCall?.hasConnected ?? false){
            sendDefaultAudioInterruptionNotificationToStartAudioResource()
            return
        }
        if(self.outgoingCall?.hasConnected ?? false){
            sendDefaultAudioInterruptionNotificationToStartAudioResource()
            return
        }
        self.outgoingCall?.startCall(withAudioSession: audioSession) {success in
            if success {
                self.callManager.addCall(self.outgoingCall!)
                self.outgoingCall?.startAudio()
            }
        }
        self.answerCall?.ansCall(withAudioSession: audioSession) { success in
            if success{
                self.answerCall?.startAudio()
            }
        }
        sendDefaultAudioInterruptionNotificationToStartAudioResource()

        // Phase 8.2 v2.1 surgery — configureAudioSession() 호출 삭제.
        // 이전엔 didActivate 안에서 또 configureAudioSession() (즉 setActive(true))
        // 호출 → CallKit 이 이미 setActive 한 audio session 을 또 건드려 race 유발.
        // 함수 정의는 showCallkitIncoming / startCall 등에서 여전히 사용하므로 유지.

        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_AUDIO_SESSION, [ "isActivate": true ])
    }
    
    public func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        NSLog("[FlutterCallkitIncoming] didDeactivate")

        // Phase 8.2 v2.1 — Notify Dart so CallService can observe deactivation.
        // Dart treats this as observation-only (cleanup is driven by endCall/decline,
        // not by audio session lifecycle). Used for telemetry / metrics.
        sendEvent("com.hiennv.flutter_callkit_incoming.AUDIO_SESSION_DEACTIVATED", [:])

        if let appDelegate = UIApplication.shared.delegate as? CallkitIncomingAppDelegate {
            appDelegate.didDeactivateAudioSession(audioSession)
        }

        if self.outgoingCall?.isOnHold ?? false || self.answerCall?.isOnHold ?? false{
            print("Call is on hold")
            return
        }

        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_AUDIO_SESSION, [ "isActivate": false ])
    }
    
    private func sendMuteEvent(_ id: String, _ isMuted: Bool) {
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_MUTE, [ "id": id, "isMuted": isMuted ])
    }
    
    private func sendHoldEvent(_ id: String, _ isOnHold: Bool) {
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_TOGGLE_HOLD, [ "id": id, "isOnHold": isOnHold ])
    }
    
    @objc public func sendCallbackEvent(_ data: [String: Any]?) {
        self.sendEvent(SwiftFlutterCallkitIncomingPlugin.ACTION_CALL_CALLBACK, data)
    }
    
    
    private func requestNotificationPermission(_ map: [String: Any]) {
        CallkitNotificationManager.shared.requestNotificationPermission(map)
    }
    
    
    private func showMissedCallNotification(_ data: Data) {
        if(!data.isShowMissedCallNotification){
            return
        }
        
        let content = UNMutableNotificationContent()
        content.title = "\(data.nameCaller)"
        content.body = "\(data.missedNotificationSubtitle)"
        content.sound = UNNotificationSound.default
        content.categoryIdentifier = "MISSED_CALL_CATEGORY"
        content.userInfo = data.toJSON()

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)

        let request = UNNotificationRequest(
            identifier: data.uuid,
            content: content,
            trigger: trigger
        )

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Error scheduling missed call notification: \(error)")
            } else {
                print("Missed call notification scheduled.")
            }
        }
    }
    
}

class EventCallbackHandler: NSObject, FlutterStreamHandler {
    private var eventSink: FlutterEventSink?
    
    public func send(_ event: String, _ body: Any) {
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
