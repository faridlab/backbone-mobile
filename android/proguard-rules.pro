# Backbone MobileApp ProGuard Rules

# Kotlin
-keep class kotlin.reflect.** { *; }
-keep class kotlin.** { *; }
-dontwarn kotlin.**
-dontwarn kotlin.reflect.**

# Kotlinx Serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.ktor.util.pipeline.** { *; }
-keepclassmembers class io.ktor.** {
    *** Handler;
    *** SuspendHandler;
}

# Decompose
-keep class com.arkivanov.decompose.** { *; }
-keep interface com.arkivanov.decompose.** { *; }
-dontwarn com.arkivanov.decompose.**

# Koin
-keepattributes *Annotation*
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }
-dontwarn org.koin.**

# SQLDelight
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Google Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Google Tink Crypto - don't warn about missing annotations
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
