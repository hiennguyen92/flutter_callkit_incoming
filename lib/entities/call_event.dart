/// Object CallEvent.
class CallEvent {
  Event event;
  dynamic body;

  CallEvent(this.body, this.event);
  @override
  String toString() => 'CallEvent( body: $body, event: $event)';
}

enum Event {
  actionDidUpdateDevicePushTokenVoip,
  actionCallIncoming,
  actionCallStart,
  actionCallAccept,
  actionCallDecline,
  actionCallEnded,
  actionCallTimeout,
  actionCallConnected,
  actionCallCallback,
  actionCallToggleHold,
  actionCallToggleMute,
  actionCallToggleDmtf,
  actionCallToggleGroup,
  actionCallToggleAudioSession,
  actionCallCustom,
  // PATHB-V4: SnowChat Phase 8.2 Path B v4 (2026-05-02) — fires immediately after
  // native plugin's reportNewIncomingCall succeeds. Used by SnowChat CallService
  // to stop audioplayers ringtone (foreground socket-only path) when CallKit
  // takes over. See app/lib/core/call/call_service.dart _onCallKitShown.
  actionPathbV4CallkitShown,
}

/// Using extension for backward compatibility Dart SDK 2.17.0 and lower
extension EventX on Event {
  String get name {
    switch (this) {
      case Event.actionDidUpdateDevicePushTokenVoip:
        return 'com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP';
      case Event.actionCallIncoming:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING';
      case Event.actionCallStart:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_START';
      case Event.actionCallAccept:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT';
      case Event.actionCallDecline:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE';
      case Event.actionCallEnded:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED';
      case Event.actionCallTimeout:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT';
      case Event.actionCallConnected:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CONNECTED';
      case Event.actionCallCallback:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK';
      case Event.actionCallToggleHold:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD';
      case Event.actionCallToggleMute:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE';
      case Event.actionCallToggleDmtf:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF';
      case Event.actionCallToggleGroup:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP';
      case Event.actionCallToggleAudioSession:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION';
      case Event.actionCallCustom:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CUSTOM';
      case Event.actionPathbV4CallkitShown:
        // PATHB-V4: SnowChat-specific — no plugin namespace prefix so other apps
        // using this fork don't accidentally match generic events. Native side
        // emits this exact string via SwiftFlutterCallkitIncomingPlugin.sendEvent.
        return 'PATHB_V4_CALLKIT_SHOWN';
    }
  }
}
