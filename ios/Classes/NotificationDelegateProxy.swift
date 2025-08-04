//
//  NotificationDelegate.swift
//  Pods
//
//  Created by Hien Nguyen on 2/8/25.
//

class NotificationDelegateProxy: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegateProxy()
    
    private override init() {
        super.init()
        registerNotificationCategory()
    }
    
    weak var originalDelegate: UNUserNotificationCenterDelegate?
    
    
    func attach() {
        let center = UNUserNotificationCenter.current()
        originalDelegate = center.delegate
        center.delegate = self
    }
    

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        originalDelegate?.userNotificationCenter?(center, didReceive: response, withCompletionHandler: completionHandler)
        completionHandler()
    }
    
    func registerNotificationCategory() {
        let callbackAction = UNNotificationAction(identifier: "CALLBACK_ACTION",
                                                      title: "Gọi lại",
                                                      options: [.foreground])

        let missedCallCategory = UNNotificationCategory(identifier: "MISSED_CALL_CATEGORY",
                                                            actions: [callbackAction],
                                                            intentIdentifiers: [],
                                                            options: [])

        UNUserNotificationCenter.current().setNotificationCategories([missedCallCategory])
    }
}
