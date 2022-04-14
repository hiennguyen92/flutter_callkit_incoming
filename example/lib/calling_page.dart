import 'package:flutter/material.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:flutter_callkit_incoming_example/navigation_service.dart';

class CallingPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return CallingPageState();
  }
}

class CallingPageState extends State<CallingPage> {
  late dynamic calling;

  @override
  Widget build(BuildContext context) {
    calling = ModalRoute.of(context)!.settings.arguments;
    print(calling);

    return Scaffold(
        body: Container(
            height: MediaQuery.of(context).size.height,
            width: double.infinity,
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text('Calling...'),
                  TextButton(
                    style: ButtonStyle(
                      foregroundColor:
                          MaterialStateProperty.all<Color>(Colors.blue),
                    ),
                    onPressed: () {
                      FlutterCallkitIncoming.endCall(calling);
                      calling = null;
                      NavigationService.instance.goBack();
                    },
                    child: Text('End Call'),
                  )
                ],
              ),
            )));
  }

  @override
  void dispose() {
    super.dispose();
    if (calling != null) {
      FlutterCallkitIncoming.endCall(calling);
    }
  }
}
