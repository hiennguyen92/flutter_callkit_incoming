import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel = const EventChannel('flutter_callkit_incoming_events');

  static Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future showCallkitIncoming(dynamic params) async {
    await _channel.invokeMethod("showCallkitIncoming", params);
  }

  static Future endCall(dynamic params) async {
    await _channel.invokeMethod("endCall", params);
  }

  static Future endAllCalls(dynamic params) async {
    await _channel.invokeMethod("endAllCalls", params);
  }

  static CallEvent? _receiveCallEvent(dynamic data) {
    if(data is Map){
      final event = data['event'];
      final body = Map<String, dynamic>.from(data['body']);
      print('event: ${event.toString()}');
      print('body: ${body.toString()}');
    }
    return null;
  }
}

class CallEvent {

}
