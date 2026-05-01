# PATHB-V2 Fork Modifications

SnowChat fork of flutter_callkit_incoming includes Path B v2/v4 native dedup
infrastructure for iOS PushKit cold-launch race resolution. Upstream PR
candidate after production validation.

## Files modified

- `ios/Classes/SwiftFlutterCallkitIncomingPlugin.swift`
- `lib/entities/call_event.dart`

## Summary

| Layer | Change | Purpose |
|-------|--------|---------|
| Swift | `Set<String> activeCallKitUUIDs` + `NSLock` | Track CallKit UUIDs across PushKit/CXProvider/Dart-MethodChannel queues |
| Swift | 4 helpers: `isCallKitUUIDActive`, `addActiveCallKitUUID`, `removeActiveCallKitUUID`, `clearAllActiveCallKitUUIDs` | Atomic Set operations |
| Swift | Both `showCallkitIncoming` variants (lines ~290 + ~335) populate the Set on `reportNewIncomingCall` success | Native-side dedup primary path |
| Swift | 7 cleanup sites with `removeActiveCallKitUUID` / `clearAllActiveCallKitUUIDs` | Lifecycle-bound state cleanup |
| Swift | `addActiveCallKitUUID` invokes `sendEvent("PATHB_V4_CALLKIT_SHOWN", {callId})` on main thread | F3 audioplayers stop hook (notify Dart) |
| Dart | `Event.actionPathbV4CallkitShown` enum case + name = `"PATHB_V4_CALLKIT_SHOWN"` | Routes native event through plugin's `_receiveCallEvent` |

## Cleanup site enumeration (§3.5.1)

| # | Method | Trigger | Cleanup call |
|---|--------|---------|--------------|
| 1 | `endCall(_:)` | Dart-initiated end | `removeActiveCallKitUUID(uuidSourceString)` |
| 2 | `provider(_:perform answerCall:)` | User accepts | `removeActiveCallKitUUID(call.uuid.uuidString)` |
| 3a | `provider(_:perform endCall:)` UUID-not-found guard | Stale UUID end | `removeActiveCallKitUUID(action.callUUID.uuidString)` |
| 3b | `provider(_:perform endCall:)` removeCall path | User declines / ends | `removeActiveCallKitUUID(call.uuid.uuidString)` |
| 4 | `callEndTimeout(_:)` | 60s ringer auto-dismiss | `removeActiveCallKitUUID(data.uuid)` (defensive, post-UUID-guard) |
| 5 | `saveEndCall(_:_:)` | App-side end with reason | `removeActiveCallKitUUID(uuid)` (post switch reason) |
| 6 | `providerDidReset(_:)` | CallKit subsystem reset | `clearAllActiveCallKitUUIDs()` |
| 7 | `endAllCalls()` | Dart logout / app reset | `clearAllActiveCallKitUUIDs()` |

## Bridge for Dart query

Native `isCallKitUUIDActive(_ uuid:)` is exposed to Dart via host app's
`MethodChannel("snowchat/voip_native")` handler in `app/ios/Runner/VoipNativeBridge.swift`:

```swift
case "isCallActive":
    let uuid = args["uuid"] as? String ?? ""
    let active = SwiftFlutterCallkitIncomingPlugin.sharedInstance?
        .isCallKitUUIDActive(uuid) ?? false
    result(active)
```

## F3 audioplayers stop hook flow

```
Native showCallkitIncoming success
  → addActiveCallKitUUID(uuid)
  → DispatchQueue.main.async {
       self.sendEvent("PATHB_V4_CALLKIT_SHOWN", ["callId": uuid])
     }
  → plugin EventChannel sink (`flutter_callkit_incoming_events`)
  → plugin Dart `_receiveCallEvent` maps to `Event.actionPathbV4CallkitShown`
  → host CallKitManager._bindPackageEvents() detects, broadcasts onCallKitShown stream
  → CallNotifier subscribes, forwards to CallService.onCallKitShown(callId)
  → if _currentRingtoneCallId matches → _stopRingtone()
```

## Backward compat

- Other apps using this fork that don't subscribe to `Event.actionPathbV4CallkitShown`
  see it as an unknown event — `Event.values.firstWhere` matches by name so the
  unknown event delivers an `Event.actionPathbV4CallkitShown` instance which
  their switch-case ignores (no crash, no behavior change for the Set state itself).
- `addActiveCallKitUUID` / `removeActiveCallKitUUID` are private — no API surface change.
- `isCallKitUUIDActive` / `clearAllActiveCallKitUUIDs` are `@objc public` for host bridge access.

## Upstream PR candidate

Refactor as opt-in feature flag (PluginInitializerOptions) for upstream merge.
SnowChat-specific F3 hook (`PATHB_V4_CALLKIT_SHOWN` event + audioplayers stop) is
NOT upstream-suitable — too app-specific. Native dedup Set itself IS upstream-suitable
since CXProvider duplicate-report rejection logging is a generic concern.
