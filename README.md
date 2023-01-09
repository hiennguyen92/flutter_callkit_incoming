# Flutter Callkit Incoming

A Flutter plugin to show incoming call in your Flutter app(Custom for Android/Callkit for iOS).

[![pub package](https://img.shields.io/pub/v/flutter_callkit_incoming.svg)](https://pub.dev/packages/flutter_callkit_incoming)
[![Build Status](https://github.com/hiennguyen92/flutter_callkit_incoming/actions/workflows/main.yml/badge.svg)](https://github.com/hiennguyen92/flutter_callkit_incoming/actions/workflows/main.yml)

<a href="https://www.buymeacoffee.com/hiennguyen92" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>

## :star: Features

* Show an incoming call
* Start an outgoing call
* Custom UI Android/Callkit for iOS
* Example using Pushkit/VoIP for iOS

  <br>

## iOS: ONLY WORKING ON REAL DEVICE, not on simulator(Callkit framework not working on simulator)

<br>

## 🚀&nbsp; Installation

1. Install Packages

  * Run this command:
    ```console
    flutter pub add flutter_callkit_incoming
    ```
  * Add pubspec.yaml:
    ```console
        dependencies:
          flutter_callkit_incoming: any
    ```
2. Configure Project
  * Android
     * AndroidManifest.xml
     ```
      <manifest...>
          ...
          <!-- 
              Using for load image from internet
          -->
          <uses-permission android:name="android.permission.INTERNET"/>
      </manifest>
     ```
  * iOS
     * Info.plist
      ```
      <key>UIBackgroundModes</key>
      <array>
          <string>processing</string>
          <string>remote-notification</string>
          <string>voip</string>
      </array>
      ```

3. Usage
  * Import
    ```console
    import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
    ``` 
  * Received an incoming call
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
        textMissedCall: 'Missed call',
        textCallback: 'Call back',
        duration: 30000,
        extra: <String, dynamic>{'userId': '1a2b3c4d'},
        headers: <String, dynamic>{'apiKey': 'Abc@123!', 'platform': 'flutter'},
        android: const AndroidParams(
            isCustomNotification: true,
            isShowLogo: false,
            isShowCallback: false,
            isShowMissedCallNotification: true,
            ringtonePath: 'system_ringtone_default',
            backgroundColor: '#0955fa',
            backgroundUrl: 'https://i.pravatar.cc/500',
            actionColor: '#4CAF50',
            incomingCallNotificationChannelName: "Incoming Call",
            missedCallNotificationChannelName: "Missed Call"),
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
  * Show miss call notification
    ```dart
      this._currentUuid = _uuid.v4();
      CallKitParams params = CallKitParams(
        id: _currentUuid,
        nameCaller: 'Hien Nguyen',
        handle: '0123456789',
        type: 1,
        textMissedCall: 'Missed call',
        textCallback: 'Call back',
        extra: <String, dynamic>{'userId': '1a2b3c4d'},
      );
      await FlutterCallkitIncoming.showMissCallNotification(params);
    ```

  * Started an outgoing call
    ```dart
      this._currentUuid = _uuid.v4();
      CallKitParams params = CallKitParams(
        id: this._currentUuid,
        nameCaller: 'Hien Nguyen',
        handle: '0123456789',
        type: 1,
        extra: <String, dynamic>{'userId': '1a2b3c4d'},
        ios: IOSParams(handleType: 'generic')
      );
      await FlutterCallkitIncoming.startCall(params);
    ```

  * Ended an incoming/outgoing call
    ```dart
      await FlutterCallkitIncoming.endCall(this._currentUuid);
    ```

  * Ended all calls
    ```dart
      await FlutterCallkitIncoming.endAllCalls();
    ```

  * Get active calls. iOS: return active calls from Callkit, Android: only return last call
    ```dart
      await FlutterCallkitIncoming.activeCalls();
    ```
    Output
    ```json
    [{"id": "8BAA2B26-47AD-42C1-9197-1D75F662DF78", ...}]
    ```

  * Get device push token VoIP. iOS: return deviceToken, Android: Empty

    ```dart
      await FlutterCallkitIncoming.getDevicePushTokenVoIP();
    ```
    Output

    ```json
    <device token>

    //Example
    d6a77ca80c5f09f87f353cdd328ec8d7d34e92eb108d046c91906f27f54949cd
    
    ```
    Make sure using `SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP(deviceToken)` inside AppDelegate.swift (<a href="https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/ios/Runner/AppDelegate.swift">Example</a>)
    ```swift
    func pushRegistry(_ registry: PKPushRegistry, didUpdate credentials: PKPushCredentials, for type: PKPushType) {
        print(credentials.token)
        let deviceToken = credentials.token.map { String(format: "%02x", $0) }.joined()
        //Save deviceToken to your server
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP(deviceToken)
    }
    
    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        print("didInvalidatePushTokenFor")
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP("")
    }
    ```


  * Listen events
    ```dart
      FlutterCallkitIncoming.onEvent.listen((CallEvent event) {
        switch (event!.event) {
          case Event.ACTION_CALL_INCOMING:
            // TODO: received an incoming call
            break;
          case Event.ACTION_CALL_START:
            // TODO: started an outgoing call
            // TODO: show screen calling in Flutter
            break;
          case Event.ACTION_CALL_ACCEPT:
            // TODO: accepted an incoming call
            // TODO: show screen calling in Flutter
            break;
          case Event.ACTION_CALL_DECLINE:
            // TODO: declined an incoming call
            break;
          case Event.ACTION_CALL_ENDED:
            // TODO: ended an incoming/outgoing call
            break;
          case Event.ACTION_CALL_TIMEOUT:
            // TODO: missed an incoming call
            break;
          case Event.ACTION_CALL_CALLBACK:
            // TODO: only Android - click action `Call back` from missed call notification
            break;
          case Event.ACTION_CALL_TOGGLE_HOLD:
            // TODO: only iOS
            break;
          case Event.ACTION_CALL_TOGGLE_MUTE:
            // TODO: only iOS
            break;
          case Event.ACTION_CALL_TOGGLE_DMTF:
            // TODO: only iOS
            break;
          case Event.ACTION_CALL_TOGGLE_GROUP:
            // TODO: only iOS
            break;
          case Event.ACTION_CALL_TOGGLE_AUDIO_SESSION:
            // TODO: only iOS
            break;
          case Event.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP:
            // TODO: only iOS
            break;
        }
      });
    ```
  * Call from Native (iOS/Android) 

    ```swift
      //Swift iOS
      var info = [String: Any?]()
      info["id"] = "44d915e1-5ff4-4bed-bf13-c423048ec97a"
      info["nameCaller"] = "Hien Nguyen"
      info["handle"] = "0123456789"
      info["type"] = 1
      //... set more data
      SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(flutter_callkit_incoming.Data(args: info), fromPushKit: true)
    ```
    
    ```kotlin
        //Kotlin/Java Android
        FlutterCallkitIncomingPlugin.getInstance().showIncomingNotification(...)
    ```

    <br>

    ```swift
      //OR
      let data = flutter_callkit_incoming.Data(id: "44d915e1-5ff4-4bed-bf13-c423048ec97a", nameCaller: "Hien Nguyen", handle: "0123456789", type: 0)
      data.nameCaller = "Johnny"
      data.extra = ["user": "abc@123", "platform": "ios"]
      //... set more data
      SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(data, fromPushKit: true)
    ```
    
    <br>

    ```objc
      //Objective-C
      #if __has_include(<flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>)
      #import <flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>
      #else
      #import "flutter_callkit_incoming-Swift.h"
      #endif

      Data * data = [[Data alloc]initWithId:@"44d915e1-5ff4-4bed-bf13-c423048ec97a" nameCaller:@"Hien Nguyen" handle:@"0123456789" type:1];
      [data setNameCaller:@"Johnny"];
      [data setExtra:@{ @"userId" : @"HelloXXXX", @"key2" : @"value2"}];
      //... set more data
      [SwiftFlutterCallkitIncomingPlugin.sharedInstance showCallkitIncoming:data fromPushKit:YES];
    ```
    
    <br>

    ```swift
      //send custom event from native
      SwiftFlutterCallkitIncomingPlugin.sharedInstance?.sendEventCustom("customEvent", body: ["customKey": "customValue"])

    ```

    ```kotlin
        //Kotlin/Java Android
        FlutterCallkitIncomingPlugin.getInstance().sendEventCustom(event: String, body: Map<String, Any>)
    ```

4. Properties

    | Prop            | Description                                                             | Default     |
    | --------------- | ----------------------------------------------------------------------- | ----------- |
    |  **`id`**       | UUID identifier for each call. UUID should be unique for every call and when the call is  ended, the same UUID for that call to be used. suggest using <a href='https://pub.dev/packages/uuid'>uuid.</a> ACCEPT ONLY UUID    | Required    |
    | **`nameCaller`**| Caller's name.                                                          | _None_      |
    | **`appName`**   | App's name. using for display inside Callkit(iOS).                      |   App Name, `Deprecated for iOS > 14, default using App name`  |
    | **`avatar`**    | Avatar's URL used for display for Android. `/android/src/main/res/drawable-xxxhdpi/ic_default_avatar.png`                             |    _None_   |
    | **`handle`**    | Phone number/Email/Any.                                                 |    _None_   |
    |   **`type`**    |  0 - Audio Call, 1 - Video Call                                         |     `0`     |
    | **`duration`**  | Incoming call/Outgoing call display time (second). If the time is over, the call will be missed.                                                                                     |    `30000`  |
   | **`textAccept`**  | Text `Accept` used in Android                                            |    `Accept`  |
   | **`textDecline`**  | Text `Decline` used in Android                                           |    `Decline`  |
   | **`textMissedCall`**  | Text `Missed Call` used in Android (show in miss call notification)  |    `Missed Call`  |
   | **`textCallback`**  | Text `Call back` used in Android (show in miss call notification)     |    `Call back`  |
    |   **`extra`**   | Any data added to the event when received.                              |     `{}`    |
    |   **`headers`** | Any data for custom header avatar/background image.                     |     `{}`    |
    |  **`android`**  | Android data needed to customize UI.                                    |    Below    |
    |    **`ios`**    | iOS data needed.                                                        |    Below    |

    <br>
    
* Android

    | Prop                        | Description                                                             | Default          |
    | --------------------------- | ----------------------------------------------------------------------- | ---------------- |
    | **`isCustomNotification`**  | Using custom notifications.                                             | `false`          |
    | **`isCustomSmallExNotification`**  | Using custom notification small on some devices clipped out in android.                                             | `false`          |
    |       **`isShowLogo`**      | Show logo app inside full screen. `/android/src/main/res/drawable-xxxhdpi/ic_logo.png` | `false`          |
    |       **`isShowMissedCallNotification`**      | Show missed call notification when timeout | `true`          |
    |       **`isShowCallback`**      | Show callback action from miss call notification. | `true`          |
    |      **`ringtonePath`**     | File name ringtone. put file into `/android/app/src/main/res/raw/ringtone_default.pm3`                                                                                                    |`system_ringtone_default` <br>using ringtone default of the phone|
    |     **`backgroundColor`**   | Incoming call screen background color.                                  |     `#0955fa`    |
    |      **`backgroundUrl`**    | Using image background for Incoming call screen. example: http://... https://... or "assets/abc.png"                       |       _None_     |
    |      **`actionColor`**      | Color used in button/text on notification.                              |    `#4CAF50`     |
    |  **`incomingCallNotificationChannelName`** | Notification channel name of incoming call.              | `Incoming call`  |
    |  **`missedCallNotificationChannelName`** | Notification channel name of missed call.                  |  `Missed call`   |

    <br>
    
* iOS

    | Prop                                      | Description                                                             | Default     |
    | ----------------------------------------- | ----------------------------------------------------------------------- | ----------- |
    |               **`iconName`**              | App's Icon. using for display inside Callkit(iOS)                       | `CallKitLogo` <br> using from `Images.xcassets/CallKitLogo`    |
    |              **`handleType`**             | Type handle call `generic`, `number`, `email`                           | `generic`   |
    |             **`supportsVideo`**           |                                                                         |   `true`    |
    |          **`maximumCallGroups`**          |                                                                         |     `2`     |
    |       **`maximumCallsPerCallGroup`**      |                                                                         |     `1`     |
    |           **`audioSessionMode`**          |                                                                         |   _None_, `gameChat`, `measurement`, `moviePlayback`, `spokenAudio`, `videoChat`, `videoRecording`, `voiceChat`, `voicePrompt`  |
    |        **`audioSessionActive`**           |                                                                         |    `true`   |
    |   **`audioSessionPreferredSampleRate`**   |                                                                         |  `44100.0`  |
    |**`audioSessionPreferredIOBufferDuration`**|                                                                         |  `0.005`    |
    |            **`supportsDTMF`**             |                                                                         |    `true`   |
    |            **`supportsHolding`**          |                                                                         |    `true`   |
    |          **`supportsGrouping`**           |                                                                         |    `true`   |
    |         **`supportsUngrouping`**          |                                                                         |   `true`    |
    |           **`ringtonePath`**              | Add file to root project xcode  `/ios/Runner/Ringtone.caf`  and Copy Bundle Resources(Build Phases)                                                                                                               |`Ringtone.caf`<br>`system_ringtone_default` <br>using ringtone default of the phone|


5. Source code

    ```
    please checkout repo github
    https://github.com/hiennguyen92/flutter_callkit_incoming
    ```
    * <a href='https://github.com/hiennguyen92/flutter_callkit_incoming'>https://github.com/hiennguyen92/flutter_callkit_incoming</a>
    * <a href='https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/example/lib/main.dart'>Example</a>

  <br>

6. Pushkit - Received VoIP and Wake app from Terminated State (only for IOS)
  * Please check <a href="https://github.com/hiennguyen92/flutter_callkit_incoming/blob/master/PUSHKIT.md">PUSHKIT.md</a> setup Pushkit for IOS

  <br>

7. Todo
  * Run background
  * Simplify the setup process

    <br>

## :bulb: Demo

1. Demo Illustration: 
2. Image
<table>
  <tr>
    <td>iOS(Lockscreen)</td>
    <td>iOS(full screen)</td>
    <td>iOS(Alert)</td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image1.png" width="220">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image2.png" width="220">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image3.png" width="220">
    </td>
  </tr>
  <tr>
    <td>Android(Lockscreen) - Audio</td>
    <td>Android(Alert) - Audio</td>
    <td>Android(Lockscreen) - Video</td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image4.jpg" width="220">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image5.jpg" width="220">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image6.jpg" width="220">
    </td>
  </tr>
  <tr>
    <td>Android(Alert) - Video</td>
    <td>isCustomNotification: false</td>
    <td></td>
  </tr>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image7.jpg" width="220">
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/hiennguyen92/flutter_callkit_incoming/master/images/image8.jpg" width="220">
    </td>
  </tr>
 </table>
