import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'call_kit_method_channel.dart';

abstract class CallKitPlatform extends PlatformInterface {
  /// Constructs a CallKitPlatform.
  CallKitPlatform() : super(token: _token);

  static final Object _token = Object();

  static CallKitPlatform _instance = MethodChannelCallKit();

  /// The default instance of [CallKitPlatform] to use.
  ///
  /// Defaults to [MethodChannelCallKit].
  static CallKitPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CallKitPlatform] when
  /// they register themselves.
  static set instance(CallKitPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
