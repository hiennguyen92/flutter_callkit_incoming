import 'package:json_annotation/json_annotation.dart';

import 'android_params.dart';
import 'ios_params.dart';

part 'call_kit_params.g.dart';

/// Object config for General.
@JsonSerializable(explicitToJson: true)
class CallKitParams {
  const CallKitParams({
    this.id,
    this.nameCaller,
    this.appName,
    this.avatar,
    this.handle,
    this.type,
    this.duration,
    this.textAccept,
    this.textDecline,
    this.textMissedCall,
    this.textCallback,
    this.extra,
    this.headers,
    this.android,
    this.ios,
  });

  final String? id;
  final String? nameCaller;
  final String? appName;
  final String? avatar;
  final String? handle;
  final double? type;
  final double? duration;
  final String? textAccept;
  final String? textDecline;
  final String? textMissedCall;
  final String? textCallback;
  final Map<String, dynamic>? extra;
  final Map<String, dynamic>? headers;
  final AndroidParams? android;
  final IOSParams? ios;

  factory CallKitParams.fromJson(Map<String, dynamic> json) =>
      _$CallKitParamsFromJson(json);

  Map<String, dynamic> toJson() => _$CallKitParamsToJson(this);
}
