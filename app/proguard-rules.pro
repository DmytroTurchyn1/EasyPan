# Firestore serialization
-keep class com.google.firebase.firestore.** { *; }

# Your DTOs for Firebase
-keep class com.cook.easypan.easypan.data.dto.** { *; }
-keepclassmembers class com.cook.easypan.easypan.data.dto.** { *; }

# Serialization support
-keepattributes *Annotation*
-keepattributes Signature

# Google Sign-In (minimal)
-keep class androidx.credentials.CredentialManager { *; }
-keep class androidx.credentials.GetCredentialRequest** { *; }
-keep class androidx.credentials.GetCredentialResponse { *; }
-keep class androidx.credentials.CustomCredential { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Keep all navigation routes
-keepnames class com.cook.easypan.easypan.presentation.navigation.Route { *; }
-keepnames class com.cook.easypan.easypan.presentation.navigation.Route$* { *; }

# Suppress warnings
-dontwarn sun.misc.Unsafe
-dontwarn javax.naming.**
-dontwarn org.apiguardian.api.API
