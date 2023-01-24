import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_callkit_incoming/entities/android_params.dart';
import 'package:flutter_callkit_incoming/entities/call_event.dart';
import 'package:flutter_callkit_incoming/entities/call_kit.dart';
import 'package:flutter_callkit_incoming/entities/ios_params.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:flutter_callkit_incoming_example/app_router.dart';
import 'package:flutter_callkit_incoming_example/navigation_service.dart';
import 'package:http/http.dart';
import 'package:uuid/uuid.dart';

class HomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return HomePageState();
  }
}

class HomePageState extends State<HomePage> {
  late final Uuid _uuid;
  String? _currentUuid;
  String textEvents = "";

  @override
  void initState() {
    super.initState();
    _uuid = Uuid();
    _currentUuid = "";
    textEvents = "";
    initCurrentCall();
    listenerEvent(onEvent);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
        actions: <Widget>[
          IconButton(
            icon: Icon(
              Icons.call,
              color: Colors.white,
            ),
            onPressed: makeFakeCallInComing,
          ),
          IconButton(
            icon: Icon(
              Icons.call_end,
              color: Colors.white,
            ),
            onPressed: endCurrentCall,
          ),
          IconButton(
            icon: Icon(
              Icons.call_made,
              color: Colors.white,
            ),
            onPressed: startOutGoingCall,
          ),
          IconButton(
            icon: Icon(
              Icons.call_merge,
              color: Colors.white,
            ),
            onPressed: activeCalls,
          ),
          IconButton(
            icon: Icon(
              Icons.clear_all_sharp,
              color: Colors.white,
            ),
            onPressed: endAllCalls,
          )
        ],
      ),
      body: LayoutBuilder(
        builder: (BuildContext context, BoxConstraints viewportConstraints) {
          if (textEvents.isNotEmpty) {
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(
                  minHeight: viewportConstraints.maxHeight,
                ),
                child: Text('$textEvents'),
              ),
            );
          } else {
            return Center(
              child: Text('No Event'),
            );
          }
        },
      ),
    );
  }

  Future<CallKit?> initCurrentCall() async {
    //check current call from pushkit if possible
    final calls = await FlutterCallkitIncoming.instance.activeCalls();
    if (calls != null && calls.isNotEmpty) {
      print('DATA: $calls');
      _currentUuid = calls.first.id;
      return calls.first;
    } else {
      _currentUuid = "";
      return null;
    }
  }

  Future<void> makeFakeCallInComing() async {
    await Future.delayed(const Duration(seconds: 10), () async {
      _currentUuid = _uuid.v4();

      final params = CallKit(
        id: _currentUuid!,
        nameCaller: 'Hien Nguyen',
        appName: 'Callkit',
        avatar: 'https://i.pravatar.cc/100',
        handle: '0123456789',
        type: 0,
        duration: 30000,
        textAccept: 'Accept',
        textDecline: 'Decline',
        textMissedCall: 'Missed call',
        textCallback: 'Call back',
        extra: <String, dynamic>{'userId': '1a2b3c4d'},
        headers: <String, dynamic>{'apiKey': 'Abc@123!', 'platform': 'flutter'},
        android: const AndroidParams(
          isCustomNotification: true,
          isShowLogo: false,
          isShowCallback: true,
          isShowMissedCallNotification: true,
          ringtonePath: 'system_ringtone_default',
          backgroundColor: '#0955fa',
          backgroundUrl: 'assets/test.png',
          actionColor: '#4CAF50',
          incomingCallNotificationChannelName: 'Incoming Call',
          missedCallNotificationChannelName: 'Missed Call',
        ),
        ios: IOSParams(
          iconName: 'CallKitLogo',
          handleType: '',
          supportsVideo: true,
          maximumCallGroups: 2,
          maximumCallsPerCallGroup: 1,
          audioSessionMode: 'default',
          audioSessionActive: true,
          audioSessionPreferredSampleRate: 44100.0,
          audioSessionPreferredIOBufferDuration: 0.005,
          supportsDTMF: true,
          supportsHolding: true,
          supportsGrouping: false,
          supportsUngrouping: false,
          ringtonePath: 'system_ringtone_default',
        ),
      );
      await FlutterCallkitIncoming.instance.showCallkitIncoming(params);
    });
  }

  Future<void> endCurrentCall() async {
    initCurrentCall();
    await FlutterCallkitIncoming.instance.endCall(_currentUuid!);
  }

  Future<void> startOutGoingCall() async {
    _currentUuid = _uuid.v4();
    final params = CallKit(
      id: _currentUuid!,
      nameCaller: 'Hien Nguyen',
      handle: '0123456789',
      type: 1,
      extra: <String, dynamic>{'userId': '1a2b3c4d'},
      ios: IOSParams(handleType: 'number'),
    );
    await FlutterCallkitIncoming.instance.startCall(params);
  }

  Future<void> activeCalls() async {
    var calls = await FlutterCallkitIncoming.instance.activeCalls();
    print(calls);
  }

  Future<void> endAllCalls() async {
    await FlutterCallkitIncoming.instance.endAllCalls();
  }

  Future<void> getDevicePushTokenVoIP() async {
    var devicePushTokenVoIP =
        await FlutterCallkitIncoming.instance.getDevicePushTokenVoIP();
    print(devicePushTokenVoIP);
  }

  Future<void> listenerEvent(Function? callback) async {
    try {
      FlutterCallkitIncoming.instance.onEvent.listen((event) async {
        print('HOME: $event');

        if (event is CallActionIncoming) {
          // TODO: received an incoming call
        } else if (event is CallActionStart) {
          // TODO: started an outgoing call
          // TODO: show screen calling in Flutter
        } else if (event is CallActionAccept) {
          // TODO: accepted an incoming call
          // TODO: show screen calling in Flutter
          NavigationService.instance.pushNamedIfNotCurrent(
            AppRoute.callingPage,
            args: event.callKit,
          );
        } else if (event is CallActionDecline) {
          // TODO: declined an incoming call
          await requestHttp("ACTION_CALL_DECLINE_FROM_DART");
        } else if (event is CallActionEnded) {
          // TODO: ended an incoming/outgoing call
        } else if (event is CallActionTimeout) {
          // TODO: missed an incoming call
        } else if (event is CallActionCallback) {
          // TODO: only Android - click action `Call back` from missed call notification
        }

        if (callback != null) {
          callback(event.toString());
        }
      });
    } on Exception {}
  }

  //check with https://webhook.site/#!/2748bc41-8599-4093-b8ad-93fd328f1cd2
  Future<void> requestHttp(String content) async {
    get(Uri.parse(
        'https://webhook.site/2748bc41-8599-4093-b8ad-93fd328f1cd2?data=$content'));
  }

  void onEvent(String event) {
    if (!mounted) {
      return;
    }

    setState(() {
      textEvents += "$event\n";
    });
  }
}
