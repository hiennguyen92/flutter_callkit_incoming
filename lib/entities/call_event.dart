import 'package:flutter_callkit_incoming/entities/android_params.dart';
import 'package:flutter_callkit_incoming/entities/call_kit_params.dart';

/// Event name constants for external reference
abstract class CallEventConstants {
  static const String ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP =
      'com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP';
  static const String ACTION_CALL_INCOMING =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING';
  static const String ACTION_CALL_START =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_START';
  static const String ACTION_CALL_ACCEPT =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT';
  static const String ACTION_CALL_DECLINE =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE';
  static const String ACTION_CALL_ENDED =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED';
  static const String ACTION_CALL_TIMEOUT =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT';
  static const String ACTION_CALL_CONNECTED =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CONNECTED';
  static const String ACTION_CALL_CALLBACK =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK';
  static const String ACTION_CALL_TOGGLE_HOLD =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD';
  static const String ACTION_CALL_TOGGLE_MUTE =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE';
  static const String ACTION_CALL_TOGGLE_DMTF =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF';
  static const String ACTION_CALL_TOGGLE_GROUP =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP';
  static const String ACTION_CALL_TOGGLE_AUDIO_SESSION =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION';
  static const String ACTION_CALL_CUSTOM =
      'com.hiennv.flutter_callkit_incoming.ACTION_CALL_CUSTOM';
}

/// Base sealed class for CallEvent
sealed class CallEvent {
  const CallEvent();

  String get eventName;
}

class CallEventActionDidUpdateDevicePushTokenVoip extends CallEvent {
  const CallEventActionDidUpdateDevicePushTokenVoip();

  @override
  String get eventName =>
      CallEventConstants.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP;

  @override
  String toString() => 'CallEventActionDidUpdateDevicePushTokenVoip()';
}

class CallEventActionCallIncoming extends CallEvent {
  const CallEventActionCallIncoming(this.callKitParams);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_INCOMING;

  final CallKitParams callKitParams;

  @override
  String toString() =>
      'CallEventActionCallIncoming(callKitParams: $callKitParams)';
}

class CallEventActionCallStart extends CallEvent {
  const CallEventActionCallStart(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_START;

  final String id;

  @override
  String toString() => 'CallEventActionCallStart(id: $id)';
}

class CallEventActionCallAccept extends CallEvent {
  const CallEventActionCallAccept(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_ACCEPT;

  final String id;

  @override
  String toString() => 'CallEventActionCallAccept(id: $id)';
}

class CallEventActionCallDecline extends CallEvent {
  const CallEventActionCallDecline(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_DECLINE;

  final String id;

  @override
  String toString() => 'CallEventActionCallDecline(id: $id)';
}

class CallEventActionCallEnded extends CallEvent {
  const CallEventActionCallEnded(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_ENDED;

  final String id;

  @override
  String toString() => 'CallEventActionCallEnded(id: $id)';
}

class CallEventActionCallTimeout extends CallEvent {
  const CallEventActionCallTimeout(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TIMEOUT;

  final String id;

  @override
  String toString() => 'CallEventActionCallTimeout(id: $id)';
}

class CallEventActionCallConnected extends CallEvent {
  const CallEventActionCallConnected(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_CONNECTED;

  final String id;

  @override
  String toString() => 'CallEventActionCallConnected(id: $id)';
}

class CallEventActionCallCallback extends CallEvent {
  const CallEventActionCallCallback(this.id);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_CALLBACK;

  final String id;

  @override
  String toString() => 'CallEventActionCallCallback(id: $id)';
}

class CallEventActionCallToggleHold extends CallEvent {
  const CallEventActionCallToggleHold(this.id, this.isOnHold);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TOGGLE_HOLD;

  final String id;
  final bool isOnHold;

  @override
  String toString() =>
      'CallEventActionCallToggleHold(id: $id, isOnHold: $isOnHold)';
}

class CallEventActionCallToggleMute extends CallEvent {
  const CallEventActionCallToggleMute(this.id, this.isMuted);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TOGGLE_MUTE;

  final String id;
  final bool isMuted;

  @override
  String toString() =>
      'CallEventActionCallToggleMute(id: $id, isMuted: $isMuted)';
}

class CallEventActionCallToggleDmtf extends CallEvent {
  const CallEventActionCallToggleDmtf(this.id, this.digits, this.type);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TOGGLE_DMTF;

  final String id;
  final String digits;
  final DTMFActionType type;

  @override
  String toString() =>
      'CallEventActionCallToggleDmtf(id: $id, digits: $digits, type: $type)';
}

class CallEventActionCallToggleGroup extends CallEvent {
  const CallEventActionCallToggleGroup(this.id, this.callUUIDToGroupWith);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TOGGLE_GROUP;

  final String id;
  final String? callUUIDToGroupWith;

  @override
  String toString() => 'CallEventActionCallToggleGroup(id: $id,'
      ' callUUIDToGroupWith: $callUUIDToGroupWith)';
}

class CallEventActionCallToggleAudioSession extends CallEvent {
  const CallEventActionCallToggleAudioSession(this.isActive);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_TOGGLE_AUDIO_SESSION;

  final bool isActive;

  @override
  String toString() =>
      'CallEventActionCallToggleAudioSession(isActive: $isActive)';
}

class CallEventActionCallCustom extends CallEvent {
  const CallEventActionCallCustom(this.body);

  @override
  String get eventName => CallEventConstants.ACTION_CALL_CUSTOM;

  final Map<String, dynamic> body;

  @override
  String toString() => 'CallEventActionCallCustom(body: $body)';
}
