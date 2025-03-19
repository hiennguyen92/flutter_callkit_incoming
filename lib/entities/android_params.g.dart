// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'android_params.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AndroidParams _$AndroidParamsFromJson(Map<String, dynamic> json) =>
    AndroidParams(
      isCustomNotification: json['isCustomNotification'] as bool?,
      isCustomSmallExNotification: json['isCustomSmallExNotification'] as bool?,
      isShowLogo: json['isShowLogo'] as bool?,
      isShowCallID: json['isShowCallID'] as bool?,
      ringtonePath: json['ringtonePath'] as String?,
      backgroundColor: json['backgroundColor'] as String?,
      backgroundUrl: json['backgroundUrl'] as String?,
      actionColor: json['actionColor'] as String?,
      textColor: json['textColor'] as String?,
      incomingCallNotificationChannelName:
          json['incomingCallNotificationChannelName'] as String?,
      missedCallNotificationChannelName:
          json['missedCallNotificationChannelName'] as String?,
      isShowFullLockedScreen: json['isShowFullLockedScreen'] as bool?,
      isImportant: json['isImportant'] as bool?,
      isBot: json['isBot'] as bool?,
      isFullScreen: json['isFullScreen'] as bool?,
    );

Map<String, dynamic> _$AndroidParamsToJson(AndroidParams instance) =>
    <String, dynamic>{
      'isCustomNotification': instance.isCustomNotification,
      'isCustomSmallExNotification': instance.isCustomSmallExNotification,
      'isShowLogo': instance.isShowLogo,
      'isShowCallID': instance.isShowCallID,
      'ringtonePath': instance.ringtonePath,
      'backgroundColor': instance.backgroundColor,
      'backgroundUrl': instance.backgroundUrl,
      'actionColor': instance.actionColor,
      'textColor': instance.textColor,
      'incomingCallNotificationChannelName':
          instance.incomingCallNotificationChannelName,
      'missedCallNotificationChannelName':
          instance.missedCallNotificationChannelName,
      'isShowFullLockedScreen': instance.isShowFullLockedScreen,
      'isImportant': instance.isImportant,
      'isBot': instance.isBot,
      'isFullScreen': instance.isFullScreen,
    };
