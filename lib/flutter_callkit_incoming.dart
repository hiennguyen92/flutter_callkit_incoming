import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      const EventChannel('flutter_callkit_incoming_events');

  static Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

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
  late String event;
  late dynamic body;

  CallEvent(this.event, this.body);

  @override
  String toString() {
    return "{ event: ${event.toString()}, body: ${body.toString()} }";
  }
}
