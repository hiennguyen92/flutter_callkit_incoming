import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var _uuid = Uuid();
  var _currentUuid;
  var textEvents = "";

  @override
  void initState() {
    super.initState();
    listenerEvent();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> listenerEvent() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      FlutterCallkitIncoming.onEvent.listen((event) {
        print(event);
        if (!mounted) return;
        setState(() {
          textEvents += "${event.toString()}\n";
        });
      });
    } on Exception {}
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
          actions: <Widget>[
            IconButton(
              icon: Icon(
                Icons.call,
                color: Colors.white,
              ),
              onPressed: () async {
                await Future.delayed(const Duration(seconds: 5), () async {
                  this._currentUuid = _uuid.v4();
                  var params = <String, dynamic>{
                    'id': _currentUuid,
                    'nameCaller': 'Hien Nguyen',
                    'appName': 'Callkit',
                    'avatar': 'https://i.pravatar.cc/100',
                    'handle': 'Callkit: 0123456789',
                    'type': 0,
                    'duration': 30000,
                    'extra': <String, dynamic>{'userId': '1234abcd'},
                    'android': <String, dynamic>{
                      'isCustomNotification': true,
                      'sound': 'ringtone_default',
                      'backgroundColor': '#0955fa',
                      'background': 'https://i.pravatar.cc/500',
                      'actionColor': '#4CAF50'
                    },
                    'ios': <String, dynamic>{
                      'iconName': 'AppIcon40x40',
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
                      'supportsGrouping': true,
                      'supportsUngrouping': true,
                      'ringtonePath': 'Ringtone.caf'
                    }
                  };
                  await FlutterCallkitIncoming.showCallkitIncoming(params);
                });
              },
            ),
            IconButton(
              icon: Icon(
                Icons.call_end,
                color: Colors.white,
              ),
              onPressed: () async {
                var params = <String, dynamic>{'id': this._currentUuid};
                await FlutterCallkitIncoming.endCall(params);
              },
            ),
            IconButton(
              icon: Icon(
                Icons.call_made,
                color: Colors.white,
              ),
              onPressed: () async {
                this._currentUuid = _uuid.v4();
                var params = <String, dynamic>{
                  'id': this._currentUuid,
                  'handle': '0123456789',
                  'type': 1,
                  'ios': <String, dynamic>{'handleType': 'number'}
                }; //number/email
                await FlutterCallkitIncoming.startCall(params);
              },
            )
          ],
        ),
        body: LayoutBuilder(
          builder: (BuildContext context, BoxConstraints viewportConstraints) {
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(
                  minHeight: viewportConstraints.maxHeight,
                ),
                child: Text('$textEvents'),
              ),
            );
          },
        ),
      ),
    );
  }
}
