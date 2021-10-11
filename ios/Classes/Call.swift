//
//  Call.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 07/10/2021.
//

import Foundation
import AVFoundation

class Call: NSObject {
    
    let uuid: UUID
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
    
    init(uuid: UUID, isOutGoing: Bool = false){
        self.uuid = uuid
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

class Data {
    let uuid: String
    let nameCaller: String
    let appName: String
    let handle: String
    let avatar: String
    let type: Int
    let duration: Int
    let extra: [String: Any?]
    
    //iOS
    let iconName: String
    let handleType: String
    let supportsVideo: Bool
    let maximumCallGroups: Int
    let maximumCallsPerCallGroup: Int
    let supportsDTMF: Bool
    let supportsHolding: Bool
    let supportsGrouping: Bool
    let supportsUngrouping: Bool
    let includesCallsInRecents: Bool
    let ringtonePath: String
    let audioSessionMode: String
    let audioSessionActive: Bool
    let audioSessionPreferredSampleRate: Double
    let audioSessionPreferredIOBufferDuration: Double
    
    
    init(args: [String: Any?]) {
        self.uuid = args["id"] as? String ?? ""
        self.nameCaller = args["nameCaller"] as? String ?? ""
        self.appName = args["appName"] as? String ?? "Callkit"
        self.handle = args["handle"] as? String ?? ""
        self.avatar = args["avatar"] as? String ?? ""
        self.type = args["type"] as? Int ?? 0
        self.duration = args["duration"] as? Int ?? 30000
        self.extra = args["extra"] as? [String: Any?] ?? [:]
        
        self.iconName = args["iconName"] as? String ?? ""
        self.handleType = args["handleType"] as? String ?? ""
        self.supportsVideo = args["supportsVideo"] as? Bool ?? true
        self.maximumCallGroups = args["maximumCallGroups"] as? Int ?? 2
        self.maximumCallsPerCallGroup = args["maximumCallsPerCallGroup"] as? Int ?? 1
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
    
}
