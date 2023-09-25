// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'android_params.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AndroidParams _$AndroidParamsFromJson(Map json) => $checkedCreate(
      'AndroidParams',
      json,
      ($checkedConvert) {
        final val = AndroidParams(
          isCustomNotification:
              $checkedConvert('isCustomNotification', (v) => v as bool?),
          isCustomSmallExNotification:
              $checkedConvert('isCustomSmallExNotification', (v) => v as bool?),
          isShowLogo: $checkedConvert('isShowLogo', (v) => v as bool?),
          ringtonePath: $checkedConvert('ringtonePath', (v) => v as String?),
          backgroundColor:
              $checkedConvert('backgroundColor', (v) => v as String?),
          backgroundUrl: $checkedConvert('backgroundUrl', (v) => v as String?),
          actionColor: $checkedConvert('actionColor', (v) => v as String?),
          incomingCallNotificationChannelName: $checkedConvert(
              'incomingCallNotificationChannelName', (v) => v as String?),
          missedCallNotificationChannelName: $checkedConvert(
              'missedCallNotificationChannelName', (v) => v as String?),
        );
        return val;
      },
    );

Map<String, dynamic> _$AndroidParamsToJson(AndroidParams instance) =>
    <String, dynamic>{
      'isCustomNotification': instance.isCustomNotification,
      'isCustomSmallExNotification': instance.isCustomSmallExNotification,
      'isShowLogo': instance.isShowLogo,
      'ringtonePath': instance.ringtonePath,
      'backgroundColor': instance.backgroundColor,
      'backgroundUrl': instance.backgroundUrl,
      'actionColor': instance.actionColor,
      'incomingCallNotificationChannelName':
          instance.incomingCallNotificationChannelName,
      'missedCallNotificationChannelName':
          instance.missedCallNotificationChannelName,
    };
