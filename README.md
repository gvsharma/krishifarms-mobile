# KrishiFarms Mobile

Internal Android CRM for KrishiFarms field and office operations — farmer management, crop procurement, workforce, expenses, and document capture. Built **offline-first** for low-connectivity rural areas; syncs to a FastAPI backend when online.

> **Audience:** Engineers and agentic IDEs (Cursor, etc.) starting a new session on this repo.

---

## Quick Reference

| Item | Value |
|------|-------|
| **Package** | `com.krishifarms.mobile` |
| **Repo** | https://github.com/gvsharma/krishifarms-mobile |
| **Branch** | `initial-commit` |
| **Gradle module** | `:app` (single-module monolith; multi-module split planned) |
| **Min / Target SDK** | 26 / 35 |
| **API base (debug)** | `https://api-staging.krishifarms.com/api/v1/` |
| **API base (release)** | `https://api.krishifarms.com/api/v1/` |

---

## 1. Project Overview

KrishiFarms Mobile is an **internal deployment** Android app (not Play Store). It supports agricultural supply-chain CRM workflows:

- Register and manage **farmers** and **farms**
- Record **crop procurement** with weighment data
- Track **workers**, **work orders**, and **attendance**
- Log **expenses** with bill attachments
- Capture and upload **documents** (Aadhaar, land records, receipts)
- **Offline-first**: all writes go to Room immediately; background sync pushes to server

Designed for 3–10 users initially, scaling to 100+ without architectural rework. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full target design.

---

## 2. Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + Clean Architecture (feature packages) |
| DI | Hilt + KSP |
| Networking | Retrofit, OkHttp, Kotlinx Serialization |
| Local DB | Room (schema export to `app/schemas/`) |
| Preferences | DataStore Preferences + EncryptedSharedPreferences (tokens) |
| Background work | WorkManager + Hilt Worker |
| Images / camera | Coil, CameraX |
| Navigation | Navigation Compose |
| i18n | `values/strings.xml` + `values-te/strings.xml` (English + Telugu) |

---

## 3. Backend

| Property | Detail |
|----------|--------|
| Framework | **FastAPI** |
| Auth | **JWT** — `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` |
| API prefix | **`/api/v1/`** (configured via `BuildConfig.API_BASE_URL`) |
| Sync | `POST /sync/push`, `GET /sync/pull` (see sync engine docs) |
| Media | `POST /media/upload` (multipart) |

Per-module CRUD endpoints follow REST conventions: `/farmers`, `/procurements`, `/expenses`, `/workers`, etc.

---

## 4. Branch & Repository

```bash
git clone https://github.com/gvsharma/krishifarms-mobile.git
cd krishifarms-mobile
git checkout initial-commit
```

All active development on this branch lives in a **single `:app` module**. Future Gradle splits are commented in `settings.gradle.kts`.

---

## 5. Project Structure

### Gradle layout

```
krishifarms-mobile/
├── app/                          # Application module (all code today)
│   ├── build.gradle.kts
│   ├── schemas/                  # Room schema exports
│   └── src/main/
│       ├── java/com/krishifarms/mobile/
│       └── res/
│           ├── values/strings.xml
│           └── values-te/strings.xml
├── docs/
│   └── ARCHITECTURE.md           # Full architecture design doc
├── gradle/
│   ├── libs.versions.toml        # Version catalog
│   └── wrapper/
├── build.gradle.kts              # Root plugins
└── settings.gradle.kts
```

### Package tree (`com.krishifarms.mobile`)

| Package | Purpose |
|---------|---------|
| *(root)* | `KrishiFarmsApplication`, `MainActivity` |
| `core/common` | `Result`, `SyncStatus`, `Resource`, dispatchers, network monitor |
| `core/di` | Hilt modules: `AppModule`, `NetworkModule`, `DatabaseModule`, `RepositoryModule` |
| `core/network` | Retrofit services, DTOs, interceptors, `safeApiCall`, token refresh |
| `core/database` | `KrishiFarmsDatabase`, entities, DAOs, type converters, `SyncMetadata` |
| `core/data` | Shared repository implementations (e.g. `SyncRepositoryImpl`) |
| `core/domain` | Shared domain models and repository interfaces |
| `core/navigation` | `KrishiFarmsNavHost`, `MainNavGraph`, `Routes`, feature route objects |
| `core/sync` | **Sync engine** — `SyncEngine`, handlers, workers, conflict resolver, DI |
| `core/ui` | Theme, shared composables (`SyncStatusIcon`, `FeaturePlaceholderScreen`) |
| `core/util` | Utilities |
| `feature/auth` | Login, JWT session, encrypted token storage |
| `feature/dashboard` | Home KPI cards, pull-to-refresh stats |
| `feature/farmer` | Farmer CRUD — list, detail, form, repository, sync |
| `feature/procurement` | Procurement list, detail, form, repository, sync |
| `feature/expense` | Expense list, detail, form, bill picker, repository, sync |
| `feature/worker` | Workers, work orders, attendance, repositories |
| `feature/document` | Document domain models and repository interface |
| `feature/farmers` | Legacy/alternate farmer package stub (prefer `feature/farmer`) |

Each feature follows: `data/` → `domain/` → `presentation/` → `di/` → `navigation/` (when wired).

---

## 6. Implemented Modules

Status reflects **code completeness** and **navigation wiring** in `MainNavGraph`.

| Module | Code | Nav wired | Notes |
|--------|------|-----------|-------|
| **Auth** | ✅ | ✅ | Mobile-number login, JWT, encrypted tokens, session gate |
| **Dashboard** | ✅ | ✅ | KPI cards navigate to feature routes |
| **Farmer** | ✅ | ✅ | Full CRUD + `FarmerSyncHandler`; reference implementation |
| **Procurement** | ✅ | ✅ | List, detail, create form + `ProcurementSyncHandler` (create-focused) |
| **Worker** | ✅ | ✅ | Workers, work orders, attendance + `WorkerSyncHandler` |
| **Expense** | ✅ | ✅ | List, detail, form + `ExpenseSyncHandler`; dashboard KPI navigates to list |
| **Document** | 🔶 | ❌ | Domain + `DocumentSyncHandler` + `DocumentUploadWorker`; no UI |
| **Sync engine** | ✅ | — | Implemented — queue, handlers, WorkManager; see [docs/SYNC_ENGINE.md](docs/SYNC_ENGINE.md) |
| **Farms** | ❌ | stub | `FeatureStubScreen` only |
| **Farmer payments** | ❌ | stub | Placeholder |
| **Collections / Payments** | ❌ | stub | Placeholder |
| **Vehicles / Trips** | ❌ | stub | Placeholder |
| **Assets / Rentals** | ❌ | stub | Placeholder |
| **Documents** | ✅ | ✅ | CameraX capture, gallery, preview, compress, upload; drawer → list |
| **Settings** | ❌ | stub | Logout hook present on stub |
| **Sync status UI** | 🔶 | ❌ | `SyncStatusIndicator` + `SyncDebugScreen` in `core/ui`; `Routes.SYNC` still stub |

Legend: ✅ complete · 🔶 partial · ❌ not started / stub only

---

## 7. Architecture Patterns

### Clean / MVVM

```
Compose Screen  →  ViewModel (StateFlow<UiState>)  →  Repository interface
                                                          ↓
                                              RepositoryImpl (Room + Retrofit + SyncEngine)
```

- **Presentation**: stateless composables, immutable `UiState`, events to ViewModel
- **Domain**: pure Kotlin models and repository contracts
- **Data**: Room entities, Retrofit APIs, mappers, repository implementations

### Offline-first

1. User action → write to **Room immediately** → UI shows success
2. Repository enqueues operation via **`OfflineSyncEngine`** (`core/sync`)
3. **WorkManager** (`SyncWorker`, entity-specific workers) processes queue when online
4. **`SyncHandler`** per entity type pushes to FastAPI and updates local state

### Sync queue

- **`sync_queue`** table: pending operations with idempotency keys
- **`SyncMetadata`** embedded on entities: `syncStatus`, `lastSyncedAt`, `localUpdatedAt`, `syncError`, `isDeleted`
- Handlers registered in `core/sync/di/SyncModule.kt` via Hilt multibinding

### Repository pattern

- Interface in `feature/<name>/domain/repository/`
- Implementation in `feature/<name>/data/repository/`
- Hilt `@Binds` in `feature/<name>/di/<Name>Module.kt`
- Reads always expose `Flow` from Room; writes set `SyncStatus.PENDING_*` and enqueue sync

---

## 8. Navigation

```
MainActivity
  └── KrishiFarmsNavHost          # Root nav + session gate
        ├── SESSION_LOADING       # Token/session restore
        ├── LOGIN                 # LoginScreen
        └── MAIN_GRAPH            # Authenticated shell
              └── MainNavGraph    # Drawer + TopAppBar + feature NavHost ("main shell")
                    ├── DASHBOARD
                    ├── farmerGraph()      # feature/farmer/navigation/
                    ├── workerRoutes()     # feature/worker/navigation/
                    ├── procurement routes # inline + ProcurementRoutes
                    └── FeatureStubScreen  # unimplemented features
```

**Session gate** (`KrishiFarmsNavHost`): observes `AuthViewModel.sessionState` and redirects among loading, login, and main graph.

**Main shell** (`MainNavGraph`): `ModalNavigationDrawer` + `Scaffold` + inner `NavHost`. Drawer lists all modules from `MainFeatureDestinations`.

### Registering a new feature route

1. Add route constants to `core/navigation/Routes.kt` (or `<Feature>Routes.kt`)
2. Create `feature/<name>/navigation/<Feature>Navigation.kt` with `NavGraphBuilder` extension
3. Call the extension inside `MainNavGraph`'s `NavHost { }` block
4. Replace `FeatureStubScreen` composable for that route

**Reference:** `feature/farmer/navigation/FarmerNavigation.kt`, `feature/worker/navigation/WorkerNavigation.kt`

---

## 9. Key Conventions

### Naming

| Artifact | Pattern | Example |
|----------|---------|---------|
| Screen | `<Entity><Action>Screen` | `FarmerListScreen` |
| ViewModel | `<Entity><Action>ViewModel` | `FarmerListViewModel` |
| UiState | `<Entity><Action>UiState` | `FarmerListUiState` |
| Repository | `<Entity>Repository` | `FarmerRepository` |
| Entity | `<Entity>Entity` | `FarmerEntity` |
| Hilt module | `<Feature>Module` | `FarmerModule` |

### UiState pattern

Each screen exposes `StateFlow<UiState>` with fields for content, loading, error, and optional sync state. ViewModels use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`.

### Sync fields on entities

All syncable Room entities embed `SyncMetadata`:

```kotlin
@Embedded val sync: SyncMetadata = SyncMetadata()
// syncStatus, lastSyncedAt, localUpdatedAt, syncError, isDeleted
```

Use `SyncStatus` enum from `core/common/SyncStatus.kt`. Display pending state with `SyncStatusIcon`.

### Bilingual strings

- English: `app/src/main/res/values/strings.xml`
- Telugu: `app/src/main/res/values-te/strings.xml`
- Use `stringResource(R.string.*)` in Compose — never hardcode user-facing text
- Domain fields may carry bilingual data (e.g. farmer `name` + `nameTe` when added)

### API / DTO conventions

- Kotlinx Serialization with `@SerialName` for snake_case backend fields
- Retrofit services created from `@Named("authenticated_retrofit") Retrofit` in feature DI modules

---

## 10. Build Instructions

### Prerequisites

- JDK 17
- Android SDK 35 (compile/target)
- Android Studio Ladybug or newer recommended

### Setup

1. **Create `local.properties`** at project root (gitignored):

   ```properties
   sdk.dir=/path/to/Android/sdk
   ```

2. **Build debug APK**:

   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on device/emulator**:

   ```bash
   ./gradlew installDebug
   ```

4. **Run unit tests**:

   ```bash
   ./gradlew test
   ```

Debug builds point at the **staging** API. Release builds use production URL and ProGuard.

---

## 11. For AI Agents

This section helps agentic IDEs implement features consistently without re-discovering patterns each session.

### Where to start for a new feature

1. Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) §3 (package structure) and §7 (repository design)
2. Study a **complete reference module**: `feature/farmer` or `feature/procurement`
3. Check `MainNavGraph.kt` for whether the route is stubbed or wired
4. Check `core/database/entity/Entities.kt` and `Daos.kt` for existing tables
5. Check `core/sync/di/SyncModule.kt` for registered sync handlers

### Files to read first

| Purpose | Path |
|---------|------|
| App entry + DI root | `KrishiFarmsApplication.kt`, `core/di/` |
| Navigation | `core/navigation/KrishiFarmsNavHost.kt`, `MainNavGraph.kt`, `Routes.kt` |
| Database | `core/database/KrishiFarmsDatabase.kt`, `entity/Entities.kt`, `dao/Daos.kt` |
| Sync engine | `core/sync/SyncEngine.kt`, `core/sync/di/SyncModule.kt`, `core/sync/handler/` |
| Reference feature | `feature/farmer/` (full stack) or `feature/procurement/` |
| Network setup | `core/di/NetworkModule.kt`, `core/network/ApiServices.kt` |

### Patterns to follow

Copy the **farmer** or **procurement** module structure:

```
feature/<name>/
├── data/
│   ├── remote/       # Retrofit API + DTOs
│   ├── mapper/       # Entity ↔ Domain ↔ DTO
│   └── repository/   # RepositoryImpl
├── domain/
│   ├── model/
│   └── repository/   # Interface
├── presentation/
│   ├── list/         # Screen + ViewModel + UiState
│   ├── detail/
│   └── form/
├── di/<Name>Module.kt
└── navigation/<Name>Navigation.kt
```

### Do NOT duplicate sync / upload logic

- **Enqueue changes** via `OfflineSyncEngine` and `SyncEnqueueExtensions` (`enqueueCreate`, `enqueueUpdate`, `enqueueDelete`)
- **Add a `SyncHandler`** in `core/sync/handler/` and register it in `SyncModule.kt`
- **Document / media uploads** → use `DocumentSyncHandler` + `DocumentUploadWorker` in `core/sync/worker/` — do not build a parallel upload pipeline
- **Schedule background work** via `SyncScheduler` / existing WorkManager workers

### Hilt module location pattern

- **Core bindings**: `core/di/` (`NetworkModule`, `DatabaseModule`, `RepositoryModule`, `SyncModule`)
- **Feature bindings**: `feature/<name>/di/<Name>Module.kt`
  - `@Binds` repository interface → impl
  - `@Provides` Retrofit API from `@Named("authenticated_retrofit")`

### Navigation registration pattern

1. Define routes in `Routes.kt` or `<Feature>Routes.kt`
2. Implement `fun NavGraphBuilder.<feature>Graph(navController: NavController)` in `feature/<name>/navigation/`
3. Add `import` and call inside `MainNavGraph` → `NavHost { }`
4. Remove the corresponding `FeatureStubScreen` composable

Example (farmer):

```kotlin
// MainNavGraph.kt NavHost block
farmerGraph(navController)
```

### Known open items

| Item | Detail |
|------|--------|
| **Login field mismatch** | App sends `mobile` in `LoginRequest`; confirm backend accepts `mobile` vs `email` — align DTO and validation if backend differs |
| **Document UI missing** | Sync/upload infrastructure exists; no capture/list screens |
| **Settings / sync status UI** | Settings stub; wire `SyncDebugScreen` at `Routes.SYNC` (`SyncStatusIndicator` in `core/ui`) |
| **Multi-module Gradle split** | Planned in `settings.gradle.kts` comments; not yet executed |
| **CI / Android SDK** | No `.github/workflows` yet — Android SDK not configured in CI |
| **Package name in ARCHITECTURE.md** | Doc uses `com.krishifarms.crm`; **actual code uses `com.krishifarms.mobile`** |

---

## 12. Documentation Index

| Document | Description |
|----------|-------------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Full architecture: layers, navigation topology, offline sync design, Room schema, security, rollout phases |
| [docs/SYNC_ENGINE.md](docs/SYNC_ENGINE.md) | Sync engine reference: queue, handlers, WorkManager, conflict resolution, UI components |

---

## License & Deployment

Internal KrishiFarms use only. Not distributed via public app stores.
