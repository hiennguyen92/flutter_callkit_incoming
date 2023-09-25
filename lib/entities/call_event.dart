import 'package:flutter_callkit_incoming/entities/call_kit_params.dart';

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
const ACTION_CALL_CUSTOM =
    'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CUSTOM';

/// Object CallEvent.
class CallEvent {
  CallEvent._();

  factory CallEvent.incoming(CallKitParams callkit) = CallActionIncoming;

  factory CallEvent.start(CallKitParams callkit) = CallActionStart;

  factory CallEvent.accept(CallKitParams callkit) = CallActionAccept;

  factory CallEvent.decline(CallKitParams callkit) = CallActionDecline;

  factory CallEvent.ended(CallKitParams callkit) = CallActionEnded;

  factory CallEvent.timeout(CallKitParams callkit) = CallActionTimeout;

  factory CallEvent.callback(CallKitParams callkit) = CallActionCallback;

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

  factory CallEvent.custom() = CallActionCustom;
}

class CallActionIncoming extends CallEvent {
  CallActionIncoming(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.incoming(callkitParams: $callKitParams)';
}

class CallActionStart extends CallEvent {
  CallActionStart(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.start(callkitParams: $callKitParams)';
}

class CallActionAccept extends CallEvent {
  CallActionAccept(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.accept(callkitParams: $callKitParams)';
}

class CallActionDecline extends CallEvent {
  CallActionDecline(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.decline(callkitParams: $callKitParams)';
}

class CallActionEnded extends CallEvent {
  CallActionEnded(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.ended(callkitParams: $callKitParams)';
}

class CallActionTimeout extends CallEvent {
  CallActionTimeout(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.timeout(callkitParams: $callKitParams)';
}

class CallActionCallback extends CallEvent {
  CallActionCallback(this.callKitParams) : super._();

  final CallKitParams callKitParams;

  @override
  String toString() => 'CallEvent.callback(callkitParams: $callKitParams)';
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

class CallActionCustom extends CallEvent {
  CallActionCustom() : super._();

  @override
  String toString() => 'CallEvent.custom()';
}

enum DTMFActionType {
  singleTone,
  softPause,
  hardPause,
}
