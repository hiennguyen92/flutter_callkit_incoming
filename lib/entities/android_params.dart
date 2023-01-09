import 'package:json_annotation/json_annotation.dart';

part 'android_params.g.dart';

/// Object config for Android.
@JsonSerializable(explicitToJson: true)
class AndroidParams {
  const AndroidParams({
    this.isCustomNotification,
    this.isCustomSmallExNotification,
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

  /// Using custom notifications.
  final bool? isCustomNotification;

  /// Using custom notification small on some devices clipped out in android.
  final bool? isCustomSmallExNotification;

  /// Show logo app inside full screen.
  final bool? isShowLogo;

  /// Show callback action from miss call notification.
  final bool? isShowCallback;

  /// Show missed call notification when timeout
  final bool? isShowMissedCallNotification;

  /// File name ringtone, put file into /android/app/src/main/res/raw/ringtone_default.pm3 -> value: `ringtone_default.pm3`
  final String? ringtonePath;

  /// Incoming call screen background color.
  final String? backgroundColor;

  /// Using image background for Incoming call screen. example: http://... https://... or "assets/abc.png"
  final String? backgroundUrl;

  /// Color used in button/text on notification.
  final String? actionColor;

  /// Notification channel name of incoming call.
  final String? incomingCallNotificationChannelName;

  /// Notification channel name of missed call.
  final String? missedCallNotificationChannelName;

  factory AndroidParams.fromJson(Map<String, dynamic> json) =>
      _$AndroidParamsFromJson(json);

  Map<String, dynamic> toJson() => _$AndroidParamsToJson(this);
}
