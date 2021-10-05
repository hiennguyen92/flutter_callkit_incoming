import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      const MethodChannel('flutter_callkit_incoming');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future showCallkitIncoming() async {
    await _channel.invokeMethod("showCallkitIncoming", <String, dynamic>{
      'id': 'id',
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
}
