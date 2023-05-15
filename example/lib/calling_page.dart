import 'dart:convert';
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_callkit_incoming/entities/entities.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:flutter_callkit_incoming_example/navigation_service.dart';
import 'package:http/http.dart';

class CallingPage extends StatefulWidget {
  const CallingPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return CallingPageState();
  }
}

class CallingPageState extends State<CallingPage> {
  late CallKitParams? calling;

  Timer? _timer;
  int _start = 0;

  void startTimer() {
    const oneSec = Duration(seconds: 1);
    _timer = Timer.periodic(
      oneSec,
      (Timer timer) {
        setState(() {
          _start++;
        });
      },
    );
  }

  String intToTimeLeft(int value) {
    int h, m, s;
    h = value ~/ 3600;
    m = ((value - h * 3600)) ~/ 60;
    s = value - (h * 3600) - (m * 60);
    String hourLeft = h.toString().length < 2 ? '0$h' : h.toString();
    String minuteLeft = m.toString().length < 2 ? '0$m' : m.toString();
    String secondsLeft = s.toString().length < 2 ? '0$s' : s.toString();
    String result = "$hourLeft:$minuteLeft:$secondsLeft";
    return result;
  }

  @override
  Widget build(BuildContext context) {
    final params = jsonDecode(jsonEncode(
        ModalRoute.of(context)!.settings.arguments as Map<dynamic, dynamic>));
    print(ModalRoute.of(context)!.settings.arguments);
    calling = CallKitParams.fromJson(params);

    var timeDisplay = intToTimeLeft(_start);

    return Scaffold(
      body: SizedBox(
        height: MediaQuery.of(context).size.height,
        width: double.infinity,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text(timeDisplay),
              const Text('Calling...'),
              TextButton(
                style: ButtonStyle(
                  foregroundColor:
                      MaterialStateProperty.all<Color>(Colors.blue),
                ),
                onPressed: () async {
                  if (calling != null) {
                    await makeFakeConnectedCall(calling!.id!);
                    startTimer();
                  }
                },
                child: const Text('Fake Connected Call'),
              ),
              TextButton(
                style: ButtonStyle(
                  foregroundColor:
                      MaterialStateProperty.all<Color>(Colors.blue),
                ),
                onPressed: () async {
                  if (calling != null) {
                    await makeEndCall(calling!.id!);
                    calling = null;
                  }
                  NavigationService.instance.goBack();
                  await requestHttp('END_CALL');
                },
                child: const Text('End Call'),
              )
            ],
          ),
        ),
      ),
    );
  }

  Future<void> makeFakeConnectedCall(id) async {
    await FlutterCallkitIncoming.setCallConnected(id);
  }

  Future<void> makeEndCall(id) async {
    await FlutterCallkitIncoming.endCall(id);
  }

  //check with https://webhook.site/#!/2748bc41-8599-4093-b8ad-93fd328f1cd2
  Future<void> requestHttp(content) async {
    get(Uri.parse(
        'https://webhook.site/2748bc41-8599-4093-b8ad-93fd328f1cd2?data=$content'));
  }

  @override
  void dispose() {
    super.dispose();
    _timer?.cancel();
    if (calling != null) FlutterCallkitIncoming.endCall(calling!.id!);
  }
}
