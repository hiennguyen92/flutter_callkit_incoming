import Foundation

final public class Cache {
    private init() {}
    
    public static let shared = Cache()
    
    private (set) public var latestEvent: [String: Any?]?
    
    public func updateLatestEvent(action: String, data: [String: Any?]) {
        latestEvent = [
            "event": action,
            "body": data,
        ]
    }
    
    public func clearLatestEvent() {
        latestEvent = nil
    }
}
