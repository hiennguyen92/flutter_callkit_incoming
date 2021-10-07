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
    let number: String?
    let avatar: String?
    let type: Int
    let duration: Int
    
    //iOS
    let handleType: String?
    let supportsDTMF: Bool
    let supportsHolding: Bool
    let supportsGrouping: Bool
    let supportsUngrouping: Bool
    
    
    init(args: [String: Any?]) {
        self.uuid = args["id"] as? String ?? ""
        self.nameCaller = args["nameCaller"] as? String ?? ""
        self.number = args["number"] as? String ?? ""
        self.avatar = args["avatar"] as? String ?? ""
        self.type = args["type"] as? Int ?? 0
        self.duration = args["duration"] as? Int ?? 30000
        
        self.handleType = args["handleType"] as? String ?? ""
        self.supportsDTMF = args["supportsDTMF"] as? Bool ?? false
        self.supportsHolding = args["supportsHolding"] as? Bool ?? false
        self.supportsGrouping = args["supportsGrouping"] as? Bool ?? false
        self.supportsUngrouping = args["supportsUngrouping"] as? Bool ?? false
    }
    
    func toJSON() -> [String: Any?] {
        return [:]
    }
    
}
