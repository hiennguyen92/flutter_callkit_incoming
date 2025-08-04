//
//  NotificationDelegate.swift
//  Pods
//
//  Created by Hien Nguyen on 2/8/25.
//

class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()
    
    private override init() {
        super.init()
        registerNotificationCategory()
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.alert, .sound, .badge])
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
