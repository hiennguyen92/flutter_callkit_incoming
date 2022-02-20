//
//  StringUtils.swift
//  flutter_callkit_incoming
//
//  Created by Hien Nguyen on 21/02/2022.
//

import Foundation
import CryptoSwift

extension String {
    
    func encrypt(encryptionKey: String = "xrBixqjjMhHifSDgSJ8O4QJYMZ1UHs45", iv: String = "lmYSgP3vixDAiBzW") -> String {
        if let aes = try? AES(key: encryptionKey, iv: iv),
           let encrypted = try? aes.encrypt(Array<UInt8>(self.utf8)) {
            return encrypted.toHexString()
        }
        return ""
    }
    
    func decrypt(encryptionKey: String = "xrBixqjjMhHifSDgSJ8O4QJYMZ1UHs45", iv: String = "lmYSgP3vixDAiBzW") -> String {
        if let aes = try? AES(key: encryptionKey, iv: iv),
           let decrypted = try? aes.decrypt(Array<UInt8>(hex: self)) {
            return String(data: Foundation.Data(decrypted), encoding: .utf8) ?? ""
        }
        return ""
    }
    
    func fromBase64() -> String {
        guard let data = Foundation.Data(base64Encoded: self) else {
            return ""
        }
        
        return String(data: data, encoding: .utf8)!
    }
    
    func toBase64() -> String {
        return Foundation.Data(self.utf8).base64EncodedString()
    }
    
    
    public func encryptHandle(encryptionKey: String = "xrBixqjjMhHifSDgSJ8O4QJYMZ1UHs45", iv: String = "lmYSgP3vixDAiBzW") -> String {
        return self.encrypt().toBase64()
    }
    
    public func decryptHandle(encryptionKey: String = "xrBixqjjMhHifSDgSJ8O4QJYMZ1UHs45", iv: String = "lmYSgP3vixDAiBzW") -> String {
        return self.fromBase64().decrypt()
    }
    
    public func getDecryptHandle() -> [String: Any] {
        if let data = self.decryptHandle().data(using: .utf8) {
            do {
                return try (JSONSerialization.jsonObject(with: data, options: []) as? [String: Any])!
            } catch {
                print(error.localizedDescription)
            }
        }
        return [:]
    }
    
}
