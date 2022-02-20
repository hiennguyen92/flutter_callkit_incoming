# Flutter Callkit Incoming

Setup Pushkit for IOS.

If you are making VoIP application than you definitely want to update your application in the background state as well as wake your application when any VoIP call is being received.

## 🚀&nbsp; Setup

1. Enable Voice over IP Setting

  * Xcode Project > Capabilities
    ![image info](./images/Setting.png)
  * VoIP Services Certificate

  Go to https://developer.apple.com/account/resources/certificates/add
  ![image info](./images/VoIPServicesCertificate.png)

  Download the certificate and install it into the Keychain Access app(download .cer and double click to install).

  * Export .p12
  ![image info](./images/KeychainAccess.png)
  * Convert .p12 to .pem
    ```console
      openssl pkcs12 -in YOUR_CERTIFICATES.p12 -out VOIP.pem -nodes -clcerts
    ```

2. Configure VoIP Push Notifications in Xcode project (Swift)
  ![image info](./images/Xcode-S1.png)
  
  ![image info](./images/Xcode-S2.png)

  // Start call from Recent history on click (add if necessary)

  ![image info](./images/Xcode-S3.png)

3. Testing
  * App Using
    https://github.com/onmyway133/PushNotifications

    ![image info](./images/TestingApp.png)
  * 
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
