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
    
    
    
    func startCall(_ data: Data) {
        let handle = CXHandle(type: self.getHandleType(data.handleType), value: data.handle)
        let uuid = UUID(uuidString: data.uuid)
        let startCallAction = CXStartCallAction(call: uuid!, handle: handle)
        
        startCallAction.isVideo = data.type > 0
        let callTransaction = CXTransaction()
        callTransaction.addAction(startCallAction)
        //requestCall
        requestCall(callTransaction, action: "startCall")
    }
    
    func endCall(call: Call) {
        let endCallAction = CXEndCallAction(call: call.uuid)
        let callTransaction = CXTransaction()
        callTransaction.addAction(endCallAction)
        //requestCall
        requestCall(callTransaction, action: "endCall")
    }
    
    func endCallAlls() {
        let calls = callController.callObserver.calls
        for call in calls {
            let endCallAction = CXEndCallAction(call: call.uuid)
            let callTransaction = CXTransaction()
            callTransaction.addAction(endCallAction)
            requestCall(callTransaction, action: "endCallAlls")
        }
    }
    
    func activeCalls() -> [[String: String]] {
        let calls = callController.callObserver.calls
        var json = [[String: String]]()
        for call in calls {
            let item: [String: String] = ["id": call.uuid.uuidString]
            json.append(item)
        }
        return json
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
                }else if(action == "endCall" || action == "endCallAlls"){
                    //push notification for End Call
                }
                print("Requested transaction successfully: \(action)")
            }
        }
    }
    
    private func getHandleType(_ handleType: String?) -> CXHandle.HandleType {
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
    
    
    static let callsChangedNotification = Notification.Name("CallsChangedNotification")
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
            strongSelf.postCallNotification()
        }
        callsChangedHandler?()
        postCallNotification()
    }
    
    func removeCall(_ call: Call){
        guard let idx = calls.firstIndex(where: { $0 === call }) else { return }
        calls.remove(at: idx)
        callsChangedHandler?()
        postCallNotification()
    }
    
    func removeAllCalls() {
        calls.removeAll()
        callsChangedHandler?()
        postCallNotification()
    }
    
    private func postCallNotification(){
        NotificationCenter.default.post(name: type(of: self).callsChangedNotification, object: self)
    }
    
    
}
