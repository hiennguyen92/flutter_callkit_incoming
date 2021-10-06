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
        setUproom()
        hasStartedConnecting = true
    }
    
    var answCallCompletion :((Bool) -> Void)?
    
    func ansCall(withAudioSession audioSession: AVAudioSession ,completion :((_ success : Bool)->Void)?){
        answCallCompletion = completion
        setUproom()
        hasStartedConnecting = true
    }
    
    func endCall(){
        hasEnded = true
    }
    
    func startAudio() {
        
    }
    
    private func setUproom(){
        NotificationCenter.default.post(name: NSNotification.Name("startCallTriger"), object: nil)
    }
    
}
