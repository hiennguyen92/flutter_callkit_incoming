// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'call_kit_params.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CallKitParams _$CallKitParamsFromJson(Map json) => $checkedCreate(
      'CallKitParams',
      json,
      ($checkedConvert) {
        final val = CallKitParams(
          id: $checkedConvert('id', (v) => v as String?),
          nameCaller: $checkedConvert('nameCaller', (v) => v as String?),
          appName: $checkedConvert('appName', (v) => v as String?),
          avatar: $checkedConvert('avatar', (v) => v as String?),
          handle: $checkedConvert('handle', (v) => v as String?),
          type: $checkedConvert('type', (v) => v as int?),
          normalHandle: $checkedConvert('normalHandle', (v) => v as int?),
          duration: $checkedConvert('duration', (v) => v as int?),
          textAccept: $checkedConvert('textAccept', (v) => v as String?),
          textDecline: $checkedConvert('textDecline', (v) => v as String?),
          missedCallNotification: $checkedConvert(
              'missedCallNotification',
              (v) => v == null
                  ? null
                  : NotificationParams.fromJson(
                      Map<String, dynamic>.from(v as Map))),
          extra: $checkedConvert(
              'extra',
              (v) => (v as Map?)?.map(
                    (k, e) => MapEntry(k as String, e),
                  )),
          headers: $checkedConvert(
              'headers',
              (v) => (v as Map?)?.map(
                    (k, e) => MapEntry(k as String, e),
                  )),
          android: $checkedConvert(
              'android',
              (v) => v == null
                  ? null
                  : AndroidParams.fromJson(
                      Map<String, dynamic>.from(v as Map))),
          ios: $checkedConvert(
              'ios',
              (v) => v == null
                  ? null
                  : IOSParams.fromJson(Map<String, dynamic>.from(v as Map))),
        );
        return val;
      },
    );

Map<String, dynamic> _$CallKitParamsToJson(CallKitParams instance) =>
    <String, dynamic>{
      'id': instance.id,
      'nameCaller': instance.nameCaller,
      'appName': instance.appName,
      'avatar': instance.avatar,
      'handle': instance.handle,
      'type': instance.type,
      'normalHandle': instance.normalHandle,
      'duration': instance.duration,
      'textAccept': instance.textAccept,
      'textDecline': instance.textDecline,
      'missedCallNotification': instance.missedCallNotification?.toJson(),
      'extra': instance.extra,
      'headers': instance.headers,
      'android': instance.android?.toJson(),
      'ios': instance.ios?.toJson(),
    };
