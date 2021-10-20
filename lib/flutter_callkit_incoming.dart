import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      const EventChannel('flutter_callkit_incoming_events');

  static Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  static Future showCallkitIncoming(dynamic params) async {
    await _channel.invokeMethod("showCallkitIncoming", params);
  }

  static Future startCall(dynamic params) async {
    await _channel.invokeMethod("startCall", params);
  }

  static Future endCall(dynamic params) async {
    await _channel.invokeMethod("endCall", params);
  }

  static Future endAllCalls() async {
    await _channel.invokeMethod("endAllCalls");
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

  static const String ACTION_CALL_INCOMING = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING";
  static const String ACTION_CALL_START = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_START";
  static const String ACTION_CALL_ACCEPT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT";
  static const String ACTION_CALL_DECLINE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE";
  static const String ACTION_CALL_ENDED = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED";
  static const String ACTION_CALL_TIMEOUT = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT";
  static const String ACTION_CALL_TOGGLE_HOLD = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_HOLD";
  static const String ACTION_CALL_TOGGLE_MUTE = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_MUTE";
  static const String ACTION_CALL_TOGGLE_DMTF = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_DMTF";
  static const String ACTION_CALL_TOGGLE_GROUP = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_GROUP";
  static const String ACTION_CALL_TOGGLE_AUDIO_SESSION = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TOGGLE_AUDIO_SESSION";

  late String name;
  late dynamic body;

  CallEvent(this.name, this.body);

  @override
  String toString() {
    return "{ event: ${name.toString()}, body: ${body.toString()} }";
  }
}
