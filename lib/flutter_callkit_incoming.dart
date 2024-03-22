import 'dart:async';

import 'package:flutter/services.dart';

import 'entities/entities.dart';

/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()
/// * callConnected(dynamic)

class FlutterCallkitIncoming {
  FlutterCallkitIncoming._();

  static FlutterCallkitIncoming? _instance;

  static FlutterCallkitIncoming get instance =>
      _instance ??= FlutterCallkitIncoming._();

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
  Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  /// Show Callkit Incoming.
  /// On iOS, using Callkit. On Android, using a custom UI.
  Future<void> showCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod<void>("showCallkitIncoming", params.toJson());
  }

  /// Show Miss Call Notification.
  /// Only Android
  Future<void> showMissCallNotification(CallKitParams params) async {
    await _channel.invokeMethod<void>(
        "showMissCallNotification", params.toJson());
  }

  /// Hide notification call for Android.
  /// Only Android
  static Future hideCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod("hideCallkitIncoming", params.toJson());
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  Future<void> startCall(CallKitParams params) async {
    await _channel.invokeMethod<void>("startCall", params.toJson());
  }

  /// Muting an Ongoing call.
  /// On iOS, using Callkit(update the ongoing call ui).
  /// On Android, Nothing(only callback event listener).
  static Future muteCall(String id, {bool isMuted = true}) async {
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
  static Future holdCall(String id, {bool isOnHold = true}) async {
    await _channel.invokeMethod("holdCall", {'id': id, 'isOnHold': isOnHold});
  }

  /// End an Incoming/Outgoing call.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  Future<void> endCall(String id) async {
    await _channel.invokeMethod<void>("endCall", {'id': id});
  }

  /// Set call has been connected successfully.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future setCallConnected(String id) async {
    await _channel.invokeMethod("callConnected", {'id': id});
  }

  /// End all calls.
  Future<void> endAllCalls() async {
    await _channel.invokeMethod<void>("endAllCalls");
  }

  /// Get active calls.
  /// On iOS: return active calls from Callkit.
  /// On Android: only return last call
  Future<List<CallKitParams>?> activeCalls() async {
    final jsonList = await _channel.invokeMethod<List<Object?>?>("activeCalls");

    if (jsonList == null) {
      return null;
    }
    return jsonList
        .map((e) => CallKitParams.fromJson(
            Map<String, dynamic>.from(e as Map<Object?, Object?>)))
        .toList();
  }

  /// Get device push token VoIP.
  /// On iOS: return deviceToken for VoIP.
  /// On Android: return Empty
  Future<String?> getDevicePushTokenVoIP() async {
    return _channel.invokeMethod("getDevicePushTokenVoIP");
  }

  /// Silence CallKit events
  static Future silenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", true);
  }

  /// Unsilence CallKit events
  static Future unsilenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", false);
  }

  /// Request permisstion show notification for Android(13)
  /// Only Android: show request permission post notification for Android 13+
  static Future requestNotificationPermission(dynamic data) async {
    return await _channel.invokeMethod("requestNotificationPermission", data);
  }

  /// Get latest action
  Future<CallEvent?> getLatestEvent() async {
    final event =
        await _channel.invokeMethod<Map<Object?, Object?>?>("getLatestEvent");
    if (event != null) {
      return Future.value(_receiveCallEvent(event));
    } else {
      return null;
    }
  }

  CallEvent? _receiveCallEvent(dynamic data) {
    if (data is! Map) {
      return null;
    }

    switch (data['event']) {
      case ACTION_CALL_INCOMING:
        final callkit = toCallkit(data);
        if (callkit != null) {
          return CallEvent.incoming(callkit);
        }
        throw const FormatException('[ACTION_CALL_INCOMING] body is null.');
      case ACTION_CALL_START:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_START] body is null.');
        }
        return CallEvent.start(callkit);
      case ACTION_CALL_ACCEPT:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_ACCEPT] body is null.');
        }
        return CallEvent.accept(callkit);
      case ACTION_CALL_DECLINE:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_DECLINE] body is null.');
        }
        return CallEvent.decline(callkit);
      case ACTION_CALL_ENDED:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_ENDED] body is null.');
        }
        return CallEvent.ended(callkit);
      case ACTION_CALL_TIMEOUT:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_TIMEOUT] body is null.');
        }
        return CallEvent.timeout(callkit);
      case ACTION_CALL_CALLBACK:
        final callkit = toCallkit(data);
        if (callkit == null) {
          throw const FormatException('[ACTION_CALL_CALLBACK] body is null.');
        }
        return CallEvent.callback(callkit);
      case ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP:
        final body = data['body'] as Map<Object?, Object?>?;
        final deviceToken = body?['deviceTokenVoIP'] as String?;
        if (deviceToken != null) {
          return CallEvent.updateDevicePushToken(deviceToken);
        }
        throw const FormatException(
          '[ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP] deviceTokenVoIP is null.',
        );
      case ACTION_CALL_TOGGLE_HOLD:
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
        return CallEvent.toggleHold(id, isOnHold);
      case ACTION_CALL_TOGGLE_MUTE:
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
        return CallEvent.toggleMute(id, isMuted);
      case ACTION_CALL_TOGGLE_DMTF:
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
              '[ACTION_CALL_TOGGLE_DMTF] typs is null.');
        }
        return CallEvent.toggleDMTF(id, digits, type);
      case ACTION_CALL_TOGGLE_GROUP:
        final body = data['body'] as Map<Object?, Object?>?;
        final id = body?['id'] as String?;
        if (id == null) {
          throw const FormatException('[ACTION_CALL_TOGGLE_GROUP] id is null.');
        }
        final callUUIDToGroupWith = body?['callUUIDToGroupWith'] as String?;
        if (callUUIDToGroupWith == null) {
          throw const FormatException(
            '[ACTION_CALL_TOGGLE_GROUP] callUUIDToGroupWith is null.',
          );
        }
        return CallEvent.toggleGroup(id, callUUIDToGroupWith);
      case ACTION_CALL_TOGGLE_AUDIO_SESSION:
        final body = data['body'] as Map<Object?, Object?>?;
        final isActivate = body?['isActivate'] as bool?;
        if (isActivate == null) {
          throw const FormatException(
            '[ACTION_CALL_TOGGLE_AUDIO_SESSION] isActivate is null.',
          );
        }
        return CallEvent.toggleAudioSession(isActivate);
      default:
        return null;
    }
  }

  CallKitParams? toCallkit(Map data) {
    final body = data['body'] as Map<Object?, Object?>?;
    if (body == null) {
      return null;
    }
    return CallKitParams.fromJson(Map<String, dynamic>.from(body));
  }

  DTMFActionType? toDTMFActionType(Map data) {
    final type = data['type'] as String?;
    if (type == null) {
      return null;
    }

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
