# 🍳 EasyPan

**EasyPan** is a fun, interactive, and user-friendly mobile app that helps users cook delicious meals with ease. It features a sleek design, step-by-step instructions, and an intuitive interface built with modern Android development practices and MVVM architecture.

## 📱 About

EasyPan transforms cooking from a chore into an enjoyable experience. Whether you're a beginner cook or an experienced chef, our app provides clear guidance and beautiful visuals to help you create amazing meals.

## ✨ Features

- 🎨 **Modern UI/UX** - Built with Jetpack Compose for a smooth, native Android experience
- 📖 **Step-by-Step Instructions** - Clear, easy-to-follow cooking guides with timers
- 🔍 **Recipe Discovery** - Find new recipes to try from Firebase Firestore
- 👤 **Google Authentication** - Secure sign-in with Google OAuth

### Project Structure

```
app/src/main/java/com/cook/easypan/easypan/
├── data/                           # Data layer
│   ├── auth/                       # Authentication handling
│   ├── database/                   # Firebase Firestore client
│   ├── dto/                        # Data transfer objects
│   ├── mappers/                    # Data mapping functions
│   └── repository/                 # Repository implementations
├── domain/                         # Domain layer
│   ├── Recipe.kt                   # Recipe entity
│   ├── UserData.kt                 # User entity
│   ├── RecipeRepository.kt         # Repository interface
│   └── ...                        # Other domain models
└── presentation/                   # Presentation layer
    ├── authentication/             # Auth screens & ViewModels
    ├── home/                       # Home screens & ViewModels
    ├── profile/                    # Profile screens & ViewModels
    ├── recipe_detail/              # Recipe detail screens & ViewModels
    ├── recipe_step/                # Recipe step screens & ViewModels
    ├── favorite/                   # Favorites screens & ViewModels
    └── navigation/                 # Navigation setup
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **State Management**: StateFlow, Compose State
- **Dependency Injection**: Koin
- **Backend**: Firebase (Firestore, Authentication)
- **Authentication**: Google OAuth via Firebase Auth
- **Image Loading**: Coil3
- **Network**: Ktor Client
- **Navigation**: Jetpack Navigation Compose
- **Platform**: Android (SDK 21+)

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Kotlin 1.9+
- Android SDK 21+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/DmytroTurchyn1/EasyPan.git
   ```

2. Open the project in Android Studio

3. Add your `google-services.json` file with dummy data to the `app/` directory

4. Sync the project with Gradle files

5. Build and run the app on your device or emulator

### Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication with Google provider
3. Set up Firestore database
4. Download and add `google-services.json` with dummy data to your project

## 📂 Key Components

### ViewModels
- `AuthenticationViewModel` - Handles Google sign-in/sign-out
- `HomeViewModel` - Manages recipe discovery and home screen state
- `RecipeDetailViewModel` - Recipe details and navigation
- `RecipeStepViewModel` - Step-by-step cooking instructions
- `ProfileViewModel` - User profile management
- `FavoriteViewModel` - Favorite recipes management
- `SelectedRecipeViewModel` - Shared recipe state across screens

### Repositories
- `RecipeRepository` - Interface for recipe data operations
- `DefaultRecipeRepository` - Firebase Firestore implementation

### Domain Models
- `Recipe` - Recipe entity with ingredients, steps, and metadata
- `UserData` - User profile information
- `StepDescription` - Individual cooking step details

## 🔧 Features in Detail

### Authentication
- Google OAuth integration via Firebase
- Automatic sign-in state management
- Secure user session handling

### Recipe Management
- Firebase Firestore integration for recipe data
- Step-by-step cooking instructions
- Built-in timer functionality for cooking steps
- Recipe difficulty indicators

### Navigation
- Bottom navigation bar
- Deep linking support
- Shared ViewModels for state persistence

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Dmytro** - [@DmytroTurchyn1](https://github.com/DmytroTurchyn1)