import 'package:flutter/material.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:flutter_callkit_incoming_example/navigation_service.dart';
import 'package:http/http.dart';

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
                    onPressed: () async {
                      FlutterCallkitIncoming.endCall(calling);
                      calling = null;
                      NavigationService.instance.goBack();
                      await requestHttp('END_CALL');
                    },
                    child: Text('End Call'),
                  )
                ],
              ),
            )));
  }

  //check with https://webhook.site/#!/2748bc41-8599-4093-b8ad-93fd328f1cd2
  Future<void> requestHttp(content) async {
    get(Uri.parse(
        'https://webhook.site/2748bc41-8599-4093-b8ad-93fd328f1cd2?data=$content'));
  }

  @override
  void dispose() {
    super.dispose();
    if (calling != null) {
      FlutterCallkitIncoming.endCall(calling);
    }
  }
}
