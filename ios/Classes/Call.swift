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

public class Data {
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
    
    
    public init(args: [String: Any?]) {
        self.uuid = args["id"] as? String ?? ""
        self.nameCaller = args["nameCaller"] as? String ?? ""
        self.appName = args["appName"] as? String ?? "Callkit"
        self.handle = args["handle"] as? String ?? ""
        self.avatar = args["avatar"] as? String ?? ""
        self.type = args["type"] as? Int ?? 0
        self.duration = args["duration"] as? Int ?? 30000
        self.extra = args["extra"] as? [String: Any?] ?? [:]
        
        
        if let ios = args["ios"] as? [String: Any] {
            self.iconName = ios["iconName"] as? String ?? ""
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
            self.iconName = ""
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
