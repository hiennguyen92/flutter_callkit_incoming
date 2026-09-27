/// Audio route of an ongoing call.
///
/// Values mirror the `android.telecom.CallAudioState.ROUTE_*` constants, which
/// is what the platform channel carries. Only used on Android, and only for
/// calls hosted in Telecom as self-managed connections.
enum AudioRoute {
  /// Earpiece — also what "speaker off" means: when a Bluetooth or wired
  /// headset is connected the call goes back to that device instead.
  earpiece(1),

  /// Loudspeaker.
  speaker(8),

  /// Bluetooth (SCO) headset.
  bluetooth(2),

  /// Wired headset.
  wiredHeadset(4);

  const AudioRoute(this.value);

  /// `android.telecom.CallAudioState.ROUTE_*` constant for this route.
  final int value;
}
