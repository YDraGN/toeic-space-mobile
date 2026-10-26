<h1 align="center">TOEICSpace Mobile</h1>

<p align="center">
  Native Android client for <b>TOEICSpace</b>: TOEIC Listening &amp; Reading practice combined with an LMS for language centers.
</p>

<p align="center">
  <a href="https://github.com/YDraGN/toeic-space-mobile/actions/workflows/android-ci.yml"><img alt="Android CI" src="https://github.com/YDraGN/toeic-space-mobile/actions/workflows/android-ci.yml/badge.svg?branch=develop" /></a>
  <img alt="License" src="https://img.shields.io/badge/license-MIT-green" />
  <img alt="Min SDK" src="https://img.shields.io/badge/minSdk-28-3DDC84?logo=android&logoColor=white" />
  <img alt="Target SDK" src="https://img.shields.io/badge/targetSdk-37-3DDC84?logo=android&logoColor=white" />
</p>

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img alt="Hilt" src="https://img.shields.io/badge/Hilt-DI-2196F3?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Coroutines" src="https://img.shields.io/badge/Coroutines%20%26%20Flow-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Retrofit" src="https://img.shields.io/badge/Retrofit%20%2B%20OkHttp-48B983?style=for-the-badge&logo=square&logoColor=white" />
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-Kotlin%20DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
</p>

---

## Overview

TOEICSpace Mobile shares the same backend (an ASP.NET Core microservices system behind an API Gateway) with the TOEICSpace web app.

- **Learn:** courses, modules, lessons, and progress tracking.
- **Vocabulary:** flashcards, personal sets, and spaced review.
- **Practice:** TOEIC Parts 1–7, mini tests, and timed mock tests with auto-save and auto-submit.
- **Review:** results, attempt history, explanations, Mistake Notebook, and Smart Review.
- **Roadmap:** placement test, score goal, and a personalized roadmap.
- **Classroom:** assignments, schedule, attendance, and notifications.
- **Guest mode:** practice without an account, with local caching.

Payments, invoices, and refunds are handled outside the app.

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3, Compose Navigation (type-safe routes) |
| Architecture | MVVM + Clean Architecture (UI / Domain / Data), package-by-feature, single Gradle module |
| DI | Hilt |
| Async | Kotlin Coroutines, Flow, `StateFlow` |
| Network | Retrofit, OkHttp, kotlinx.serialization |
| Local storage | DataStore (Preferences), Room (offline cache) |
| Build | AGP 9, Gradle version catalog, ktlint |
| CI | GitHub Actions: ktlint, Android Lint, unit tests, debug build |

## Getting started

### Prerequisites

- Android Studio (latest stable)
- JDK 17 or newer to launch Gradle (the Gradle daemon itself runs on JDK 25 and is downloaded automatically, see `gradle/gradle-daemon-jvm.properties`)
- Android SDK Platform 37
- A device or emulator on Android 9 (API 28) or newer

### Setup

```bash
git clone git@github.com:YDraGN/toeic-space-mobile.git
cd toeic-space-mobile
cp local.properties.example local.properties
```

Edit `local.properties` and point the app at the API Gateway:

```properties
# Android emulator → host machine
API_BASE_URL=http://10.0.2.2:5050/
```

On a physical device, use your machine's LAN IP instead of `10.0.2.2`. See [docs/api-integration.md](docs/api-integration.md).

### Common commands

```bash
./gradlew installDebug          # build and install on a connected device
./gradlew ktlintCheck           # code style
./gradlew lintDebug             # Android Lint
./gradlew testDebugUnitTest     # unit tests
./gradlew assembleRelease       # release APK (see docs/release.md)
```

## Project structure

```text
app/src/main/java/com/toeicspace/android/
├── core/          # shared code: designsystem, network, datastore, navigation, di, common
└── feature/       # one package per feature: splash, onboarding, auth, home, ...
    └── <feature>/
        ├── data/          # API services, DTOs, repository implementations
        ├── domain/        # models, repository interfaces, use cases
        └── presentation/  # Route + Screen composables, ViewModel, UiState
```

Details: [docs/architecture.md](docs/architecture.md).

## Documentation

| Document | Content |
|---|---|
| [Architecture](docs/architecture.md) | Layers, single source of truth, navigation, DI |
| [Coding conventions](docs/coding-conventions.md) | Kotlin, Compose, naming, constants, string resources |
| [Design system](docs/design-system.md) | Color, typography, spacing, dimensions, accessibility, orientation |
| [Screens flow](docs/screens-flow.md) | Loading → Onboarding → Auth → Home, guest mode |
| [API integration](docs/api-integration.md) | Gateway, authentication, Google Sign-In, error format |
| [Release](docs/release.md) | Versioning, signing, building the APK |
| [Decision records](docs/adr/) | Why we made the key technical choices |
| [Contributing](CONTRIBUTING.md) | Branches, commits, pull requests, issues |

## Related repositories

- [toeic-space-be](https://github.com/bnguien/toeic-space-be): backend microservices and API Gateway
- [toeic-space-fe](https://github.com/bnguien/toeic-space-fe): web frontend

## License

Released under the [MIT License](LICENSE).
