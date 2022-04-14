import 'dart:ffi';

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_callkit_incoming_example/app_router.dart';
import 'package:flutter_callkit_incoming_example/navigation_service.dart';
import 'dart:async';
import 'dart:convert';
import 'package:uuid/uuid.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';

Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print("Handling a background message: ${message.messageId}");
  listenerEvent(null);
  showCallkitIncoming(Uuid().v4()); //'5c3ab1f0-8750-42eb-8d29-ad28e3d7f336');
}

Future<void> listenerEvent(Function? callback) async {
  try {
    FlutterCallkitIncoming.onEvent.listen((event) async {
      print('ROOT: $event');
      switch (event!.name) {
        case CallEvent.ACTION_CALL_INCOMING:
          // TODO: received an incoming call
          break;
        case CallEvent.ACTION_CALL_START:
          // TODO: started an outgoing call
          // TODO: show screen calling in Flutter
          break;
        case CallEvent.ACTION_CALL_ACCEPT:
          // TODO: accepted an incoming call
          // TODO: show screen calling in Flutter
          // TODO: It may not be possible to navigate here because the App Widget has not been rendered. can save data call to local or use activeCalls() and check it in `WidgetsBinding.instance?.addPostFrameCallback`
          break;
        case CallEvent.ACTION_CALL_DECLINE:
          // TODO: declined an incoming call
          break;
        case CallEvent.ACTION_CALL_ENDED:
          // TODO: ended an incoming/outgoing call
          break;
        case CallEvent.ACTION_CALL_TIMEOUT:
          // TODO: missed an incoming call
          break;
        case CallEvent.ACTION_CALL_CALLBACK:
          // TODO: only Android - click action `Call back` from missed call notification
          break;
        case CallEvent.ACTION_CALL_TOGGLE_HOLD:
          // TODO: only iOS
          break;
        case CallEvent.ACTION_CALL_TOGGLE_MUTE:
          // TODO: only iOS
          break;
        case CallEvent.ACTION_CALL_TOGGLE_DMTF:
          // TODO: only iOS
          break;
        case CallEvent.ACTION_CALL_TOGGLE_GROUP:
          // TODO: only iOS
          break;
        case CallEvent.ACTION_CALL_TOGGLE_AUDIO_SESSION:
          // TODO: only iOS
          break;
        case CallEvent.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP:
          // TODO: only iOS
          break;
      }
      if (callback != null) {
        callback(event);
      }
    });
  } on Exception {}
}

Future<void> showCallkitIncoming(String uuid) async {
  var params = <String, dynamic>{
    'id': uuid,
    'nameCaller': 'Hien Nguyen',
    'appName': 'Callkit',
    'avatar': 'https://i.pravatar.cc/100',
    'handle': '0123456789',
    'type': 0,
    'duration': 30000,
    'textAccept': 'Accept',
    'textDecline': 'Decline',
    'textMissedCall': 'Missed call',
    'textCallback': 'Call back',
    'extra': <String, dynamic>{'userId': '1a2b3c4d'},
    'headers': <String, dynamic>{'apiKey': 'Abc@123!', 'platform': 'flutter'},
    'android': <String, dynamic>{
      'isCustomNotification': true,
      'isShowLogo': false,
      'isShowCallback': false,
      'ringtonePath': 'system_ringtone_default',
      'backgroundColor': '#0955fa',
      'background': 'https://i.pravatar.cc/500',
      'actionColor': '#4CAF50'
    },
    'ios': <String, dynamic>{
      'iconName': 'CallKitLogo',
      'handleType': '',
      'supportsVideo': true,
      'maximumCallGroups': 2,
      'maximumCallsPerCallGroup': 1,
      'audioSessionMode': 'default',
      'audioSessionActive': true,
      'audioSessionPreferredSampleRate': 44100.0,
      'audioSessionPreferredIOBufferDuration': 0.005,
      'supportsDTMF': true,
      'supportsHolding': true,
      'supportsGrouping': false,
      'supportsUngrouping': false,
      'ringtonePath': 'system_ringtone_default'
    }
  };
  await FlutterCallkitIncoming.showCallkitIncoming(params);
}

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var _uuid;
  var _currentUuid;

  late final FirebaseMessaging _firebaseMessaging;

  @override
  void initState() {
    super.initState();
    _uuid = Uuid();
    initFirebase();
    WidgetsBinding.instance?.addPostFrameCallback((_) async {
      var currentCall = await getCurrentCall();
      if (currentCall != null) {
        NavigationService.instance
            .pushNamed(AppRoute.callingPage, args: currentCall);
      }
    });
  }

  getCurrentCall() async {
    //check current call from pushkit if possible
    var calls = await FlutterCallkitIncoming.activeCalls();
    final objCalls = json.decode(calls);
    if (objCalls is List) {
      if (objCalls.isNotEmpty) {
        print('DATA: $objCalls');
        this._currentUuid = objCalls[0]['id'];
        return objCalls[0];
      } else {
        this._currentUuid = "";
        return null;
      }
    }
  }

  initFirebase() async {
    await Firebase.initializeApp();
    _firebaseMessaging = FirebaseMessaging.instance;
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
    FirebaseMessaging.onMessage.listen((RemoteMessage message) async {
      print(
          'Message title: ${message.notification?.title}, body: ${message.notification?.body}, data: ${message.data}');
      this._currentUuid = _uuid.v4();
      showCallkitIncoming(this._currentUuid);
    });
    _firebaseMessaging.getToken().then((token) {
      print('Device Token FCM: $token');
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData.light(),
      onGenerateRoute: AppRoute.generateRoute,
      initialRoute: AppRoute.homePage,
      navigatorKey: NavigationService.instance.navigationKey,
      navigatorObservers: <NavigatorObserver>[
        NavigationService.instance.routeObserver
      ],
    );
  }

  Future<void> getDevicePushTokenVoIP() async {
    var devicePushTokenVoIP =
        await FlutterCallkitIncoming.getDevicePushTokenVoIP();
    print(devicePushTokenVoIP);
  }
}
