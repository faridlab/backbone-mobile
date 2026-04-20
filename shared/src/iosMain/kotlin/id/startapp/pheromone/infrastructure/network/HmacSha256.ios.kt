package id.startapp.pheromone.infrastructure.network

/**
 * iOS implementation of HMAC-SHA256 using CommonCrypto.
 *
 * Uses Swift CryptoHelper via native interop for proper cryptographic implementation.
 * The Swift implementation is in ios/iosApp/KeychainHelper.swift
 *
 * To verify the Swift file is in your Xcode project:
 * 1. Open ios/iosApp/iosApp.xcodeproj
 * 2. Verify KeychainHelper.swift is included in the target
 */
actual fun hmacSha256Platform(key: String, data: String): String {
    // Calls Swift function via Kotlin/Native interop
    return CryptoHelper.hmacSHA256(key, data)
}

/**
 * Swift CryptoHelper bridge function.
 *
 * This function must be implemented in Swift (see ios/iosApp/KeychainHelper.swift).
 * The Kotlin compiler will link to the native implementation at runtime.
 */
private object CryptoHelper {
    external fun hmacSHA256(key: String, data: String): String
}
