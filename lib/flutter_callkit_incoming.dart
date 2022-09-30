import 'dart:async';

import 'package:flutter/services.dart';

/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()
///
class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      const EventChannel('flutter_callkit_incoming_events');

  /// Listen to event callback from [FlutterCallkitIncoming].
  ///
  /// FlutterCallkitIncoming.onEvent.listen((event) {
  /// CallEvent.ACTION_CALL_INCOMING - Received an incoming call
  /// CallEvent.ACTION_CALL_START - Started an outgoing call
  /// CallEvent.ACTION_CALL_ACCEPT - Accepted an incoming call
  /// CallEvent.ACTION_CALL_DECLINE - Declined an incoming call
  /// CallEvent.ACTION_CALL_ENDED - Ended an incoming/outgoing call
  /// CallEvent.ACTION_CALL_TIMEOUT - Missed an incoming call
  /// CallEvent.ACTION_CALL_CALLBACK - only Android (click action `Call back` from missed call notification)
  /// CallEvent.ACTION_CALL_TOGGLE_HOLD - only iOS
  /// CallEvent.ACTION_CALL_TOGGLE_MUTE - only iOS
  /// CallEvent.ACTION_CALL_TOGGLE_DMTF - only iOS
  /// CallEvent.ACTION_CALL_TOGGLE_GROUP - only iOS
  /// CallEvent.ACTION_CALL_TOGGLE_AUDIO_SESSION - only iOS
  /// CallEvent.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP - only iOS
  /// }
  static Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  /// Show Callkit Incoming.
  /// On iOS, using Callkit. On Android, using a custom UI.
  static Future showCallkitIncoming(dynamic params) async {
    await _channel.invokeMethod("showCallkitIncoming", params);
  }

  /// Show Miss Call Notification.
  /// Only Android
  static Future showMissCallNotification(dynamic params) async {
    await _channel.invokeMethod("showMissCallNotification", params);
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future startCall(dynamic params) async {
    await _channel.invokeMethod("startCall", params);
  }

  /// End an Incoming/Outgoing call.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future endCall(dynamic params) async {
    await _channel.invokeMethod("endCall", params);
  }

  /// End all calls.
  static Future endAllCalls() async {
    await _channel.invokeMethod("endAllCalls");
  }

  /// Get active calls.
  /// On iOS: return active calls from Callkit.
  /// On Android: only return last call
  static Future<dynamic> activeCalls() async {
    return await _channel.invokeMethod("activeCalls");
  }

  /// Get device push token VoIP.
  /// On iOS: return deviceToken for VoIP.
  /// On Android: return Empty
  static Future getDevicePushTokenVoIP() async {
    return await _channel.invokeMethod("getDevicePushTokenVoIP");
  }

  /// Get latest action
  static Future<CallEvent?> getLatestEvent() async {
    final event = await _channel.invokeMethod("getLatestEvent");
    if (event != null) {
      return Future.value(_receiveCallEvent(event));
    } else {
      return null;
    }
  }

  static CallEvent? _receiveCallEvent(dynamic data) {
    var event = "";
    dynamic body = {};
    if (data is Map) {
      event = data['event'];
      body = Map<String, dynamic>.from(data['body']);
    }
    return CallEvent(event, body);
  }
}

class CallEvent {
  static const String ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP =
      "com.hiennv.flutter_callkit_incoming.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP";

  static const String ACTION_CALL_INCOMING =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING";
  static const String ACTION_CALL_START =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_START";
  static const String ACTION_CALL_ACCEPT =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT";
  static const String ACTION_CALL_DECLINE =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE";
  static const String ACTION_CALL_ENDED =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED";
  static const String ACTION_CALL_TIMEOUT =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT";
  static const String ACTION_CALL_CALLBACK =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK";
  static const String ACTION_CALL_TOGGLE_HOLD =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD";
  static const String ACTION_CALL_TOGGLE_MUTE =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE";
  static const String ACTION_CALL_TOGGLE_DMTF =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF";
  static const String ACTION_CALL_TOGGLE_GROUP =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP";
  static const String ACTION_CALL_TOGGLE_AUDIO_SESSION =
      "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION";

  String name;
  dynamic body;

  CallEvent(this.name, this.body);

  @override
  String toString() {
    return "{ event: ${name.toString()}, body: ${body.toString()} }";
  }
}
