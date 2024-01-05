//
//  CallkitIncomingAppDelegate.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 05/01/2024.
//

import Foundation


public protocol CallkitIncomingAppDelegate : NSObjectProtocol {
    
    func onAccept(_ call: Call);
    
    func onDecline(_ call: Call);
    
    func onEnd(_ call: Call);
    
    func onTimeOut(_ call: Call);
    
}
