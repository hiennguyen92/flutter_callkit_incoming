import 'dart:async';

import 'package:flutter/services.dart';

import 'entities/entities.dart';

/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      const EventChannel('flutter_callkit_incoming_events');

  /// Listen to event callback from [FlutterCallkitIncoming].
  ///
  /// FlutterCallkitIncoming.onEvent.listen((event) {
  /// Event.ACTION_CALL_INCOMING - Received an incoming call
  /// Event.ACTION_CALL_START - Started an outgoing call
  /// Event.ACTION_CALL_ACCEPT - Accepted an incoming call
  /// Event.ACTION_CALL_DECLINE - Declined an incoming call
  /// Event.ACTION_CALL_ENDED - Ended an incoming/outgoing call
  /// Event.ACTION_CALL_TIMEOUT - Missed an incoming call
  /// Event.ACTION_CALL_CALLBACK - only Android (click action `Call back` from missed call notification)
  /// Event.ACTION_CALL_TOGGLE_HOLD - only iOS
  /// Event.ACTION_CALL_TOGGLE_MUTE - only iOS
  /// Event.ACTION_CALL_TOGGLE_DMTF - only iOS
  /// Event.ACTION_CALL_TOGGLE_GROUP - only iOS
  /// Event.ACTION_CALL_TOGGLE_AUDIO_SESSION - only iOS
  /// Event.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP - only iOS
  /// }
  static Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  /// Show Callkit Incoming.
  /// On iOS, using Callkit. On Android, using a custom UI.
  static Future showCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod("showCallkitIncoming", params.toJson());
  }

  /// Show Miss Call Notification.
  /// Only Android
  static Future showMissCallNotification(CallKitParams params) async {
    await _channel.invokeMethod("showMissCallNotification", params.toJson());
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future startCall(CallKitParams params) async {
    await _channel.invokeMethod("startCall", params.toJson());
  }

  /// End an Incoming/Outgoing call.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future endCall(String id) async {
    await _channel.invokeMethod("endCall", {'id': id});
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

  static CallEvent? _receiveCallEvent(dynamic data) {
    Event? event;
    Map<String, dynamic> body = {};

    if (data is Map) {
      event = Event.values.firstWhere((e) => e.name == data['event']);
      body = Map<String, dynamic>.from(data['body']);
      return CallEvent(body, event);
    }
    return null;
  }
}
