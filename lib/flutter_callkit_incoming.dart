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
      'nameCaller': 'Hello ABC',
      'avatar': 'https://picsum.photos/seed/picsum/200/300',
      'number': 'Callkit: 0123456789',
      'type': 1,
      'duration': 30000,
      'android': <String, dynamic>{
        'sound': 'ringtone_default'
      }
    });
  }
}
