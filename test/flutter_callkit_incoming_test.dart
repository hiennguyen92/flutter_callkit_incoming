import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_callkit_incoming');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterCallkitIncoming.platformVersion, '42');
  });
}
