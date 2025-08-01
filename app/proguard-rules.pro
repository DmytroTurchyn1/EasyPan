# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Firebase Firestore classes
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.** { *; }

# Keep all DTO classes for Firebase serialization
-keep class com.cook.easypan.easypan.data.dto.** { *; }

# Keep all public constructors and getters/setters for DTO classes
-keepclassmembers class com.cook.easypan.easypan.data.dto.** {
    public <init>();
    public <init>(...);
    public *;
}

# Keep serialization annotations
-keepattributes *Annotation*
-keepattributes Signature
-keep class kotlinx.serialization.** { *; }

# Keep data class properties
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Prevent obfuscation of classes used with Firebase
-keepnames class com.cook.easypan.easypan.data.dto.**
-keepclassmembernames class com.cook.easypan.easypan.data.dto.** { *; }

-dontwarn org.apiguardian.api.API

-keep class com.cook.easypan.easypan.data.** { *; }
-keep class com.cook.easypan.easypan.domain.** { *; }
-keep class com.cook.easypan.** { *; }

# Keep credential providers
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

