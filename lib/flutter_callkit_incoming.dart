import 'dart:async';

import 'dart:ui';

import 'package:flutter/services.dart';

import 'entities/entities.dart';

import 'package:flutter/material.dart';


/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()
/// * callConnected(dynamic)

typedef BackgroundMessageHandler = Future<void> Function(CallEvent callEvent);

@pragma('vm:entry-point')
void _flutterCallkitIncomingCallbackDispatcher() {
  WidgetsFlutterBinding.ensureInitialized();

  const MethodChannel backgroundChannel =
  MethodChannel('flutter_callkit_incoming_background');

  const MethodChannel channel =
  MethodChannel('flutter_callkit_incoming');

  backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final int rawHandle =
      await channel.invokeMethod<int>('getBackgroundHandler') ?? 0;

    final callback = PluginUtilities.getCallbackFromHandle(
      CallbackHandle.fromRawHandle(rawHandle),
    ) as Future<void> Function(CallEvent callEvent);

    final Event event = Event.values.firstWhere((e) => e.name == call.method);
    await callback(CallEvent(call.arguments, event));
  });
}

class FlutterCallkitIncoming {

  static const MethodChannel _channel =
      MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      EventChannel('flutter_callkit_incoming_events');


  /// Set a message handler function which is called when the app is in the
  /// background or terminated.
  ///
  /// This provided handler must be a top-level function and cannot be
  /// anonymous otherwise an [ArgumentError] will be thrown.
  static Future<void> onBackgroundMessage(BackgroundMessageHandler handler) async {
    final CallbackHandle pluginHandle = PluginUtilities.getCallbackHandle(
      _flutterCallkitIncomingCallbackDispatcher,
    )!;
    final CallbackHandle userHandle = PluginUtilities.getCallbackHandle(handler)!;
    await _channel.invokeMapMethod('registerBackgroundHandler',
        {
          'pluginHandle': pluginHandle.toRawHandle(),
          'userHandle': userHandle.toRawHandle()
        });
  }


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

  /// Hide notification call for Android.
  /// Only Android
  static Future hideCallkitIncoming(CallKitParams params) async {
    await _channel.invokeMethod("hideCallkitIncoming", params.toJson());
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future startCall(CallKitParams params) async {
    await _channel.invokeMethod("startCall", params.toJson());
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
  static Future endCall(String id) async {
    await _channel.invokeMethod("endCall", {'id': id});
  }

  /// Set call has been connected successfully.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  static Future setCallConnected(String id) async {
    await _channel.invokeMethod("callConnected", {'id': id});
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

  /// Silence CallKit events
  static Future silenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", true);
  }

  /// Unsilence CallKit events
  static Future unsilenceEvents() async {
    return await _channel.invokeMethod("silenceEvents", false);
  }

  /// Request permission show notification for Android(13)
  /// Only Android: show request permission post notification for Android 13+
  static Future requestNotificationPermission(dynamic data) async {
    return await _channel.invokeMethod("requestNotificationPermission", data);
  }

  /// Request permission show notification for Android(14)+
  /// Only Android: show request permission for ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
  static Future requestFullIntentPermission() async {
    return await _channel.invokeMethod("requestFullIntentPermission");
  }

  /// Check can use full screen intent for Android(14)+
  /// Only Android: canUseFullScreenIntent permission for ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
  static Future canUseFullScreenIntent() async {
    return await _channel.invokeMethod("canUseFullScreenIntent");
  }

  static CallEvent? _receiveCallEvent(dynamic data) {
    if (data is! Map) return null;
    // Phase 8.2 v2.3 (SnowChat fork) — Plugin emits SnowChat-specific events
    // (e.g. AUDIO_SESSION_ACTIVATED, AUDIO_SESSION_ACTIVATED_REPLAY,
    // AUDIO_SESSION_DEACTIVATED, PROVIDER_DID_RESET) that aren't in the
    // upstream `Event` enum. Without orElse the firstWhere throws and the
    // whole event subscription dies (Bad state: No element). Fall back to
    // actionCallCustom so the listener stays alive; downstream consumers
    // (CallKitManager._mapAction) ignore unknown events.
    final eventName = data['event'];
    final event = Event.values.firstWhere(
      (e) => e.name == eventName,
      orElse: () => Event.actionCallCustom,
    );
    Map<String, dynamic> body = {};
    final rawBody = data['body'];
    if (rawBody is Map) {
      body = Map<String, dynamic>.from(rawBody);
    }
    return CallEvent(body, event);
  }
}
