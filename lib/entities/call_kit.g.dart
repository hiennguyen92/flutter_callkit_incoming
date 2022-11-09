// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'call_kit.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CallKit _$CallKitFromJson(Map json) => $checkedCreate(
      'CallKit',
      json,
      ($checkedConvert) {
        final val = CallKit(
          id: $checkedConvert('id', (v) => v as String),
          nameCaller: $checkedConvert('nameCaller', (v) => v as String?),
          appName: $checkedConvert('appName', (v) => v as String?),
          avatar: $checkedConvert('avatar', (v) => v as String?),
          handle: $checkedConvert('handle', (v) => v as String?),
          type: $checkedConvert('type', (v) => (v as num?)?.toDouble()),
          duration: $checkedConvert('duration', (v) => (v as num?)?.toDouble()),
          textAccept: $checkedConvert('textAccept', (v) => v as String?),
          textDecline: $checkedConvert('textDecline', (v) => v as String?),
          textMissedCall:
              $checkedConvert('textMissedCall', (v) => v as String?),
          textCallback: $checkedConvert('textCallback', (v) => v as String?),
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

Map<String, dynamic> _$CallKitToJson(CallKit instance) => <String, dynamic>{
      'id': instance.id,
      'nameCaller': instance.nameCaller,
      'appName': instance.appName,
      'avatar': instance.avatar,
      'handle': instance.handle,
      'type': instance.type,
      'duration': instance.duration,
      'textAccept': instance.textAccept,
      'textDecline': instance.textDecline,
      'textMissedCall': instance.textMissedCall,
      'textCallback': instance.textCallback,
      'extra': instance.extra,
      'headers': instance.headers,
      'android': instance.android,
      'ios': instance.ios,
    };
