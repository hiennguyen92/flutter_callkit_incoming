import 'package:flutter_callkit_incoming/entities/call_kit.dart';

const ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP =
    'com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP';
const ACTION_CALL_INCOMING =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING';
const ACTION_CALL_START =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_START';
const ACTION_CALL_ACCEPT =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT';
const ACTION_CALL_DECLINE =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE';
const ACTION_CALL_ENDED =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED';
const ACTION_CALL_TIMEOUT =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT';
const ACTION_CALL_CALLBACK =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK';
const ACTION_CALL_TOGGLE_HOLD =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD';
const ACTION_CALL_TOGGLE_MUTE =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE';
const ACTION_CALL_TOGGLE_DMTF =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF';
const ACTION_CALL_TOGGLE_GROUP =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP';
const ACTION_CALL_TOGGLE_AUDIO_SESSION =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION';

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

  factory CallEvent.toggleAudioSession(bool isActivate) =
      CallActionToggleAudioSession;
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
