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

# Keep only what Firebase Firestore needs for serialization
-keep class com.google.firebase.firestore.PropertyName { *; }
-keep class com.google.firebase.firestore.Exclude { *; }
-keep class com.google.firebase.firestore.ServerTimestamp { *; }

# Keep DTO classes but only what's needed for Firebase serialization
-keep class com.cook.easypan.easypan.data.dto.RecipeDto {
    <init>();
    <fields>;
}
-keep class com.cook.easypan.easypan.data.dto.UserDto {
    <init>();
    <fields>;
}
-keep class com.cook.easypan.easypan.data.dto.StepDescriptionDto {
    <init>();
    <fields>;
}

# Keep only the default constructors and field names for Firebase
-keepclassmembers class com.cook.easypan.easypan.data.dto.** {
    <init>();
}

# Keep field names for Firebase serialization but not methods
-keepclassmembernames class com.cook.easypan.easypan.data.dto.** {
    <fields>;
}

# Keep serialization annotations
-keepattributes *Annotation*
-keepattributes Signature

# Keep only specific Kotlinx serialization classes that are actually used
-keep class kotlinx.serialization.SerialName { *; }
-keep class kotlinx.serialization.Serializable { *; }
