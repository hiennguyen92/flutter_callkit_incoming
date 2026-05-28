//
//  CallkitIncomingAppDelegate.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 05/01/2024.
//  Modified 2026-05-02 by Kennt Kim — Phase 8.2 v2.1 audio-session-callkit-delegation
//   - Added providerDidReset() hook for system reset cleanup (CallKit airplane mode toggle,
//     callservicesd restart). Required by Plan §4.1.3 / AppDelegate side cleanup.
//

import Foundation
import AVFAudio
import CallKit


public protocol CallkitIncomingAppDelegate : NSObjectProtocol {

    func onAccept(_ call: Call, _ action: CXAnswerCallAction);

    func onDecline(_ call: Call, _ action: CXEndCallAction);

    func onEnd(_ call: Call, _ action: CXEndCallAction);

    func onTimeOut(_ call: Call);

    func didActivateAudioSession(_ audioSession: AVAudioSession)

    func didDeactivateAudioSession(_ audioSession: AVAudioSession)

    /// CallKit system reset hook — fires on airplane mode toggle, callservicesd restart, etc.
    /// Implementations must clear any in-flight call state (Dart CallService, alias maps).
    /// Phase 8.2 v2.1 — added to fix dangling state after iOS-initiated provider reset.
    func providerDidReset()

}
