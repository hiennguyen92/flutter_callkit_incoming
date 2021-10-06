//
//  CallManager.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 07/10/2021.
//

import Foundation
import CallKit

@available(iOS 10.0, *)
class CallManager: NSObject {
    
    private let callController = CXCallController()
    private(set) var calls = [Call]()
    
    
    
    func startCall(handle: String, isVideo: Bool = false) {
        let handle = CXHandle(type: .phoneNumber, value: handle)
        let startCall = CXStartCallAction(call: UUID(), handle: handle)
        
        startCall.isVideo = isVideo
        let callTransaction = CXTransaction()
        callTransaction.addAction(startCall)
        //requestCall
        requestCall(callTransaction, action: "startCall")
    }
    
    func endCall(call: Call) {
        let endCall = CXEndCallAction(call: call.uuid)
        let callTransaction = CXTransaction()
        callTransaction.addAction(endCall)
        //requestCall
        requestCall(callTransaction, action: "endCall")
    }
    
    func setHold(call: Call, onHold: Bool) {
        let handleCall = CXSetHeldCallAction(call: call.uuid, onHold: onHold)
        let callTransaction = CXTransaction()
        callTransaction.addAction(handleCall)
        //requestCall
    }
    
    
    private func requestCall(_ transaction: CXTransaction, action: String) {
        callController.request(transaction){ error in
            if let error = error {
                //fail
                print("Error requesting transaction: \(error)")
            }else {
                if(action == "startCall"){
                    //push notification for Start Call
                }else if(action == "endCall"){
                    //push notification for End Call
                }
                print("Requested transaction successfully: \(action)")
            }
        }
    }
    
    
    var callsChangedHandler: (() -> Void)?
    
    func callWithUUID(uuid: UUID) -> Call?{
        guard let idx = calls.firstIndex(where: { $0.uuid == uuid }) else { return nil }
        return calls[idx]
    }
    
    func addCall(_ call: Call){
        calls.append(call)
        call.stateDidChange = { [weak self] in
            guard let strongSelf = self else { return }
            strongSelf.callsChangedHandler?()
        }
        callsChangedHandler?()
    }
    
    func removeCall(_ call: Call){
        guard let idx = calls.firstIndex(where: { $0 === call }) else { return }
        calls.remove(at: idx)
        callsChangedHandler?()
    }
    
    func removeAllCalls() {
        calls.removeAll()
        callsChangedHandler?()
    }
    
    
    
    
}
