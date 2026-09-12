# TOEICSpace Mobile

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/KOTLIN-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/JETPACK%20COMPOSE-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img alt="Android" src="https://img.shields.io/badge/ANDROID-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Architecture" src="https://img.shields.io/badge/ARCHITECTURE-CLEAN%20%2B%20MVVM-0A66C2?style=for-the-badge" />
  <img alt="License" src="https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge" />
</p>

TOEICSpace Mobile is the native Android client for TOEICSpace, an online TOEIC Listening & Reading learning and test preparation platform combined with a Learning Management System (LMS) for language centers.

The app follows Clean Architecture combined with the MVVM pattern, built entirely with Kotlin and Jetpack Compose, and shares the same Backend/API as the TOEICSpace web application.

## ✨ Features

- Course, module, and lesson learning with progress tracking.
- Vocabulary learning via flashcards, personal vocabulary sets, and vocabulary review.
- TOEIC Part 1–7 practice, mini tests, and mock tests with timer, auto-save, and auto-submit.
- Test results, attempt history, and on-demand answer explanations.
- Mistake Notebook and Smart Review sessions.
- Placement test, TOEIC goal setting, and personalized learning roadmap.
- Assignment tracking, class schedule, and attendance.
- In-app notifications.
- Guest mode with local caching for practice results, mistakes, and quick-added vocabulary.

This release targets Android only. Tuition payment, invoice, and refund management are handled outside the app and are out of scope for this repository.

## 🏗️ Architecture

The project uses Clean Architecture with a package-by-feature structure. Each feature is organized into `data`, `domain`, and `presentation` layers. See [`GUIDELINE_ANDROID.md`](GUIDELINE_ANDROID.md) for the full architecture, coding conventions, and Compose Preview guidelines.

## 🛠️ Installation

### Prerequisites

- Android Studio (latest stable)
- JDK `17`
- Android SDK Platform `34` (compileSdk), minSdk `28`
- Git

### Local setup

1. Clone the repository:

```bash
   git clone git@github.com:YDraGN/toeic-space-mobile.git
   cd toeic-space-mobile
```

2. Open the project in Android Studio and let Gradle Sync complete.

3. Create the local configuration file:

```bash
   cp local.properties.example local.properties
```

4. Set the backend API URL in `local.properties`:

```properties
   API_BASE_URL=http://localhost:8080
```

## 🚀 Usage

Run the app on an emulator or physical device directly from Android Studio, or via Gradle:

```bash
./gradlew installDebug
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Build a release APK:

```bash
./gradlew assembleRelease
```

Run unit tests:

```bash
./gradlew test
```

Run lint checks:

```bash
./gradlew ktlintCheck
```

## 📚 Documentation

```text
docs/
├── api-integration.md
├── release.md
└── screens-flow.md

GUIDELINE_ANDROID.md
```

## 📄 License

This project is licensed under the [MIT License](LICENSE).
