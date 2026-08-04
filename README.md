# 🍳 EasyPan

**EasyPan** is a fun, interactive, and user-friendly mobile app that helps users cook easy delicious
meals.

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="70">](https://play.google.com/store/apps/details?id=com.cook.easypan)

## 📱 About

EasyPan transforms cooking from a chore into an enjoyable experience. Whether you're a beginner cook
or an experienced chef, our app provides clear guidance and beautiful visuals to help you create
amazing meals — from picking a single recipe to planning a whole week and walking into the store
with the grocery list already sorted.

## ✨ Features

- 🎨 **Modern UI/UX** — built entirely with Jetpack Compose and Material 3
- 👤 **Google Sign-In** — secure OAuth through Credential Manager and Firebase Auth
- 🔍 **Recipe Discovery** — browse and filter recipes served from Firebase Firestore
- ❤️ **Favorites** — save recipes and pick them back up on any device
- 📖 **Guided Cooking** — step-by-step instructions with a countdown timer that keeps running in a
  foreground service, plus an optional keep-screen-on mode
- 🎉 **Finish Screen** — confetti and a running count of everything you've cooked
- 🗓️ **Meal Planning** — a preferences wizard (favorite ingredients, portions, ingredients to skip,
  allergies, meals per day) generates a weekly plan you can review and regenerate
- 🧾 **Grocery Receipt** — the plan's ingredients are merged, categorized, and rendered as a paper
  receipt you can check off and share as an image
- ⭐ **EasyPan Chef** — subscription tier (monthly or yearly) powered by RevenueCat, behind a custom
  in-app paywall
- 📴 **Offline-First** — recipes, favorites, and profile data are cached locally so the app opens
  with content
- 🔔 **Push Notifications** — new-recipe announcements via Firebase Cloud Messaging

> The grocery receipt flow ships behind the `receipt_screen` Firebase Remote Config flag
> (see `app/src/main/res/xml/remote_config_defaults.xml`), so it can be rolled out independently of
> a store release.

## 🛠️ Tech Stack

- **Language**: Kotlin 2.4.0 (JVM target 11), AGP 9.3.1
- **Platform**: Android — `minSdk 28`, `targetSdk`/`compileSdk 37`
- **UI Framework**: Jetpack Compose (BOM 2026.06.01) + Material 3
- **Architecture**: MVVM with a strict data / domain / presentation split
- **State Management**: `StateFlow`, Compose state, `SavedStateHandle` for process death
- **Dependency Injection**: Koin 4.2.2
- **Navigation**: Navigation Compose 2.9.8 with type-safe `@Serializable` routes
- **Backend**: Firebase BOM 34.15.0 — Auth, Firestore, Analytics, Crashlytics (+ NDK), Performance
  Monitoring, Cloud Messaging, Remote Config, App Check (Play Integrity)
- **Authentication**: Credential Manager 1.6.0 + Google ID → Firebase Auth
- **Billing**: RevenueCat 10.15.1
- **Local Storage**: AndroidX DataStore 1.2.1 with a `kotlinx-serialization` JSON serializer
- **Image Loading**: Coil 3.5.0
- **Network**: Ktor 3.5.1 (OkHttp engine)
- **Performance**: Baseline profiles via `profileinstaller` + a Macrobenchmark module

## 🚀 Getting Started

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/DmytroTurchyn1/EasyPan.git
   ```

2. Open the project in Android Studio

3. Add your `google-services.json` file to the `app/` directory

4. Add a `keystore.properties` file to the project root

5. Sync the project with Gradle files

6. Build and run the app on your device or emulator

### Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication with the Google provider
3. Set up a Firestore database
4. Register App Check with the Play Integrity provider (the debug build uses the debug provider)
5. Download `google-services.json` and place it in `app/`

## 🧰 Build & Test

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore.properties)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented (UI) tests — requires a connected device or emulator
./gradlew connectedAndroidTest

# Run the macrobenchmarks
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

Three build types:

| Type        | Notes                                                                             |
|-------------|-----------------------------------------------------------------------------------|
| `debug`     | Unminified, App Check debug provider, RevenueCat test key                         |
| `release`   | R8 + resource shrinking, signed from `keystore.properties`                        |
| `benchmark` | Inherits from `release`, non-debuggable, reuses the `src/release/java` source set |

## 🔒 Security

Please report vulnerabilities privately through the repository's Security tab rather than opening a
public issue — see [SECURITY.md](SECURITY.md).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
