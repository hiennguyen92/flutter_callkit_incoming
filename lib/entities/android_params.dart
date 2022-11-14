import 'package:json_annotation/json_annotation.dart';

part 'android_params.g.dart';

@JsonSerializable()
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
    this.incomingCallNotificationChannelName,
    this.missedCallNotificationChannelName,
  });

  factory AndroidParams.fromJson(Map<String, dynamic> json) =>
      _$AndroidParamsFromJson(json);

  final bool? isCustomNotification;
  final bool? isShowLogo;
  final bool? isShowCallback;
  final bool? isShowMissedCallNotification;
  final String? ringtonePath;
  final String? backgroundColor;
  final String? backgroundUrl;
  final String? actionColor;
  final String? incomingCallNotificationChannelName;
  final String? missedCallNotificationChannelName;

  Map<String, dynamic> toJson() => _$AndroidParamsToJson(this);

  @override
  String toString() =>
      'AndroidParams(' +
      'isCustomNotification: $isCustomNotification, ' +
      'isShowLogo: $isShowLogo, ' +
      'isShowCallback: $isShowCallback, ' +
      'isShowMissedCallNotification: $isShowMissedCallNotification, ' +
      'ringtonePath: $ringtonePath, ' +
      'backgroundColor: $backgroundColor, ' +
      'backgroundUrl: $backgroundUrl, ' +
      'backgroundUrl: $backgroundUrl, ' +
      'actionColor: $actionColor, ' +
      'incomingCallNotificationChannelName: $incomingCallNotificationChannelName, ' +
      'missedCallNotificationChannelName: $missedCallNotificationChannelName' +
      ')';
}
