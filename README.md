# Flutter Callkit Incoming

A Flutter plugin to show incoming call in your Flutter app (Custom for Android/Callkit for iOS).

[![pub package](https://img.shields.io/pub/v/flutter_callkit_incoming.svg)](https://pub.dev/packages/flutter_callkit_incoming)
[![pub points](https://img.shields.io/pub/points/flutter_callkit_incoming?label=pub%20points)](https://pub.dev/packages/flutter_callkit_incoming/score)
[![GitHub stars](https://img.shields.io/github/stars/hiennguyen92/flutter_callkit_incoming.svg?style=social)](https://github.com/hiennguyen92/flutter_callkit_incoming/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/hiennguyen92/flutter_callkit_incoming.svg?style=social)](https://github.com/hiennguyen92/flutter_callkit_incoming/network)
[![GitHub license](https://img.shields.io/github/license/hiennguyen92/flutter_callkit_incoming.svg)](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/LICENSE)
[![Build Status](https://github.com/hiennguyen92/flutter_callkit_incoming/actions/workflows/main.yml/badge.svg)](https://github.com/hiennguyen92/flutter_callkit_incoming/actions/workflows/main.yml)

## Sponsors

Our top sponsors are shown below!

<a href="https://getstream.io/video/sdk/flutter/tutorial/video-calling/?utm_source=Github&utm_medium=Github_Repo_Content_Ad&utm_content=Developer&utm_campaign=Github_Video&utm_term=flutter_callkit" target="_blank">
  <img width="250px" src="https://stream-blog.s3.amazonaws.com/blog/wp-content/uploads/fc148f0fc75d02841d017bb36e14e388/Stream-logo-with-background-.png"/>
</a>
<br/>
<span>
  <a href="https://getstream.io/video/sdk/flutter/tutorial/video-calling/?utm_source=Github&utm_medium=Github_Repo_Content_Ad&utm_content=Developer&utm_campaign=Github_Video&utm_term=flutter_callkit" target="_blank">Try the Flutter Video Tutorial 📹</a>
</span>
</br>
</br>
<a href="https://www.buymeacoffee.com/hiennguyen92" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174">
</a>

## ⭐ Features

- Show an incoming call
- Start an outgoing call
- Custom UI Android/Callkit for iOS
- Example using Pushkit/VoIP for iOS

## ⚠️ iOS: ONLY WORKING ON REAL DEVICE

**Please make sure setup/using [PUSHKIT](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/PUSHKIT.md) FOR VOIP**

> **Note:** Please do not use on simulator (Callkit framework not working on simulator)

## 🚀 Installation

### 1. Install Packages

For version >= v2.5.0, please make sure install and use Java SDK version >= 17 (Android)

**Run this command:**
```bash
flutter pub add flutter_callkit_incoming
```

**Or add to pubspec.yaml:**
```yaml
dependencies:
  flutter_callkit_incoming: ^latest
```

### 2. Configure Project

#### Android

**AndroidManifest.xml:**
```xml
<manifest...>
    ...
    <!-- Using for load image from internet -->
    <uses-permission android:name="android.permission.INTERNET"/>

    <application ...>
        <activity ...
            android:name=".MainActivity"
            android:launchMode="singleInstance"><!-- add this -->
        ...
    </application>
</manifest>
```

**Proguard Rules:**
The following rule needs to be added in the `proguard-rules.pro` to avoid obfuscated keys:
```
-keep class com.hiennv.flutter_callkit_incoming.** { *; }
```

#### iOS

**Info.plist:**
```xml
<key>UIBackgroundModes</key>
<array>
    <string>voip</string>
    <string>remote-notification</string>
    <string>processing</string> <!-- you can add this if needed -->
</array>
```

### 3. Usage

#### Import
```dart
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
```

#### Show Incoming Call
```dart
this._currentUuid = _uuid.v4();
CallKitParams callKitParams = CallKitParams(
  id: _currentUuid,
  nameCaller: 'Hien Nguyen',
  appName: 'Callkit',
  avatar: 'https://i.pravatar.cc/100',
  handle: '0123456789',
  type: 0,
  textAccept: 'Accept',
  textDecline: 'Decline',
  missedCallNotification: NotificationParams(
    showNotification: true,
    isShowCallback: true,
    subtitle: 'Missed call',
    callbackText: 'Call back',
  ),
  callingNotification: const NotificationParams(
    showNotification: true,
    isShowCallback: true,
    subtitle: 'Calling...',
    callbackText: 'Hang Up',
  ),
  duration: 30000,
  extra: <String, dynamic>{'userId': '1a2b3c4d'},
  headers: <String, dynamic>{'apiKey': 'Abc@123!', 'platform': 'flutter'},
  android: const AndroidParams(
    isCustomNotification: true,
    isShowLogo: false,
    logoUrl: 'https://i.pravatar.cc/100',
    ringtonePath: 'system_ringtone_default',
    backgroundColor: '#0955fa',
    backgroundUrl: 'https://i.pravatar.cc/500',
    actionColor: '#4CAF50',
    textColor: '#ffffff',
    incomingCallNotificationChannelName: "Incoming Call",
    missedCallNotificationChannelName: "Missed Call",
    isShowCallID: false
  ),
  ios: IOSParams(
    iconName: 'CallKitLogo',
    handleType: 'generic',
    supportsVideo: true,
    maximumCallGroups: 2,
    maximumCallsPerCallGroup: 1,
    audioSessionMode: 'default',
    audioSessionActive: true,
    audioSessionPreferredSampleRate: 44100.0,
    audioSessionPreferredIOBufferDuration: 0.005,
    supportsDTMF: true,
    supportsHolding: true,
    supportsGrouping: false,
    supportsUngrouping: false,
    ringtonePath: 'system_ringtone_default',
  ),
);
await FlutterCallkitIncoming.showCallkitIncoming(callKitParams);
```

> **Note:** For Firebase Message: `@pragma('vm:entry-point')`  
> https://github.com/firebase/flutterfire/blob/master/docs/cloud-messaging/receive.md#apple-platforms-and-android

#### Request Notification Permission (Android 13+/iOS)

For Android 13+, please `requestNotificationPermission` or requestPermission of firebase_messaging before `showCallkitIncoming`:

```dart
await FlutterCallkitIncoming.requestNotificationPermission({
  "title": "Notification permission",
  "rationaleMessagePermission": "Notification permission is required, to show notification.",
  "postNotificationMessageRequired": "Notification permission is required, Please allow notification permission from setting."
});
```

#### Request Full Intent Permission (Android 14+)

For Android 14+, please use `canUseFullScreenIntent` and `requestFullIntentPermission`:

```dart
// Check if can use full screen intent
await FlutterCallkitIncoming.canUseFullScreenIntent();

// Request full intent permission
await FlutterCallkitIncoming.requestFullIntentPermission();
```

#### Show Missed Call Notification
```dart
this._currentUuid = _uuid.v4();
CallKitParams params = CallKitParams(
  id: _currentUuid,
  nameCaller: 'Hien Nguyen',
  handle: '0123456789',
  type: 1,
  missedCallNotification: const NotificationParams(
    showNotification: true,
    isShowCallback: true,
    subtitle: 'Missed call',
    callbackText: 'Call back',
  ),
  android: const AndroidParams(
    isCustomNotification: true,
    isShowCallID: true,
  ),
  extra: <String, dynamic>{'userId': '1a2b3c4d'},
);
await FlutterCallkitIncoming.showMissCallNotification(params);
```

#### Hide Call Notification (Android)
```dart
CallKitParams params = CallKitParams(
  id: _currentUuid,
);
await FlutterCallkitIncoming.hideCallkitIncoming(params);
```

#### Start Outgoing Call
```dart
this._currentUuid = _uuid.v4();
CallKitParams params = CallKitParams(
  id: this._currentUuid,
  nameCaller: 'Hien Nguyen',
  handle: '0123456789',
  type: 1,
  extra: <String, dynamic>{'userId': '1a2b3c4d'},
  ios: IOSParams(handleType: 'generic'),
  callingNotification: const NotificationParams(
    showNotification: true,
    isShowCallback: true,
    subtitle: 'Calling...',
    callbackText: 'Hang Up',
  ),
  android: const AndroidParams(
    isCustomNotification: true,
    isShowCallID: true,
  )
);
await FlutterCallkitIncoming.startCall(params);
```

#### End Call
```dart
// End specific call
await FlutterCallkitIncoming.endCall(this._currentUuid);

// End all calls
await FlutterCallkitIncoming.endAllCalls();
```

#### Get Active Calls

iOS: returns active calls from Callkit (only id), Android: only returns last call

```dart
await FlutterCallkitIncoming.activeCalls();
```

**Output:**
```json
[{"id": "8BAA2B26-47AD-42C1-9197-1D75F662DF78", ...}]
```

#### Set Call Connected

Used to determine Incoming Call or Outgoing Call status in phone book(reset/start timer):

```dart
await FlutterCallkitIncoming.setCallConnected(this._currentUuid);
```

> **Note:** After the call is ACCEPT or startCall, please call this function. Normally it should be called when WebRTC/P2P is established.

#### Get Device Push Token VoIP

iOS: returns deviceToken, Android: returns none

```dart
await FlutterCallkitIncoming.getDevicePushTokenVoIP();
```

**Output:**
```
d6a77ca80c5f09f87f353cdd328ec8d7d34e92eb108d046c91906f27f54949cd
```

> **Important:** Make sure using `SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP(deviceToken)` inside AppDelegate.swift ([Example](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/ios/Runner/AppDelegate.swift))

```swift
func pushRegistry(_ registry: PKPushRegistry, didUpdate credentials: PKPushCredentials, for type: PKPushType) {
    print(credentials.token)
    let deviceToken = credentials.token.map { String(format: "%02x", $0) }.joined()
    // Save deviceToken to your server
    SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP(deviceToken)
}

func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
    print("didInvalidatePushTokenFor")
    SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP("")
}
```

#### Listen Events
```dart
FlutterCallkitIncoming.onEvent.listen((CallEvent event) {
  switch (event!.event) {
    case Event.actionCallIncoming:
      // TODO: received an incoming call
      break;
    case Event.actionCallStart:
      // TODO: started an outgoing call
      // TODO: show screen calling in Flutter
      break;
    case Event.actionCallAccept:
      // TODO: accepted an incoming call
      // TODO: show screen calling in Flutter
      break;
    case Event.actionCallDecline:
      // TODO: declined an incoming call
      break;
    case Event.actionCallEnded:
      // TODO: ended an incoming/outgoing call
      break;
    case Event.actionCallTimeout:
      // TODO: missed an incoming call
      break;
    case Event.actionCallCallback:
      // TODO: click action `Call back` from missed call notification
      break;
    case Event.actionCallToggleHold:
      // TODO: only iOS
      break;
    case Event.actionCallToggleMute:
      // TODO: only iOS
      break;
    case Event.actionCallToggleDmtf:
      // TODO: only iOS
      break;
    case Event.actionCallToggleGroup:
      // TODO: only iOS
      break;
    case Event.actionCallToggleAudioSession:
      // TODO: only iOS
      break;
    case Event.actionDidUpdateDevicePushTokenVoip:
      // TODO: only iOS
      break;
    case Event.actionCallCustom:
      // TODO: for custom action
      break;
  }
});
```

#### Call from Native (iOS/Android)

**Swift (iOS):**
```swift
var info = [String: Any?]()
info["id"] = "44d915e1-5ff4-4bed-bf13-c423048ec97a"
info["nameCaller"] = "Hien Nguyen"
info["handle"] = "0123456789"
info["type"] = 1
// ... set more data
SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(flutter_callkit_incoming.Data(args: info), fromPushKit: true)

// Please make sure call `completion()` at the end of the pushRegistry(......, completion: @escaping () -> Void)
// or `DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { completion() }`
// if you don't call completion() in pushRegistry(......, completion: @escaping () -> Void), there may be app crash by system when receiving VoIP
```

**Kotlin (Android):**
```kotlin
FlutterCallkitIncomingPlugin.getInstance().showIncomingNotification(...)
```

**Alternative Swift approach:**
```swift
let data = flutter_callkit_incoming.Data(id: "44d915e1-5ff4-4bed-bf13-c423048ec97a", nameCaller: "Hien Nguyen", handle: "0123456789", type: 0)
data.nameCaller = "Johnny"
data.extra = ["user": "abc@123", "platform": "ios"]
// ... set more data
SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(data, fromPushKit: true)
```

**Objective-C:**
```objc
#if __has_include(<flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>)
#import <flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>
#else
#import "flutter_callkit_incoming-Swift.h"
#endif

Data * data = [[Data alloc]initWithId:@"44d915e1-5ff4-4bed-bf13-c423048ec97a" nameCaller:@"Hien Nguyen" handle:@"0123456789" type:1];
[data setNameCaller:@"Johnny"];
[data setExtra:@{ @"userId" : @"HelloXXXX", @"key2" : @"value2"}];
// ... set more data
[SwiftFlutterCallkitIncomingPlugin.sharedInstance showCallkitIncoming:data fromPushKit:YES];
```

**Send Custom Event from Native:**

**Swift:**
```swift
SwiftFlutterCallkitIncomingPlugin.sharedInstance?.sendEventCustom(body: ["customKey": "customValue"])
```

**Kotlin:**
```kotlin
FlutterCallkitIncomingPlugin.getInstance().sendEventCustom(body: Map<String, Any>)
```

#### Call API when Accept/Decline/End/Timeout
#### Setup for Missed call notification(iOS)

**AppDelegate.swift:**
```swift
@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate, PKPushRegistryDelegate, CallkitIncomingAppDelegate {
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        
        // Setup VOIP
        let mainQueue = DispatchQueue.main
        let voipRegistry: PKPushRegistry = PKPushRegistry(queue: mainQueue)
        voipRegistry.delegate = self
        voipRegistry.desiredPushTypes = [PKPushType.voIP]

        // Use if using WebRTC
        // RTCAudioSession.sharedInstance().useManualAudio = true
        // RTCAudioSession.sharedInstance().isAudioEnabled = false

        //Add for Missed call notification
        if #available(iOS 10.0, *) {
          UNUserNotificationCenter.current().delegate = self as UNUserNotificationCenterDelegate
        }
        
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }

    // Add for Missed call notification(show notification when foreground)
    override func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler:
                                   @escaping (UNNotificationPresentationOptions) -> Void) {
        
        CallkitNotificationManager.shared.userNotificationCenter(center, willPresent: notification, withCompletionHandler: completionHandler)
    }
    
    // Add for Missed call notification(action when click callback in missed notification)
    override func userNotificationCenter(_ center: UNUserNotificationCenter,
                                         didReceive response: UNNotificationResponse,
                                         withCompletionHandler completionHandler: @escaping () -> Void) {
        if response.actionIdentifier == CallkitNotificationManager.CALLBACK_ACTION {
            let data = response.notification.request.content.userInfo as? [String: Any]
            SwiftFlutterCallkitIncomingPlugin.sharedInstance?.sendCallbackEvent(data)
        }
        completionHandler()
    }

    // Func Call API for Accept
    func onAccept(_ call: Call, _ action: CXAnswerCallAction) {
        let json = ["action": "ACCEPT", "data": call.data.toJSON()] as [String: Any]
        print("LOG: onAccept")
        self.performRequest(parameters: json) { result in
            switch result {
            case .success(let data):
                print("Received data: \(data)")
                // Make sure call action.fulfill() when you are done (connected WebRTC - Start counting seconds)
                action.fulfill()
            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
    }
    
    // Func Call API for Decline
    func onDecline(_ call: Call, _ action: CXEndCallAction) {
        let json = ["action": "DECLINE", "data": call.data.toJSON()] as [String: Any]
        print("LOG: onDecline")
        self.performRequest(parameters: json) { result in
            switch result {
            case .success(let data):
                print("Received data: \(data)")
                // Make sure call action.fulfill() when you are done
                action.fulfill()
            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
    }
    
    // Func Call API for End
    func onEnd(_ call: Call, _ action: CXEndCallAction) {
        let json = ["action": "END", "data": call.data.toJSON()] as [String: Any]
        print("LOG: onEnd")
        self.performRequest(parameters: json) { result in
            switch result {
            case .success(let data):
                print("Received data: \(data)")
                // Make sure call action.fulfill() when you are done
                action.fulfill()
            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
    }
    
    func onTimeOut(_ call: Call) {
        let json = ["action": "TIMEOUT", "data": call.data.toJSON()] as [String: Any]
        print("LOG: onTimeOut")
        self.performRequest(parameters: json) { result in
            switch result {
            case .success(let data):
                print("Received data: \(data)")
            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
    }

    func didActivateAudioSession(_ audioSession: AVAudioSession) {
        // Use if using WebRTC
        // RTCAudioSession.sharedInstance().audioSessionDidActivate(audioSession)
        // RTCAudioSession.sharedInstance().isAudioEnabled = true
    }
    
    func didDeactivateAudioSession(_ audioSession: AVAudioSession) {
        // Use if using WebRTC
        // RTCAudioSession.sharedInstance().audioSessionDidDeactivate(audioSession)
        // RTCAudioSession.sharedInstance().isAudioEnabled = false
    }
}
```

> **Please check full example:** [Example](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/ios/Runner/AppDelegate.swift)

**MainActivity.kt:**
```kotlin
class MainActivity: FlutterActivity(){

    private var callkitEventCallback = object: CallkitEventCallback{
        override fun onCallEvent(event: CallkitEventCallback.CallEvent, callData: Bundle) {
            when (event) {
                CallkitEventCallback.CallEvent.ACCEPT -> {
                    // Do something with answer
                }
                CallkitEventCallback.CallEvent.DECLINE -> {
                    // Do something with decline
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlutterCallkitIncomingPlugin.registerEventCallback(callkitEventCallback)
    }

    override fun onDestroy() {
        FlutterCallkitIncomingPlugin.unregisterEventCallback(callkitEventCallback)
        super.onDestroy()
    }


}
```

> **Please check full example:** [Example](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/android/app/src/main/kotlin/com/example/flutter_callkit_incoming_example/MainActivity.kt
)

## 📋 Properties

### Main Properties

| Property | Description | Default |
|----------|-------------|---------|
| **`id`** | UUID identifier for each call. UUID should be unique for every call and when the call is ended, the same UUID for that call to be used. Suggest using [uuid](https://pub.dev/packages/uuid). ACCEPT ONLY UUID | Required |
| **`nameCaller`** | Caller's name | _None_ |
| **`appName`** | App's name. Used for display inside Callkit (iOS). | App Name, `Deprecated for iOS > 14, default using App name` |
| **`avatar`** | Avatar's URL used for display for Android. `/android/src/main/res/drawable-xxxhdpi/ic_default_avatar.png` | _None_ |
| **`handle`** | Phone number/Email/Any | _None_ |
| **`type`** | 0 - Audio Call, 1 - Video Call | `0` |
| **`duration`** | Incoming call/Outgoing call display time (second). If the time is over, the call will be missed | `30000` |
| **`textAccept`** | Text `Accept` used in Android | `Accept` |
| **`textDecline`** | Text `Decline` used in Android | `Decline` |
| **`extra`** | Any data added to the event when received | `{}` |
| **`headers`** | Any data for custom header avatar/background image | `{}` |
| **`missedCallNotification`** | Android data needed to customize Missed Call Notification | Below |
| **`callingNotification`** | Android data needed to customize Calling Notification | Below |
| **`android`** | Android data needed to customize UI | Below |
| **`ios`** | iOS data needed | Below |

### Missed Call Notification

| Property | Description | Default |
|----------|-------------|---------|
| **`subtitle`** | Text `Missed Call` used in Android/iOS (show in missed call notification) | `Missed Call` |
| **`callbackText`** | Text `Call back` used in Android/iOS (show in missed call notification action) | `Call back` |
| **`showNotification`** | Show missed call notification when timeout | `true` |
| **`isShowCallback`** | Show callback action from missed call notification | `true` |

### Calling Notification

| Property | Description | Default |
|----------|-------------|---------|
| **`subtitle`** | Text used in Android (show in calling notification) | `Calling...` |
| **`callbackText`** | Text used in Android (show in calling notification action) | `Hang Up` |
| **`showNotification`** | Show calling notification when start call/accept call | `true` |
| **`isShowCallback`** | Show hang up action from calling notification | `true` |

### Android

| Property | Description | Default |
|----------|-------------|---------|
| **`isCustomNotification`** | Using custom notifications | `false` |
| **`isCustomSmallExNotification`** | Using custom notification small on some devices clipped out in Android | `false` |
| **`isShowLogo`** | Show logo app inside full screen. `/android/src/main/res/drawable-xxxhdpi/ic_logo.png` | `false` |
| **`logoUrl`** | Logo app inside full screen. Example: http://... https://... or "assets/abc.png" | _None_ |
| **`ringtonePath`** | File name of a ringtone ex: `ringtone_default`. Put file into `/android/app/src/main/res/raw/ringtone_default.mp3` | `system_ringtone_default` <br>using ringtone default of the phone |
| **`backgroundColor`** | Incoming call screen background color | `#0955fa` |
| **`backgroundUrl`** | Using image background for Incoming call screen. Example: http://... https://... or "assets/abc.png" | _None_ |
| **`actionColor`** | Color used in button/text on notification | `#4CAF50` |
| **`textColor`** | Color used for the text in full screen notification | `#ffffff` |
| **`incomingCallNotificationChannelName`** | Notification channel name of incoming call | `Incoming call` |
| **`missedCallNotificationChannelName`** | Notification channel name of missed call | `Missed call` |
| **`isShowCallID`** | Show call id app inside full screen/notification | `false` |
| **`isShowFullLockedScreen`** | Show full screen on Locked Screen (please make sure call `requestFullIntentPermission` for Android 14+) | `true` |

### iOS

| Property | Description | Default |
|----------|-------------|---------|
| **`iconName`** | App's Icon. Used for display inside Callkit (iOS) | `CallKitLogo` <br> using from `Images.xcassets/CallKitLogo` |
| **`handleType`** | Type handle call `generic`, `number`, `email`, Recommended to use `generic` for more reasonable callkit display | `generic` |
| **`supportsVideo`** | | `true` |
| **`maximumCallGroups`** | | `2` |
| **`maximumCallsPerCallGroup`** | | `1` |
| **`audioSessionMode`** | | _None_, `gameChat`, `measurement`, `moviePlayback`, `spokenAudio`, `videoChat`, `videoRecording`, `voiceChat`, `voicePrompt` |
| **`audioSessionActive`** | | `true` |
| **`audioSessionPreferredSampleRate`** | | `44100.0` |
| **`audioSessionPreferredIOBufferDuration`** | | `0.005` |
| **`supportsDTMF`** | | `true` |
| **`supportsHolding`** | | `true` |
| **`supportsGrouping`** | | `true` |
| **`supportsUngrouping`** | | `true` |
| **`ringtonePath`** | Add file to root project xcode `/ios/Runner/Ringtone.caf` and Copy Bundle Resources (Build Phases) | `Ringtone.caf`<br>`system_ringtone_default` <br>using ringtone default of the phone |

## 📁 Source Code

Please checkout repo GitHub:
- [https://github.com/hiennguyen92/flutter_callkit_incoming](https://github.com/hiennguyen92/flutter_callkit_incoming)
- [Example](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/lib/main.dart)

## 📱 Pushkit - Received VoIP and Wake App from Terminated State (iOS Only)

Please check [PUSHKIT.md](https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/PUSHKIT.md) for setup Pushkit for iOS.

## 📋 Todo

- [ ] Run background
- [ ] Simplify the setup process
- [X] Custom notification for iOS (Missing notification)
- [X] Keep notification when calling

## 🎯 Demo

### Demo Illustration

### Images

<table>
  <tr>
    <td><strong>iOS (Lockscreen)</strong></td>
    <td><strong>iOS (Full Screen)</strong></td>
    <td><strong>iOS (Alert)</strong></td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image1.png" width="220" alt="iOS Lockscreen">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image2.png" width="220" alt="iOS Full Screen">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image3.png" width="220" alt="iOS Alert">
    </td>
  </tr>
  <tr>
    <td><strong>Android (Lockscreen) - Audio</strong></td>
    <td><strong>Android (Alert) - Audio</strong></td>
    <td><strong>Android (Lockscreen) - Video</strong></td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image4.jpg" width="220" alt="Android Lockscreen Audio">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image5.jpg" width="220" alt="Android Alert Audio">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image6.jpg" width="220" alt="Android Lockscreen Video">
    </td>
  </tr>
  <tr>
    <td><strong>Android (Alert) - Video</strong></td>
    <td><strong>isCustomNotification: false</strong></td>
    <td></td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image7.jpg" width="220" alt="Android Alert Video">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image8.jpg" width="220" alt="Custom Notification False">
    </td>
    <td></td>
  </tr>
</table> 