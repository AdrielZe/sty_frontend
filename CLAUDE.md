# CLAUDE.md — Sty (training_tracker)

This file gives context to AI assistants working on this codebase.

---

## Project Overview

**Sty** is a native Android workout tracking app built in Kotlin and Jetpack Compose.  
Single-module, offline-first, no remote backend. All data is persisted locally via Room.

**Core features:**
- Create and manage workout routines
- Active workout session screen (log exercises, sets, reps, weight)
- Workout history visualization
- Personal records tracking (weight PRs per exercise)
- User profile

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM + UDF + Repository Pattern
- **Local DB:** Room (sole data source — no network layer)
- **DI:** Manual injection via `AppContainer` (no Hilt/Koin)
- **Navigation:** Single NavHost (`GymTrackerNavHost`)
- **ML:** TFLite (`TFLiteExerciseClassifier`) for exercise classification

---

## Project Structure

```
training_tracker/
├── GymTrackerApp.kt                  # App-level entry point / DI root
├── GymTrackerApplication.kt
├── GymTrackerNavHost.kt              # Central navigation graph
├── MainActivity.kt
├── MainTabsScreen.kt                 # Bottom nav scaffold
│
├── data/
│   ├── AppContainer.kt               # Manual DI — repositories instantiated here
│   ├── local/
│   │   ├── AppDataBase.kt
│   │   ├── Converters.kt
│   │   └── dao/                      # One DAO per entity
│   ├── models/                       # Room entities + domain models
│   │   └── extensions/
│   ├── repository/                   # Repository implementations
│   └── routes/
│       └── Routes.kt                 # Navigation route definitions
│
├── domain/
│   ├── classifiers/                  # TFLite exercise classifier
│   └── repository/                   # Repository interfaces (contracts)
│
└── ui/
    ├── components/                   # Shared reusable composables
    ├── screens/                      # One package per screen
    │   └── <screen>/
    │       ├── <Screen>Screen.kt     # Composable UI only
    │       ├── <Screen>UiState.kt    # Immutable state data class
    │       └── <Screen>ViewModel.kt  # StateFlow + viewModelScope
    ├── theme/                        # Color, Type, Dimens, Theme
    └── utils/                        # Stateless utility functions
```

---

## Architecture Rules

**Layer communication flows strictly top-down:**
```
Composable → ViewModel → Repository → DAO
```

- Composables observe `UiState` via `collectAsStateWithLifecycle()` and emit events to the ViewModel.
- ViewModels hold state as `StateFlow<XUiState>` and use `viewModelScope` for all coroutines.
- Repositories are the single source of truth. They talk to DAOs directly (no network).
- Don't use Magic Strings. Whenever you implement a string, always extract the string resource in PT-BR and its translation in EN.
- No UseCase layer — if a function only delegates to a repository, it stays in the ViewModel.
- When making changes to the UI, ALWAYS make the changes as responsive as possible, making it fit in most android screen sizes.

**What never happens:**
- Composables don't contain business logic
- ViewModels don't reference `Context` (except `ApplicationContext` via constructor if truly needed)
- DAOs are never called outside of repository implementations
- `GlobalScope` is never used

---

## State Management

Each screen has its own `UiState` data class:
- Always immutable (`val` fields, `data class`)
- Annotate with `@Stable` or `@Immutable` when used as Compose state
- Use `copy()` for updates inside the ViewModel
- One `StateFlow<XUiState>` per ViewModel as the single state source

One-time events (navigation, snackbars) use `SharedFlow`.

---

## Compose Guidelines

- State is hoisted — composables receive data and emit callbacks, they don't own state
- Use `remember` and `derivedStateOf` to avoid unnecessary recompositions
- Avoid deep nesting: extract sub-composables when nesting exceeds 3 levels
- No business logic inside composables
- `key()` is used when rendering lists with dynamic identity

---

## Naming Conventions

| Type | Convention | Example |
|---|---|---|
| Screen composable | `<Name>Screen` | `WorkoutScreen` |
| ViewModel | `<Name>ViewModel` | `WorkoutViewModel` |
| UiState | `<Name>UiState` | `WorkoutUiState` |
| Route object | `Routes.<Name>` | `Routes.Workout` |
| DAO | `<Entity>Dao` | `WorkoutDao` |
| Repository interface | `<Entity>Repository` | `WorkoutRepository` |
| Repository impl | `<Entity>RepositoryImpl` | `WorkoutRepositoryImpl` |

---

## DI — AppContainer

Dependencies are manually wired in `AppContainer`. When adding a new repository:
1. Add the interface to `domain/repository/`
2. Add the implementation to `data/repository/`
3. Instantiate and expose it in `AppContainer`
4. Inject via ViewModel factory or constructor

No Hilt, no Koin — keep it simple.

---

## Screens Reference

| Package | Screen | Purpose |
|---|---|---|
| `welcome` | `WelcomeScreen` | Onboarding / first launch |
| `create_workout` | `CreateWorkoutScreen` | Create new workout routine |
| `registered_workouts` | `RegisteredWorkoutsScreen` | List of saved routines |
| `workout_details` | `WorkoutEditScreen` | Edit an existing workout |
| `workout_screen` | `WorkoutScreen` | Active session (log sets/reps) |
| `freestyle_workout` | `FreestyleWorkoutScreen` | Ad-hoc session, no preset routine |
| `workout_history` | `WorkoutHistoryScreen` | Past sessions list |
| `workout_report` | `WorkoutReportScreen` | Summary/stats for a session |
| `records` | `RecordsScreen` | Personal weight records |
| `home` | `HomeScreen` | Main dashboard |
| `user_profile` | `UserProfileScreen` | User info and settings |

---

## Key Design Decisions

- **Offline-first by design** — no network layer is planned; all state lives in Room
- **No Hilt** — `AppContainer` is sufficient for this scope; don't add Hilt unless the graph becomes unmanageable
- **WorkoutDelegate** — `workout_screen` uses a delegate pattern to split ViewModel responsibilities; respect this boundary
- **TFLite classifier** — lives in `domain/classifiers/`; treat it as a read-only inference utility, not a data source
- **Records logic** — PRs are calculated from full historical data, not isolated peak values; any change to `RecordsRepository` or `RecordsViewModel` must preserve historical progression

---

## What to Avoid

- Adding a UseCase layer unless there's genuine shared logic across multiple ViewModels
- Interfaces for classes that have exactly one implementation (except repository contracts, which exist for testability)
- Storing mutable state in composables that belongs in the ViewModel
- Using `LiveData` — `StateFlow` is the standard here
- Speculative features or abstractions not tied to a current requirement
