//
//  KeychainHelper.swift
//  iosApp
//
//  Swift bridging code for secure Keychain storage on iOS.
//  This file should be added to the iOS Xcode project.
//
//  To use from Kotlin:
//  ```kotlin
//  // Call Swift function from Kotlin
//  KeychainHelper.set(key: "access_token", value: "token_value")
//  val token = KeychainHelper.get(key: "access_token")
//  ```
//

import Foundation
import Security
import CommonCrypto

// MARK: - Keychain Helper
@objc public class KeychainHelper: NSObject {

    private static let service = "id.startapp.keychain"
    private static let accessGroup: String? = nil // Set to your app group if using app extensions

    /// Store a string value in Keychain
    @objc public static func set(key: String, value: String) -> Bool {
        guard let data = value.data(using: .utf8) else { return false }

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ]

        // Add access group if provided
        var finalQuery = query
        if let accessGroup = accessGroup {
            finalQuery[kSecAttrAccessGroup as String] = accessGroup
        }

        // Delete any existing value first
        SecItemDelete(finalQuery as CFDictionary)

        // Add new value
        let status = SecItemAdd(finalQuery as CFDictionary, nil)
        return status == errSecSuccess
    }

    /// Get a string value from Keychain
    @objc public static func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        // Add access group if provided
        var finalQuery = query
        if let accessGroup = accessGroup {
            finalQuery[kSecAttrAccessGroup as String] = accessGroup
        }

        var result: AnyObject?
        let status = SecItemCopyMatching(finalQuery as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }

        return value
    }

    /// Delete a value from Keychain
    @objc public static func delete(key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]

        // Add access group if provided
        var finalQuery = query
        if let accessGroup = accessGroup {
            finalQuery[kSecAttrAccessGroup as String] = accessGroup
        }

        let status = SecItemDelete(finalQuery as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    /// Clear all values from Keychain for this service
    @objc public static func clearAll() -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service
        ]

        // Add access group if provided
        var finalQuery = query
        if let accessGroup = accessGroup {
            finalQuery[kSecAttrAccessGroup as String] = accessGroup
        }

        let status = SecItemDelete(finalQuery as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }
}

// MARK: - Crypto Helper
@objc public class CryptoHelper: NSObject {

    /// Compute HMAC-SHA256 signature
    /// - Parameters:
    ///   - key: The secret key as a String
    ///   - data: The data to sign as a String
    /// - Returns: Base64-encoded HMAC-SHA256 signature
    @objc public static func hmacSHA256(key: String, data: String) -> String {
        guard let keyData = key.data(using: .utf8),
              let dataData = data.data(using: .utf8) else {
            return ""
        }

        var digest = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))

        keyData.withUnsafeBytes { keyBytes in
            dataData.withUnsafeBytes { dataBytes in
                CCHmac(CCHmacAlgorithm(kCCHmacAlgSHA256),
                       keyBytes.baseAddress?.assumingMemoryBound(to: UInt8.self),
                       keyData.count,
                       dataBytes.baseAddress?.assumingMemoryBound(to: UInt8.self),
                       dataData.count,
                       &digest)
            }
        }

        let signatureData = Data(digest)
        return signatureData.base64EncodedString()
    }

    /// Generate a random nonce for request signing
    /// - Parameter length: The number of random bytes to generate
    /// - Returns: Hex-encoded random string
    @objc public static func generateNonce(length: Int = 16) -> String {
        var bytes = [UInt8](repeating: 0, count: length)
        let status = SecRandomCopyBytes(kSecRandomDefault, length, &bytes)

        guard status == errSecSuccess else {
            // Fallback to less secure random if SecRandomCopyBytes fails
            return (0..<length).map { _ in UInt8.random(in: 0...255) }
                .map { String(format: "%02x", $0) }
                .joined()
        }

        return bytes.map { String(format: "%02x", $0) }.joined()
    }
}

// MARK: - Certificate Helper
@objc public class CertificateHelper: NSObject {

    /// Compute SHA-256 hash of data (for certificate pinning)
    /// - Parameter data: Raw data bytes as NSData
    /// - Returns: Base64-encoded SHA-256 hash
    @objc public static func computeSha256Hash(_ data: Data) -> String {
        var digest = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
        data.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(data.count), &digest)
        }
        return Data(digest).base64EncodedString()
    }

    /// Compute SHA-256 hash of byte array (for Kotlin interop)
    /// - Parameter byteArray: Array of bytes as Kotlin ByteArray
    /// - Returns: Base64-encoded SHA-256 hash
    @objc public static func computeSha256HashFromByteArray(_ byteArray: [UInt8]) -> String {
        let data = Data(byteArray)
        return computeSha256Hash(data)
    }

    /// Extract public key from certificate and compute its SHA-256 hash
    /// - Parameter certificateData: DER-encoded certificate data
    /// - Returns: Base64-encoded SHA-256 hash of the public key, or nil on failure
    @objc public static func computePublicKeyHash(_ certificateData: Data) -> String? {
        guard let certificate = SecCertificateCreateWithData(nil, certificateData as CFData) else {
            return nil
        }

        // Extract public key from certificate
        var publicKey: SecKey?
        let policy = SecPolicyCreateBasicX509()

        var trust: SecTrust?
        let status = SecTrustCreateWithCertificates(certificate as CFTypeRef, policy, &trust)

        guard let trustObject = trust, status == errSecSuccess else {
            return nil
        }

        // Evaluate trust to get the public key
        var error: CFError?
        let isValid = SecTrustEvaluateWithError(trustObject, &error)

        if isValid, let signingKey = SecTrustCopyPublicKey(trustObject) {
            publicKey = signingKey
        } else if let errorRef = error {
            // Fallback: try to extract key directly from certificate
            let certData = certificateData as CFData
            if let cert = SecCertificateCreateWithData(nil, certData) {
                publicKey = SecTrustCopyPublicKey(trustObject!)
            }
        }

        guard let pubKey = publicKey else {
            return nil
        }

        // Get the external representation of the public key
        var error: Unmanaged<CFError>?
        guard let keyData = SecKeyCopyExternalRepresentation(pubKey, &error) as Data? else {
            return nil
        }

        // Hash the public key data
        return computeSha256Hash(keyData)
    }

    /// Compute public key hash from byte array (for Kotlin interop)
    /// - Parameter byteArray: Array of bytes as Kotlin ByteArray
    /// - Returns: Base64-encoded SHA-256 hash of the public key, or empty string on failure
    @objc public static func computePublicKeyHashFromByteArray(_ byteArray: [UInt8]) -> String {
        let data = Data(byteArray)
        return computePublicKeyHash(data) ?? ""
    }

    /// Extract certificate from an SSL connection for debugging/pinning
    /// - Parameters:
    ///   - host: The hostname to connect to
    ///   - port: The port number (default 443)
    /// - Returns: DER-encoded certificate data, or nil on failure
    @objc public static func extractCertificate(from host: String, port: UInt32 = 443) -> Data? {
        // This would require implementing a custom URL session delegate
        // For now, return nil as this is typically done at the network layer
        return nil
    }
}
