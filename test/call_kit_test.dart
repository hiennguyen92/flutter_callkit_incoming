import 'package:flutter_test/flutter_test.dart';
import 'package:call_kit/call_kit.dart';
import 'package:call_kit/call_kit_platform_interface.dart';
import 'package:call_kit/call_kit_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockCallKitPlatform
    with MockPlatformInterfaceMixin
    implements CallKitPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final CallKitPlatform initialPlatform = CallKitPlatform.instance;

  test('$MethodChannelCallKit is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelCallKit>());
  });

  test('getPlatformVersion', () async {
    CallKit callKitPlugin = CallKit();
    MockCallKitPlatform fakePlatform = MockCallKitPlatform();
    CallKitPlatform.instance = fakePlatform;

    expect(await callKitPlugin.getPlatformVersion(), '42');
  });
}
