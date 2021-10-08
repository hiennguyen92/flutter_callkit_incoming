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

  static Future showCallkitIncoming() async {
    await _channel.invokeMethod("showCallkitIncoming", <String, dynamic>{
      'id': '5b8b5a49-ca42-4637-a7e6-6208f192df61',
      'nameCaller': 'Hien Nguyen',
      'avatar': 'https://i.pravatar.cc/100',
      'number': 'Callkit: 0123456789',
      'type': 0,
      'duration': 30000,
      'extra': <String, dynamic>{
        'userId': '1234abcd'
      },
      'android': <String, dynamic>{
        'isCustomNotification': true,
        'sound': 'ringtone_default',
        'backgroundColor': '#0955fa',
        'background': 'https://i.pravatar.cc/500',
        'actionColor': '#4CAF50'
      }
    });
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
