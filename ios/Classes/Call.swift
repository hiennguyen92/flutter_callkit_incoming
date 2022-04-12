//
//  Call.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 07/10/2021.
//

import Foundation
import AVFoundation

public class Call: NSObject {
    
    let uuid: UUID
    let data: Data
    let isOutGoing: Bool
    
    var handle: String?
    
    var stateDidChange: (() -> Void)?
    var hasStartedConnectDidChange: (() -> Void)?
    var hasConnectDidChange: (() -> Void)?
    var hasEndedDidChange: (() -> Void)?
    
    var connectData: Date?{
        didSet{
            stateDidChange?()
            hasStartedConnectDidChange?()
        }
    }
    
    var connectedData: Date?{
        didSet{
            stateDidChange?()
            hasConnectDidChange?()
        }
    }
    
    var endDate: Date?{
        didSet{
            stateDidChange?()
            hasEndedDidChange?()
        }
    }
    
    var isOnHold = false{
        didSet{
            stateDidChange?()
        }
    }
    
    var isMuted = false{
        didSet{
            
        }
    }
    
    var hasStartedConnecting: Bool{
        get{
            return connectData != nil
        }
        set{
            connectData = newValue ? Date() : nil
        }
    }
    
    var hasConnected: Bool {
        get{
            return connectedData != nil
        }
        set{
            connectedData = newValue ? Date() : nil
        }
    }
    
    var hasEnded: Bool {
        get{
            return endDate != nil
        }
        set{
            endDate = newValue ? Date() : nil
        }
    }
    
    var duration: TimeInterval {
        guard let connectDate = connectedData else {
            return 0
        }
        return Date().timeIntervalSince(connectDate)
    }
    
    init(uuid: UUID, data: Data, isOutGoing: Bool = false){
        self.uuid = uuid
        self.data = data
        self.isOutGoing = isOutGoing
    }
    
    var startCallCompletion: ((Bool) -> Void)?
    
    func startCall(withAudioSession audioSession: AVAudioSession ,completion :((_ success : Bool)->Void)?){
        startCallCompletion = completion
        hasStartedConnecting = true
    }
    
    var answCallCompletion :((Bool) -> Void)?
    
    func ansCall(withAudioSession audioSession: AVAudioSession ,completion :((_ success : Bool)->Void)?){
        answCallCompletion = completion
        hasStartedConnecting = true
    }
    
    func endCall(){
        hasEnded = true
    }
    
    func startAudio() {
        
    }
    
    
}

@objc public class Data: NSObject {
    @objc public var uuid: String
    @objc public var nameCaller: String
    @objc public var appName: String
    @objc public var handle: String
    @objc public var avatar: String
    @objc public var type: Int
    @objc public var duration: Int
    @objc public var extra: NSDictionary
    
    //iOS
    @objc public var iconName: String
    @objc public var handleType: String
    @objc public var supportsVideo: Bool
    @objc public var maximumCallGroups: Int
    @objc public var maximumCallsPerCallGroup: Int
    @objc public var supportsDTMF: Bool
    @objc public var supportsHolding: Bool
    @objc public var supportsGrouping: Bool
    @objc public var supportsUngrouping: Bool
    @objc public var includesCallsInRecents: Bool
    @objc public var ringtonePath: String
    @objc public var audioSessionMode: String
    @objc public var audioSessionActive: Bool
    @objc public var audioSessionPreferredSampleRate: Double
    @objc public var audioSessionPreferredIOBufferDuration: Double
    
    @objc public init(id: String, nameCaller: String, handle: String, type: Int) {
        self.uuid = id
        self.nameCaller = nameCaller
        self.appName = "Callkit"
        self.handle = handle
        self.avatar = ""
        self.type = type
        self.duration = 30000
        self.extra = [:]
        self.iconName = "CallKitLogo"
        self.handleType = ""
        self.supportsVideo = true
        self.maximumCallGroups = 2
        self.maximumCallsPerCallGroup = 1
        self.supportsDTMF = true
        self.supportsHolding = true
        self.supportsGrouping = true
        self.supportsUngrouping = true
        self.includesCallsInRecents = true
        self.ringtonePath = ""
        self.audioSessionMode = ""
        self.audioSessionActive = true
        self.audioSessionPreferredSampleRate = 44100.0
        self.audioSessionPreferredIOBufferDuration = 0.005
    }
    
    @objc public convenience init(args: NSDictionary) {
        var argsConvert = [String: Any?]()
        for (key, value) in args {
            argsConvert[key as! String] = value
        }
        self.init(args: argsConvert)
    }
    
    public init(args: [String: Any?]) {
        self.uuid = args["id"] as? String ?? ""
        self.nameCaller = args["nameCaller"] as? String ?? ""
        self.appName = args["appName"] as? String ?? "Callkit"
        self.handle = args["handle"] as? String ?? ""
        self.avatar = args["avatar"] as? String ?? ""
        self.type = args["type"] as? Int ?? 0
        self.duration = args["duration"] as? Int ?? 30000
        self.extra = args["extra"] as? NSDictionary ?? [:]
        
        
        if let ios = args["ios"] as? [String: Any] {
            self.iconName = ios["iconName"] as? String ?? "CallKitLogo"
            self.handleType = ios["handleType"] as? String ?? ""
            self.supportsVideo = ios["supportsVideo"] as? Bool ?? true
            self.maximumCallGroups = ios["maximumCallGroups"] as? Int ?? 2
            self.maximumCallsPerCallGroup = ios["maximumCallsPerCallGroup"] as? Int ?? 1
            self.supportsDTMF = ios["supportsDTMF"] as? Bool ?? true
            self.supportsHolding = ios["supportsHolding"] as? Bool ?? true
            self.supportsGrouping = ios["supportsGrouping"] as? Bool ?? true
            self.supportsUngrouping = ios["supportsUngrouping"] as? Bool ?? true
            self.includesCallsInRecents = ios["includesCallsInRecents"] as? Bool ?? true
            self.ringtonePath = ios["ringtonePath"] as? String ?? ""
            self.audioSessionMode = ios["audioSessionMode"] as? String ?? ""
            self.audioSessionActive = ios["audioSessionActive"] as? Bool ?? true
            self.audioSessionPreferredSampleRate = ios["audioSessionPreferredSampleRate"] as? Double ?? 44100.0
            self.audioSessionPreferredIOBufferDuration = ios["audioSessionPreferredIOBufferDuration"] as? Double ?? 0.005
        }else {
            self.iconName = args["iconName"] as? String ?? "CallKitLogo"
            self.handleType = args["handleType"] as? String ?? ""
            self.supportsVideo = args["supportsVideo"] as? Bool ?? true
            self.maximumCallGroups = args["maximumCallGroups"] as? Int ?? 2
            self.maximumCallsPerCallGroup =  args["maximumCallsPerCallGroup"] as? Int ?? 1
            self.supportsDTMF = args["supportsDTMF"] as? Bool ?? true
            self.supportsHolding = args["supportsHolding"] as? Bool ?? true
            self.supportsGrouping = args["supportsGrouping"] as? Bool ?? true
            self.supportsUngrouping = args["supportsUngrouping"] as? Bool ?? true
            self.includesCallsInRecents = args["includesCallsInRecents"] as? Bool ?? true
            self.ringtonePath = args["ringtonePath"] as? String ?? ""
            self.audioSessionMode = args["audioSessionMode"] as? String ?? ""
            self.audioSessionActive = args["audioSessionActive"] as? Bool ?? true
            self.audioSessionPreferredSampleRate = args["audioSessionPreferredSampleRate"] as? Double ?? 44100.0
            self.audioSessionPreferredIOBufferDuration = args["audioSessionPreferredIOBufferDuration"] as? Double ?? 0.005
        }
    }
    
    func toJSON() -> [String: Any?] {
        let ios = [
            "iconName": iconName,
            "handleType": handleType,
            "supportsVideo": supportsVideo,
            "maximumCallGroups": maximumCallGroups,
            "maximumCallsPerCallGroup": maximumCallsPerCallGroup,
            "supportsDTMF": supportsDTMF,
            "supportsHolding": supportsHolding,
            "supportsGrouping": supportsGrouping,
            "supportsUngrouping": supportsUngrouping,
            "includesCallsInRecents": includesCallsInRecents,
            "ringtonePath": ringtonePath,
            "audioSessionMode": audioSessionMode,
            "audioSessionActive": audioSessionActive,
            "audioSessionPreferredSampleRate": audioSessionPreferredSampleRate,
            "audioSessionPreferredIOBufferDuration": audioSessionPreferredIOBufferDuration
        ] as [String : Any?]
        let map = [
            "uuid": uuid,
            "id": uuid,
            "nameCaller": nameCaller,
            "appName": appName,
            "handle": handle,
            "avatar": avatar,
            "type": type,
            "duration": duration,
            "extra": extra,
            "ios": ios
        ] as [String : Any?]
        return map
    }
    
    func getEncryptHandle() -> String {
        return String(format: "{\"nameCaller\":\"%@\", \"handle\":\"%@\"}", nameCaller, handle).encryptHandle()
    }
    
    
}
