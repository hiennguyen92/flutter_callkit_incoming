// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ios_params.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IOSParams _$IOSParamsFromJson(Map json) => $checkedCreate(
      'IOSParams',
      json,
      ($checkedConvert) {
        final val = IOSParams(
          iconName: $checkedConvert('iconName', (v) => v as String?),
          handleType: $checkedConvert('handleType', (v) => v as String?),
          supportsVideo: $checkedConvert('supportsVideo', (v) => v as bool?),
          maximumCallGroups:
              $checkedConvert('maximumCallGroups', (v) => v as int?),
          maximumCallsPerCallGroup:
              $checkedConvert('maximumCallsPerCallGroup', (v) => v as int?),
          audioSessionMode:
              $checkedConvert('audioSessionMode', (v) => v as String?),
          audioSessionActive:
              $checkedConvert('audioSessionActive', (v) => v as bool?),
          audioSessionPreferredSampleRate: $checkedConvert(
              'audioSessionPreferredSampleRate',
              (v) => (v as num?)?.toDouble()),
          audioSessionPreferredIOBufferDuration: $checkedConvert(
              'audioSessionPreferredIOBufferDuration',
              (v) => (v as num?)?.toDouble()),
          supportsDTMF: $checkedConvert('supportsDTMF', (v) => v as bool?),
          supportsHolding:
              $checkedConvert('supportsHolding', (v) => v as bool?),
          supportsGrouping:
              $checkedConvert('supportsGrouping', (v) => v as bool?),
          supportsUngrouping:
              $checkedConvert('supportsUngrouping', (v) => v as bool?),
          ringtonePath: $checkedConvert('ringtonePath', (v) => v as String?),
        );
        return val;
      },
    );

Map<String, dynamic> _$IOSParamsToJson(IOSParams instance) => <String, dynamic>{
      'iconName': instance.iconName,
      'handleType': instance.handleType,
      'supportsVideo': instance.supportsVideo,
      'maximumCallGroups': instance.maximumCallGroups,
      'maximumCallsPerCallGroup': instance.maximumCallsPerCallGroup,
      'audioSessionMode': instance.audioSessionMode,
      'audioSessionActive': instance.audioSessionActive,
      'audioSessionPreferredSampleRate':
          instance.audioSessionPreferredSampleRate,
      'audioSessionPreferredIOBufferDuration':
          instance.audioSessionPreferredIOBufferDuration,
      'supportsDTMF': instance.supportsDTMF,
      'supportsHolding': instance.supportsHolding,
      'supportsGrouping': instance.supportsGrouping,
      'supportsUngrouping': instance.supportsUngrouping,
      'ringtonePath': instance.ringtonePath,
    };
