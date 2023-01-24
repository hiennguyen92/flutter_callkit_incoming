import 'package:flutter_callkit_incoming/entities/call_kit.dart';

/// Object CallEvent.
class CallEvent {
  CallEvent._();

  factory CallEvent.incoming(CallKit callkit) = CallActionIncoming;

  factory CallEvent.start(CallKit callkit) = CallActionStart;

  factory CallEvent.accept(CallKit callkit) = CallActionAccept;

  factory CallEvent.decline(CallKit callkit) = CallActionDecline;

  factory CallEvent.ended(CallKit callkit) = CallActionEnded;

  factory CallEvent.timeout(CallKit callkit) = CallActionTimeout;

  factory CallEvent.callback(CallKit callkit) = CallActionCallback;

  factory CallEvent.updateDevicePushToken(String deviceToken) =
      CallActionUpdateDevicePushToken;

  factory CallEvent.toggleHold(String id, bool isOnHold) = CallActionToggleHold;

  factory CallEvent.toggleMute(String id, bool isMuted) = CallActionToggleMute;

  factory CallEvent.toggleDMTF(String id, String digits, DTMFActionType type) =
      CallActionToggleDMTF;

  factory CallEvent.toggleGroup(String id, String callUUIDToGroupWith) =
      CallActionToggleGroup;

  factory CallEvent.toggleAudioSession(bool isActivate) = CallActionToggleAudioSession;
}

class CallActionIncoming extends CallEvent {
  CallActionIncoming(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.incoming(callkit: $callKit)';
}

class CallActionStart extends CallEvent {
  CallActionStart(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.start(callkit: $callKit)';
}

class CallActionAccept extends CallEvent {
  CallActionAccept(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.accept(callkit: $callKit)';
}

class CallActionDecline extends CallEvent {
  CallActionDecline(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.decline(callkit: $callKit)';
}

class CallActionEnded extends CallEvent {
  CallActionEnded(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.ended(callkit: $callKit)';
}

class CallActionTimeout extends CallEvent {
  CallActionTimeout(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.timeout(callkit: $callKit)';
}

class CallActionCallback extends CallEvent {
  CallActionCallback(this.callKit) : super._();

  final CallKit callKit;

  @override
  String toString() => 'CallEvent.callback(callkit: $callKit)';
}

class CallActionUpdateDevicePushToken extends CallEvent {
  CallActionUpdateDevicePushToken(this.deviceToken) : super._();

  final String deviceToken;

  @override
  String toString() =>
      'CallEvent.updateDevicePushToken(deviceToken: $deviceToken)';
}

class CallActionToggleHold extends CallEvent {
  CallActionToggleHold(
    this.id,
    this.isOnHold,
  ) : super._();

  final String id;
  final bool isOnHold;

  @override
  String toString() => 'CallEvent.toggleHold(id: $id, isOnHold: $isOnHold)';
}

class CallActionToggleMute extends CallEvent {
  CallActionToggleMute(
    this.id,
    this.isMuted,
  ) : super._();

  final String id;
  final bool isMuted;

  @override
  String toString() => 'CallEvent.toggleMute(id: $id, isMuted: $isMuted)';
}

class CallActionToggleDMTF extends CallEvent {
  CallActionToggleDMTF(
    this.id,
    this.digits,
    this.type,
  ) : super._();

  final String id;
  final String digits;
  final DTMFActionType type;

  @override
  String toString() =>
      'CallEvent.toggleDMTF(id: $id, digits: $digits, type: $type)';
}

class CallActionToggleGroup extends CallEvent {
  CallActionToggleGroup(
    this.id,
    this.callUUIDToGroupWith,
  ) : super._();

  final String id;
  final String callUUIDToGroupWith;

  @override
  String toString() =>
      'CallEvent.toggleGroup(id: $id, callUUIDToGroupWith: $callUUIDToGroupWith)';
}

class CallActionToggleAudioSession extends CallEvent {
  CallActionToggleAudioSession(this.isActivate) : super._();

  final bool isActivate;

  @override
  String toString() => 'CallEvent.toggleAudioSession(isActivate: $isActivate)';
}

enum DTMFActionType {
  singleTone,
  softPause,
  hardPause,
}

enum Event {
  ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP,
  ACTION_CALL_INCOMING,
  ACTION_CALL_START,
  ACTION_CALL_ACCEPT,
  ACTION_CALL_DECLINE,
  ACTION_CALL_ENDED,
  ACTION_CALL_TIMEOUT,
  ACTION_CALL_CALLBACK,
  ACTION_CALL_TOGGLE_HOLD,
  ACTION_CALL_TOGGLE_MUTE,
  ACTION_CALL_TOGGLE_DMTF,
  ACTION_CALL_TOGGLE_GROUP,
  ACTION_CALL_TOGGLE_AUDIO_SESSION,
}

/// Using extension for backward compatibility Dart SDK 2.17.0 and lower
extension EventX on Event {
  String get name {
    switch (this) {
      case Event.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP:
        return 'com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP';
      case Event.ACTION_CALL_INCOMING:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING';
      case Event.ACTION_CALL_START:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_START';
      case Event.ACTION_CALL_ACCEPT:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT';
      case Event.ACTION_CALL_DECLINE:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE';
      case Event.ACTION_CALL_ENDED:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED';
      case Event.ACTION_CALL_TIMEOUT:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT';
      case Event.ACTION_CALL_CALLBACK:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK';
      case Event.ACTION_CALL_TOGGLE_HOLD:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD';
      case Event.ACTION_CALL_TOGGLE_MUTE:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE';
      case Event.ACTION_CALL_TOGGLE_DMTF:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF';
      case Event.ACTION_CALL_TOGGLE_GROUP:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP';
      case Event.ACTION_CALL_TOGGLE_AUDIO_SESSION:
        return 'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION';
    }
  }
}
