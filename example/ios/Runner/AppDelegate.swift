import UIKit
import CallKit
import AVFAudio
import PushKit
import Flutter
import flutter_callkit_incoming

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate, PKPushRegistryDelegate, CallkitIncomingAppDelegate {

    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        
        //Setup VOIP
        let mainQueue = DispatchQueue.main
        let voipRegistry: PKPushRegistry = PKPushRegistry(queue: mainQueue)
        voipRegistry.delegate = self
        voipRegistry.desiredPushTypes = [PKPushType.voIP]

        //Use if using WebRTC
        //RTCAudioSession.sharedInstance().useManualAudio = true
        //RTCAudioSession.sharedInstance().isAudioEnabled = false
        
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    // Call back from Recent history
    override func application(_ application: UIApplication,
                              continue userActivity: NSUserActivity,
                              restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        
        guard let handleObj = userActivity.handle else {
            return false
        }
        
        guard let isVideo = userActivity.isVideo else {
            return false
        }
        let objData = handleObj.getDecryptHandle()
        let nameCaller = objData["nameCaller"] as? String ?? ""
        let handle = objData["handle"] as? String ?? ""
        let data = flutter_callkit_incoming.Data(id: UUID().uuidString, nameCaller: nameCaller, handle: handle, type: isVideo ? 1 : 0)
        //set more data...
        //data.nameCaller = nameCaller
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.startCall(data, fromPushKit: true)
        
        return super.application(application, continue: userActivity, restorationHandler: restorationHandler)
    }
    
    // Handle updated push credentials
    func pushRegistry(_ registry: PKPushRegistry, didUpdate credentials: PKPushCredentials, for type: PKPushType) {
        print(credentials.token)
        let deviceToken = credentials.token.map { String(format: "%02x", $0) }.joined()
        print(deviceToken)
        //Save deviceToken to your server
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP(deviceToken)
    }
    
    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        print("didInvalidatePushTokenFor")
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.setDevicePushTokenVoIP("")
    }
    
    // Handle incoming pushes
    func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, for type: PKPushType, completion: @escaping () -> Void) {
        print("didReceiveIncomingPushWith")
        guard type == .voIP else { return }
        
        let id = payload.dictionaryPayload["id"] as? String ?? ""
        let nameCaller = payload.dictionaryPayload["nameCaller"] as? String ?? ""
        let handle = payload.dictionaryPayload["handle"] as? String ?? ""
        let isVideo = payload.dictionaryPayload["isVideo"] as? Bool ?? false
        
        let data = flutter_callkit_incoming.Data(id: id, nameCaller: nameCaller, handle: handle, type: isVideo ? 1 : 0)
        //set more data
        data.extra = ["user": "abc@123", "platform": "ios"]
        //data.iconName = ...
        //data.....
        SwiftFlutterCallkitIncomingPlugin.sharedInstance?.showCallkitIncoming(data, fromPushKit: true)
        
        //Make sure call completion()
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            completion()
        }
    }
    
    
    // Func Call api for Accept
    func onAccept(_ call: Call, _ action: CXAnswerCallAction) {
        let json = ["action": "ACCEPT", "data": call.data.toJSON()] as [String: Any]
        print("LOG: onAccept")
        self.performRequest(parameters: json) { result in
            switch result {
            case .success(let data):
                print("Received data: \(data)")
                //Make sure call action.fulfill() when you are done(connected WebRTC - Start counting seconds)
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
                //Make sure call action.fulfill() when you are done
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
                //Make sure call action.fulfill() when you are done
                action.fulfill()

            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
    }
    
    // Func Call API for TimeOut
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
    
    // Func Callback Toggle Audio Session
    func didActivateAudioSession(_ audioSession: AVAudioSession) {
        //Use if using WebRTC
        //RTCAudioSession.sharedInstance().audioSessionDidActivate(audioSession)
        //RTCAudioSession.sharedInstance().isAudioEnabled = true
    }
    
    // Func Callback Toggle Audio Session
    func didDeactivateAudioSession(_ audioSession: AVAudioSession) {
        //Use if using WebRTC
        //RTCAudioSession.sharedInstance().audioSessionDidDeactivate(audioSession)
        //RTCAudioSession.sharedInstance().isAudioEnabled = false
    }
    
    func performRequest(parameters: [String: Any], completion: @escaping (Result<Any, Error>) -> Void) {
        if let url = URL(string: "https://webhook.site/e32a591f-0d17-469d-a70d-33e9f9d60727") {
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            //Add header
            
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: parameters, options: [])
                request.httpBody = jsonData
            } catch {
                completion(.failure(error))
                return
            }
            
            let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    completion(.failure(error))
                    return
                }
                
                guard let data = data else {
                    completion(.failure(NSError(domain: "mobile.app", code: 0, userInfo: [NSLocalizedDescriptionKey: "Empty data"])))
                    return
                }

                do {
                    let jsonObject = try JSONSerialization.jsonObject(with: data, options: [])
                    completion(.success(jsonObject))
                } catch {
                    completion(.failure(error))
                }
            }
            task.resume()
        } else {
            completion(.failure(NSError(domain: "mobile.app", code: 0, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
        }
    }
    
    
}
