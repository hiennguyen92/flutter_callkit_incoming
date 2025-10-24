import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

import 'entities/entities.dart';

/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()
/// * callConnected(dynamic)

class FlutterCallkitIncoming {
  static const MethodChannel _channel =
      MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      EventChannel('flutter_callkit_incoming_events');

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
  static Future<void> showCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod("showCallkitIncoming", params.toJson());
  }

  /// Show Miss Call Notification.
  /// Only Android
  static Future<void> showMissCallNotification(CallKitParams params) async {
    await _channel.invokeMethod("showMissCallNotification", params.toJson());
  }

  /// Hide notification call for Android.
  /// Only Android
  static Future<void> hideCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod("hideCallkitIncoming", params.toJson());
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future<void> startCall(CallKitParams params) async {
    await _channel.invokeMethod("startCall", params.toJson());
  }

  /// Muting an Ongoing call.
  /// On iOS, using Callkit(update the ongoing call ui).
  /// On Android, Nothing(only callback event listener).
  static Future<void> muteCall(String id, {bool isMuted = true}) async {
    await _channel.invokeMethod("muteCall", {'id': id, 'isMuted': isMuted});
  }

  /// Get Callkit Mic Status (muted/unmuted).
  /// On iOS, using Callkit(update call ui).
  /// On Android, Nothing(only callback event listener).
  static Future<bool> isMuted(String id) async {
    return (await _channel.invokeMethod("isMuted", {'id': id})) as bool? ??
        false;
  }

  /// Hold an Ongoing call.
  /// On iOS, using Callkit(update the ongoing call ui).
  /// On Android, Nothing(only callback event listener).
  static Future<void> holdCall(String id, {bool isOnHold = true}) async {
    await _channel.invokeMethod("holdCall", {'id': id, 'isOnHold': isOnHold});
  }

  /// End an Incoming/Outgoing call.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future<void> endCall(String id) async {
    await _channel.invokeMethod("endCall", {'id': id});
  }

  /// Set call has been connected successfully.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future<void> setCallConnected(String id) async {
    await _channel.invokeMethod("callConnected", {'id': id});
  }

  /// End all calls.
  static Future<void> endAllCalls() async {
    await _channel.invokeMethod("endAllCalls");
  }

  /// Get active calls.
  /// On iOS: return active calls from Callkit.
  /// On Android: only return last call
  static Future<List<CallKitParams>> activeCalls() async {
    final result = await _channel.invokeMethod("activeCalls");
    if (result is! List) {
      return [];
    }

    return result.map((data) {
      return CallKitParams.fromJson(_convertMap(data) as Map<String, dynamic>);
    }).toList();
  }

  /// Get device push token VoIP.
  /// On iOS: return deviceToken for VoIP.
  /// On Android: return Empty
  static Future<String?> getDevicePushTokenVoIP() async {
    return await _channel.invokeMethod("getDevicePushTokenVoIP");
  }

  /// Silence CallKit events
  static Future<bool> silenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", true);
  }

  /// Unsilence CallKit events
  static Future<bool> unsilenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", false);
  }

  /// Request permission show notification for Android(13)
  /// Only Android: show request permission post notification for Android 13+
  static Future<bool> requestNotificationPermission(dynamic data) async {
    return await _channel.invokeMethod("requestNotificationPermission", data);
  }

  /// Request permission show notification for Android(14)+
  /// Only Android: show request permission for ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
  static Future<bool> requestFullIntentPermission() async {
    return await _channel.invokeMethod("requestFullIntentPermission");
  }

  /// Check can use full screen intent for Android(14)+
  /// Only Android: canUseFullScreenIntent permission for ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
  static Future<bool> canUseFullScreenIntent() async {
    return await _channel.invokeMethod("canUseFullScreenIntent");
  }

  static CallEvent? _receiveCallEvent(dynamic data) {
    if (data is! Map) {
      return null;
    }

    final eventName = data['event'];
    switch (eventName) {
      case CallEventConstants.actionDidUpdateDevicePushTokenVoip:
        return const CallEventActionDidUpdateDevicePushTokenVoip();
      case CallEventConstants.actionCallIncoming:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_INCOMING] body is null.');
        }
        return CallEventActionCallIncoming(callkitParams);
      case CallEventConstants.actionCallStart:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_START] id is null.');
        }
        return CallEventActionCallStart(callkitParams.id);
      case CallEventConstants.actionCallAccept:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_ACCEPT] id is null.');
        }
        return CallEventActionCallAccept(callkitParams.id);
      case CallEventConstants.actionCallDecline:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_DECLINE] id is null.');
        }
        return CallEventActionCallDecline(callkitParams.id);
      case CallEventConstants.actionCallEnded:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_ENDED] id is null.');
        }
        return CallEventActionCallEnded(callkitParams.id);
      case CallEventConstants.actionCallTimeout:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_TIMEOUT] id is null.');
        }
        return CallEventActionCallTimeout(callkitParams.id);
      case CallEventConstants.actionCallConnected:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_CONNECTED] id is null.');
        }
        return CallEventActionCallConnected(callkitParams.id);
      case CallEventConstants.actionCallCallback:
        final callkitParams = toCallkitParams(data);
        if (callkitParams == null) {
          throw const FormatException('[ACTION_CALL_CALLBACK] id is null.');
        }
        return CallEventActionCallCallback(callkitParams.id);
      case CallEventConstants.actionCallToggleHold:
        final body = data['body'] as Map<Object?, Object?>?;
        final id = body?['id'] as String?;
        if (id == null) {
          throw const FormatException('[ACTION_CALL_TOGGLE_HOLD] id is null.');
        }
        final isOnHold = body?['isOnHold'] as bool?;
        if (isOnHold == null) {
          throw const FormatException(
              '[ACTION_CALL_TOGGLE_HOLD] isOnHold is null.');
        }
        return CallEventActionCallToggleHold(
          id,
          isOnHold,
        );
      case CallEventConstants.actionCallToggleMute:
        final body = data['body'] as Map<Object?, Object?>?;
        final id = body?['id'] as String?;
        if (id == null) {
          throw const FormatException('[ACTION_CALL_TOGGLE_MUTE] id is null.');
        }
        final isMuted = body?['isMuted'] as bool?;
        if (isMuted == null) {
          throw const FormatException(
              '[ACTION_CALL_TOGGLE_MUTE] isMuted is null.');
        }
        return CallEventActionCallToggleMute(id, isMuted);
      case CallEventConstants.actionCallToggleDmtf:
        final body = data['body'] as Map<Object?, Object?>?;
        final id = body?['id'] as String?;
        if (id == null) {
          throw const FormatException('[ACTION_CALL_TOGGLE_DMTF] id is null.');
        }
        final digits = body?['digits'] as String?;
        if (digits == null) {
          throw const FormatException(
              '[ACTION_CALL_TOGGLE_DMTF] digits is null.');
        }
        final type = body != null ? toDTMFActionType(body) : null;
        if (type == null) {
          throw const FormatException(
              '[ACTION_CALL_TOGGLE_DMTF] type is null.');
        }
        return CallEventActionCallToggleDmtf(id, digits, type);
      case CallEventConstants.actionCallToggleGroup:
        final body = data['body'] as Map<Object?, Object?>?;
        final id = body?['id'] as String?;
        if (id == null) {
          throw const FormatException('[ACTION_CALL_TOGGLE_GROUP] id is null.');
        }
        final callUUIDToGroupWith = body?['callUUIDToGroupWith'] as String?;
        return CallEventActionCallToggleGroup(id, callUUIDToGroupWith);
      case CallEventConstants.actionCallToggleAudioSession:
        final body = data['body'] as Map<Object?, Object?>?;
        final isActive = body?['isActive'] as bool?;
        if (isActive == null) {
          throw const FormatException(
              '[ACTION_CALL_TOGGLE_AUDIO_SESSION] id is null.');
        }
        return CallEventActionCallToggleAudioSession(isActive);
      case CallEventConstants.actionCallCustom:
        final body = data['body'] as Map<String, dynamic>?;
        if (body == null) {
          throw const FormatException('[ACTION_CALL_CUSTOM] body is null.');
        }
        return CallEventActionCallCustom(body);
      default:
        return null;
    }
  }

  static dynamic _convertMap(dynamic data) {
    if (data is Map) {
      return data
          .map((key, value) => MapEntry(key.toString(), _convertMap(value)));
    } else if (data is List) {
      return data.map((item) => _convertMap(item)).toList();
    } else {
      return data;
    }
  }

  static CallKitParams? toCallkitParams(Map<dynamic, dynamic> data) {
    final body = data['data'];
    if (body == null) {
      return null;
    }
    final jsonData = jsonDecode(jsonEncode(body)) as Map<String, dynamic>;
    return CallKitParams.fromJson(jsonData);
  }

  static DTMFActionType? toDTMFActionType(Map data) {
    final type = data['type'] as String?;
    if (type == null) return null;

    switch (type) {
      case 'singleTone':
        return DTMFActionType.singleTone;
      case 'softPause':
        return DTMFActionType.softPause;
      case 'hardPause':
        return DTMFActionType.hardPause;
      default:
        return null;
    }
  }
}
