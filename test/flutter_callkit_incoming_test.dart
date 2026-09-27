import 'package:flutter/services.dart';
import 'package:flutter_callkit_incoming/entities/entities.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('flutter_callkit_incoming');
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  MethodCall? lastCall;

  setUp(() {
    lastCall = null;
    messenger.setMockMethodCallHandler(channel, (methodCall) async {
      lastCall = methodCall;
      return true;
    });
  });

  tearDown(() {
    messenger.setMockMethodCallHandler(channel, null);
  });

  group('AudioRoute', () {
    // The wire values must stay in sync with android.telecom.CallAudioState.
    test('mirrors the CallAudioState route constants', () {
      expect(AudioRoute.earpiece.value, 1); // CallAudioState.ROUTE_EARPIECE
      expect(AudioRoute.bluetooth.value, 2); // CallAudioState.ROUTE_BLUETOOTH
      expect(AudioRoute.wiredHeadset.value,
          4); // CallAudioState.ROUTE_WIRED_HEADSET
      expect(AudioRoute.speaker.value, 8); // CallAudioState.ROUTE_SPEAKER
    });
  });

  group('setAudioRoute', () {
    test('sends the call id and the requested route constant', () async {
      final applied = await FlutterCallkitIncoming.setAudioRoute(
        'call-1',
        AudioRoute.speaker,
      );

      expect(applied, isTrue);
      expect(lastCall?.method, 'setAudioRoute');
      expect(lastCall?.arguments, {'id': 'call-1', 'route': 8});
    });

    test('maps every route to its own constant', () async {
      for (final route in AudioRoute.values) {
        await FlutterCallkitIncoming.setAudioRoute('call-1', route);
        expect((lastCall?.arguments as Map)['route'], route.value);
      }
    });

    test('returns false when there is no active connection', () async {
      // Native side answers false without throwing.
      messenger.setMockMethodCallHandler(channel, (methodCall) async => false);

      expect(
        await FlutterCallkitIncoming.setAudioRoute(
            'missing', AudioRoute.earpiece),
        isFalse,
      );
    });

    test('returns false when the platform replies with no value', () async {
      messenger.setMockMethodCallHandler(channel, (methodCall) async => null);

      expect(
        await FlutterCallkitIncoming.setAudioRoute(
            'call-1', AudioRoute.bluetooth),
        isFalse,
      );
    });
  });
}
