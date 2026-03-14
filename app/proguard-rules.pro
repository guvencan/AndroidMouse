# Stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Hilt / Dagger
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# DataStore
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# App models (domain layer — must not be obfuscated)
-keep class com.godofcodes.androidmouse.domain.model.** { *; }
