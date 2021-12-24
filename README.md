# Flutter Callkit Incoming

A Flutter plugin to show incoming call in your Flutter app(Custom for Android/Callkit for iOS).

## :star: Features

* Show an incoming call
* Start an outgoing call
* Custom UI Android/Callkit for iOS

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
      var params = <String, dynamic>{
        'id': _currentUuid,
        'nameCaller': 'Hien Nguyen',
        'appName': 'Callkit',
        'avatar': 'https://i.pravatar.cc/100',
        'handle': '0123456789',
        'type': 0,
        'duration': 30000,
        'extra': <String, dynamic>{'userId': '1a2b3c4d'},
        'headers': <String, dynamic>{'apiKey': 'Abc@123!', 'platform': 'flutter'},
        'android': <String, dynamic>{
          'isCustomNotification': true,
          'isShowLogo': false,
          'ringtonePath': 'ringtone_default',
          'backgroundColor': '#0955fa',
          'backgroundUrl': 'https://i.pravatar.cc/500',
          'actionColor': '#4CAF50'
        },
        'ios': <String, dynamic>{
          'iconName': 'AppIcon40x40',
          'handleType': 'generic',
          'supportsVideo': true,
          'maximumCallGroups': 2,
          'maximumCallsPerCallGroup': 1,
          'audioSessionMode': 'default',
          'audioSessionActive': true,
          'audioSessionPreferredSampleRate': 44100.0,
          'audioSessionPreferredIOBufferDuration': 0.005,
          'supportsDTMF': true,
          'supportsHolding': true,
          'supportsGrouping': false,
          'supportsUngrouping': false,
          'ringtonePath': 'Ringtone.caf'
        }
      };
      await FlutterCallkitIncoming.showCallkitIncoming(params);
    ```

  * Started an outgoing call
    ```dart
      this._currentUuid = _uuid.v4();
      var params = <String, dynamic>{
        'id': this._currentUuid,
        'nameCaller': 'Hien Nguyen',
        'handle': '0123456789',
        'type': 1,
        'extra': <String, dynamic>{'userId': '1a2b3c4d'},
        'ios': <String, dynamic>{'handleType': 'generic'}
      };
      await FlutterCallkitIncoming.startCall(params);
    ```

  * Ended an incoming/outgoing call
    ```dart
      var params = <String, dynamic>{'id': this._currentUuid};
      await FlutterCallkitIncoming.endCall(params);
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

  * Listen events
    ```dart
      FlutterCallkitIncoming.onEvent.listen((event) {
        switch (event!.name) {
          case CallEvent.ACTION_CALL_INCOMING:
            // TODO: received an incoming call
            break;
          case CallEvent.ACTION_CALL_START:
            // TODO: started an outgoing call
            // TODO: show screen calling in Flutter
            break;
          case CallEvent.ACTION_CALL_ACCEPT:
            // TODO: accepted an incoming call
            // TODO: show screen calling in Flutter
            break;
          case CallEvent.ACTION_CALL_DECLINE:
            // TODO: declined an incoming call
            break;
          case CallEvent.ACTION_CALL_ENDED:
            // TODO: ended an incoming/outgoing call
            break;
          case CallEvent.ACTION_CALL_TIMEOUT:
            // TODO: missed an incoming call
            break;
          case CallEvent.ACTION_CALL_CALLBACK:
            // TODO: only Android - click action `Call back` from missed call notification
            break;
          case CallEvent.ACTION_CALL_TOGGLE_HOLD:
            // TODO: only iOS
            break;
          case CallEvent.ACTION_CALL_TOGGLE_MUTE:
            // TODO: only iOS
            break;
          case CallEvent.ACTION_CALL_TOGGLE_DMTF:
            // TODO: only iOS
            break;
          case CallEvent.ACTION_CALL_TOGGLE_GROUP:
            // TODO: only iOS
            break;
          case CallEvent.ACTION_CALL_TOGGLE_AUDIO_SESSION:
            // TODO: only iOS
            break;
        }
      });
    ```
  * Call from Native (iOS PushKit) 
    ```java
      var info = [String: Any?]()
      info["id"] = "44d915e1-5ff4-4bed-bf13-c423048ec97a"
      info["nameCaller"] = "Hien Nguyen"
      info["handle"] = "0123456789"
      SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(flutter_callkit_incoming.Data(args: info), fromPushKit: true)
    ```

4. Properties

    | Prop            | Description                                                             | Default     |
    | --------------- | ----------------------------------------------------------------------- | ----------- |
    |  **`id`**       | UUID identifier for each call. UUID should be unique for every call and when the call is  ended, the same UUID for that call to be used. suggest using <a href='https://pub.dev/packages/uuid'>uuid</a>    | Required    |
    | **`nameCaller`**| Caller's name.                                                          | _None_      |
    | **`appName`**   | App's name. using for display inside Callkit(iOS).                      |   App Name  |
    | **`avatar`**    | Avatar's URL used for display for Android. `/android/src/main/res/drawable-xxxhdpi/ic_default_avatar.png`                             |    _None_   |
    | **`handle`**    | Phone number/Email/Any.                                                 |    _None_   |
    |   **`type`**    |  0 - Audio Call, 1 - Video Call                                         |     `0`     |
    | **`duration`**  | Incoming call/Outgoing call display time (second). If the time is over, the call will be missed.                                                                                     |    `30000`  |
    |   **`extra`**   | Any data added to the event when received.                              |     `{}`    |
    |   **`headers`** | Any data for custom header avatar/background image.                     |     `{}`    |
    |  **`android`**  | Android data needed to customize UI.                                    |    Below    |
    |    **`ios`**    | iOS data needed.                                                        |    Below    |

    <br>
    
* Android

    | Prop                        | Description                                                             | Default          |
    | --------------------------- | ----------------------------------------------------------------------- | ---------------- |
    | **`isCustomNotification`**  | Using custom notifications.                                             | `false`          |
    |       **`isShowLogo`**      | Show logo app inside full screen. `/android/src/main/res/drawable-xxxhdpi/ic_logo.png` | `false`          |
    |      **`ringtonePath`**     | File name ringtone. put file into `/android/app/src/main/res/raw/ringtone_default.pm3`                                                                                                    |`ringtone_default`|
    |     **`backgroundColor`**   | Incoming call screen background color.                                  |     `#0955fa`    |
    |      **`backgroundUrl`**    | Using image background for Incoming call screen.                        |       _None_     |
    |      **`actionColor`**      | Color used in button/text on notification.                              |    `#4CAF50`     |

    <br>
    
* iOS

    | Prop                                      | Description                                                             | Default     |
    | ----------------------------------------- | ----------------------------------------------------------------------- | ----------- |
    |               **`iconName`**              | App's Icon. using for display inside Callkit(iOS)                       | `false`     |
    |              **`handleType`**             | Type handle call `generic`, `number`, `email`                           | `generic`   |
    |             **`supportsVideo`**           |                                                                         |   `true`    |
    |          **`maximumCallGroups`**          |                                                                         |     `2`     |
    |       **`maximumCallsPerCallGroup`**      |                                                                         |     `1`     |
    |           **`audioSessionMode`**          |                                                                         |   _None_    |
    |        **`audioSessionActive`**           |                                                                         |    `true`   |
    |   **`audioSessionPreferredSampleRate`**   |                                                                         |  `44100.0`  |
    |**`audioSessionPreferredIOBufferDuration`**|                                                                         |  `0.005`    |
    |            **`supportsDTMF`**             |                                                                         |    `true`   |
    |            **`supportsHolding`**          |                                                                         |    `true`   |
    |          **`supportsGrouping`**           |                                                                         |    `true`   |
    |         **`supportsUngrouping`**          |                                                                         |   `true`    |
    |           **`ringtonePath`**              | Add file to root project xcode  `/ios/Runner/Ringtone.caf`  and Copy Bundle Resources(Build Phases)                                                                                                               |`Ringtone.caf`|


5. Source code

    ```
    please checkout repo github
    https://github.com/hiennguyen92/flutter_callkit_incoming
    ```
  * <a href='https://github.com/hiennguyen92/flutter_callkit_incoming'>https://github.com/hiennguyen92/flutter_callkit_incoming</a>

    <br>

6. Todo
  * Add `WakeLock` (background tasks) for Android
  * Switch using `service` for Android

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
