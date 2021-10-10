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
  var uuid = Uuid();
  var currentUuid;
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();

    FlutterCallkitIncoming.onEvent.listen((event) {
      print(event);
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await FlutterCallkitIncoming.platformVersion ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
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
                  this.currentUuid = uuid.v4();
                  var params = <String, dynamic>{
                    'id': currentUuid,
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
                var params = <String, dynamic>{
                  'id': this.currentUuid,
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
                };
                await FlutterCallkitIncoming.endCall(params);
              },
            )
          ],
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }
}
