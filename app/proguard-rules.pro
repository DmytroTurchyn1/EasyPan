# DTOs deserialized by Firestore via reflection
-keep class com.cook.easypan.easypan.data.dto.** { *; }
-keepclassmembers class com.cook.easypan.easypan.data.dto.** { *; }

# Serialization support
-keepattributes *Annotation*
-keepattributes Signature

# Google Sign-In (Credential Manager Google ID response parsing)
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Keep all navigation routes
-keep class com.cook.easypan.easypan.presentation.navigation.Route { *; }
-keep class com.cook.easypan.easypan.presentation.navigation.Route$* { *; }

# Strip verbose logging from release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Suppress warnings
-dontwarn sun.misc.Unsafe
-dontwarn javax.naming.**
-dontwarn org.apiguardian.api.API
