
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
    await _channel.invokeMethod("showCallkitIncoming", "hihi");
  }
  
}
