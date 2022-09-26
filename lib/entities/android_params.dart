import 'package:json_annotation/json_annotation.dart';

part 'android_params.g.dart';

@JsonSerializable(explicitToJson: true)
class AndroidParams {
  const AndroidParams({
    this.isCustomNotification,
    this.isShowLogo,
    this.isShowCallback,
    this.isShowMissedCallNotification,
    this.ringtonePath,
    this.backgroundColor,
    this.backgroundUrl,
    this.actionColor,
  });

  final bool? isCustomNotification;
  final bool? isShowLogo;
  final bool? isShowCallback;
  final bool? isShowMissedCallNotification;
  final String? ringtonePath;
  final String? backgroundColor;
  final String? backgroundUrl;
  final String? actionColor;

  factory AndroidParams.fromJson(Map<String, dynamic> json) => _$AndroidParamsFromJson(json);

  Map<String, dynamic> toJson() => _$AndroidParamsToJson(this);
}
