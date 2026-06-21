# KrishiFarms Mobile — Agent & Developer Guide

**Primary onboarding doc for agentic IDEs (Cursor, Copilot, etc.) and engineers starting a new session.**

| | |
|---|---|
| **Audience** | AI coding agents, Android developers, reviewers |
| **Branch** | `initial-commit` |
| **Package** | `com.krishifarms.mobile` |
| **When to read** | First file in every new session before editing code |

---

## Quick orientation

**KrishiFarms Mobile** is an internal Android CRM for agricultural supply-chain operations: farmers, procurement, workforce, expenses, and document capture. It is **offline-first** — all writes go to Room immediately; a centralized sync engine pushes to a **FastAPI** backend when online.

| Layer | Stack |
|-------|-------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + Clean Architecture (feature packages) |
| DI | Hilt + KSP |
| Local DB | Room v4 (`app/schemas/`) |
| Network | Retrofit, OkHttp, Kotlinx Serialization |
| Background | WorkManager + Hilt Worker |
| Navigation | Navigation Compose (drawer shell) |
| i18n | `values/strings.xml` + `values-te/strings.xml` |

**Gradle:** single `:app` module today. Multi-module split is planned (see commented blocks in `settings.gradle.kts`).

**API bases:** debug → `https://api-staging.krishifarms.com/api/v1/` · release → `https://api.krishifarms.com/api/v1/`

> **Doc note:** [ARCHITECTURE.md](ARCHITECTURE.md) uses package name `com.krishifarms.crm` in places — **actual code uses `com.krishifarms.mobile`**.

---

## Session startup checklist

Read these files **in order** before implementing or refactoring:

| # | Purpose | Path |
|---|---------|------|
| 1 | Agent guide (this file) | `docs/AGENTS.md` |
| 2 | Project overview & build | `README.md` |
| 3 | Target architecture design | `docs/ARCHITECTURE.md` |
| 4 | Sync engine reference | `docs/SYNC_ENGINE.md` |
| 5 | Navigation & stubs | `core/navigation/MainNavGraph.kt`, `Routes.kt` |
| 6 | Database schema | `core/database/KrishiFarmsDatabase.kt`, `entity/Entities.kt`, `dao/Daos.kt` |
| 7 | Sync registration | `core/sync/SyncEngine.kt`, `core/sync/di/SyncModule.kt`, `core/sync/handler/` |
| 8 | Reference feature (copy patterns) | `feature/farmer/` or `feature/procurement/` |
| 9 | Strings (both locales) | `app/src/main/res/values/strings.xml`, `values-te/strings.xml` |

Then grep for the feature route in `MainNavGraph.kt` to see if it is wired or still a `FeatureStubScreen`.

---

## Repository map

### Top-level directories

| Path | Description |
|------|-------------|
| `app/` | Android application module — all Kotlin source, resources, Room schemas |
| `app/src/main/java/com/krishifarms/mobile/` | Root package (`KrishiFarmsApplication`, `MainActivity`) |
| `app/src/main/res/` | Compose themes, bilingual strings, drawables, `file_paths.xml` |
| `app/schemas/` | Exported Room database JSON schemas (CI / migration reference) |
| `docs/` | Architecture, sync, and agent documentation |
| `gradle/` | Version catalog (`libs.versions.toml`) and Gradle wrapper |
| `.cursor/rules/` | Cursor agent rules (incl. documentation maintenance) |
| `build.gradle.kts` | Root Gradle plugins |
| `settings.gradle.kts` | Module includes (`:app` only; future splits commented) |

### `core/` packages (`com.krishifarms.mobile.core`)

| Package | Description |
|---------|-------------|
| `core/common` | `Result`, `SyncStatus`, `Resource`, `IdGenerator`, `NetworkMonitor`, dispatchers |
| `core/di` | Hilt: `AppModule`, `NetworkModule`, `DatabaseModule`, `RepositoryModule` |
| `core/network` | Retrofit services, DTOs, interceptors, `safeApiCall`, token refresh |
| `core/database` | `KrishiFarmsDatabase`, entities, DAOs, type converters, `SyncMetadata` |
| `core/data` | Shared repository impls (e.g. `SyncRepositoryImpl`) |
| `core/domain` | Shared domain models and repository interfaces |
| `core/navigation` | `KrishiFarmsNavHost`, `MainNavGraph`, `Routes`, stub screens |
| `core/sync` | **Sync engine** — `SyncEngine`, handlers, workers, conflict resolver, DI |
| `core/ui` | Theme, `SyncStatusIcon`, `SyncStatusIndicator`, `SyncDebugScreen`, placeholders |
| `core/util` | `ImageCompressor`, `DocumentFileManager`, `CameraXCapture`, attachment storage |

### `feature/` packages

| Package | Description |
|---------|-------------|
| `feature/auth` | Login, JWT session, encrypted token storage, session gate |
| `feature/dashboard` | Home KPI cards, pull-to-refresh stats |
| `feature/farmer` | **Reference module** — farmer CRUD, list/detail/form, `FarmerSyncHandler` |
| `feature/procurement` | Procurement list, detail, create form, `ProcurementSyncHandler` |
| `feature/expense` | Expense list, detail, form, bill picker, `ExpenseSyncHandler` |
| `feature/worker` | Workers, work orders, attendance, `WorkerSyncHandler` |
| `feature/document` | Document list, CameraX capture, upload, preview, `DocumentSyncHandler` |

Each feature follows: `data/` → `domain/` → `presentation/` → `di/` → `navigation/` (when wired).

---

## Module implementation status

Status reflects **code completeness** and **navigation wiring** in `MainNavGraph.kt` (verified against `initial-commit` branch).

| Module | Code | Nav wired | Notes |
|--------|:----:|:---------:|-------|
| **Auth** | ✅ | ✅ | Mobile login, JWT, encrypted tokens, session gate |
| **Dashboard** | ✅ | ✅ | KPI cards navigate to feature routes |
| **Farmer** | ✅ | ✅ | Full CRUD + `FarmerSyncHandler` — **copy this for new entities** |
| **Procurement** | ✅ | ✅ | List, detail, create; sync is create-focused |
| **Worker** | ✅ | ✅ | Workers, work orders, attendance + `WorkerSyncHandler` |
| **Expense** | ✅ | ✅ | List, detail, form + `ExpenseSyncHandler` |
| **Document** | ✅ | ✅ | List, CameraX capture, upload, preview; drawer → `Routes.DOCUMENTS` |
| **Sync engine** | ✅ | — | Queue, handlers, WorkManager — see [SYNC_ENGINE.md](SYNC_ENGINE.md) |
| **Farms** | ❌ | stub | `FeatureStubScreen` only |
| **Farmer payments** | ❌ | stub | Placeholder |
| **Collections** | ❌ | stub | Placeholder |
| **Payments** | ❌ | stub | Placeholder |
| **Vehicles** | ❌ | stub | Placeholder |
| **Vehicle trips** | ❌ | stub | Placeholder |
| **Assets** | ❌ | stub | Placeholder |
| **Rentals** | ❌ | stub | Placeholder |
| **Settings** | ❌ | stub | Logout hook on stub screen |
| **Sync status UI** | 🔶 | stub | `SyncStatusIndicator` + `SyncDebugScreen` exist; `Routes.SYNC` still uses stub |

Legend: ✅ complete · 🔶 partial · ❌ not started / stub only

**Keep this table current** when you ship or stub a module (see [Documentation maintenance contract](#documentation-maintenance-contract)).

---

## How to implement a new feature

### Step-by-step (with pattern sources)

| Step | Action | Copy from |
|------|--------|-----------|
| 1 | Confirm entity does not already exist | `core/database/entity/Entities.kt`, `dao/Daos.kt` |
| 2 | Add Room entity + DAO + migration if needed | `FarmerEntity` pattern in `Entities.kt` |
| 3 | Add domain model + repository interface | `feature/farmer/domain/` |
| 4 | Add Retrofit API + DTOs + mapper | `feature/farmer/data/remote/`, `data/mapper/` |
| 5 | Implement repository (Room first, enqueue sync) | `feature/farmer/data/repository/FarmerRepositoryImpl.kt` |
| 6 | Add `SyncHandler` + register in `SyncModule.kt` | `core/sync/handler/FarmerSyncHandler.kt` |
| 7 | Create Hilt module (`@Binds` repo, `@Provides` API) | `feature/farmer/di/FarmerModule.kt` |
| 8 | Build list / detail / form screens + ViewModels + UiState | `feature/farmer/presentation/` |
| 9 | Define routes | `Routes.kt` or `feature/<name>/navigation/<Name>Routes.kt` |
| 10 | Add `NavGraphBuilder` extension | `feature/farmer/navigation/FarmerNavigation.kt` |
| 11 | Wire in `MainNavGraph` NavHost; remove stub | `core/navigation/MainNavGraph.kt` |
| 12 | Add drawer destination if new top-level module | `Routes.kt` → `MainFeatureDestinations` |
| 13 | Add strings **in both** `strings.xml` and `strings-te.xml` | Existing `nav_*`, `*_title` keys |
| 14 | Update module status table in this file + `README.md` | § Module implementation status |

### Target package layout

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

### Naming conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Screen | `<Entity><Action>Screen` | `FarmerListScreen` |
| ViewModel | `<Entity><Action>ViewModel` | `FarmerListViewModel` |
| UiState | `<Entity><Action>UiState` | `FarmerListUiState` |
| Repository | `<Entity>Repository` | `FarmerRepository` |
| Entity | `<Entity>Entity` | `FarmerEntity` |
| Hilt module | `<Feature>Module` | `FarmerModule` |

---

## Architecture invariants

Do not violate these without an architecture review and doc updates.

### Offline-first

1. User action → write to **Room immediately** → UI shows success.
2. Repository sets `SyncStatus.PENDING_*` on embedded `SyncMetadata`.
3. Repository enqueues via **`OfflineSyncEngine`** (`enqueueCreate` / `enqueueUpdate` / `enqueueDelete`).
4. **WorkManager** (`SyncWorker`) processes `sync_queue` when online.

### Sync queue (centralized — no per-feature workers)

- Queue table: `sync_queue` (`SyncOperationEntity`).
- Handlers: one `SyncHandler` per entity type, registered in `core/sync/di/SyncModule.kt` via Hilt multibinding (`@StringKey("FARMER")`, etc.).
- Document/media uploads: `DocumentSyncHandler` + `DocumentUploadWorker` — **do not** build a parallel upload pipeline.
- Legacy feature workers (`FarmerSyncWorker`, `ExpenseSyncWorker`) delegate to the central engine; do not add new ones.

### MVVM + Clean Architecture

```
Compose Screen  →  ViewModel (StateFlow<UiState>)  →  Repository interface
                                                          ↓
                                              RepositoryImpl (Room + Retrofit + SyncEngine)
```

- ViewModels expose `StateFlow` with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`.
- Reads always return `Flow` from Room.
- Domain layer has no Android imports.

### Hilt

- Core bindings: `core/di/`.
- Feature bindings: `feature/<name>/di/<Name>Module.kt`.
- Retrofit APIs: `@Provides` from `@Named("authenticated_retrofit") Retrofit`.

### Navigation wiring

```
MainActivity
  └── KrishiFarmsNavHost          # Session gate (loading → login → main)
        └── MainNavGraph          # Drawer + TopAppBar + feature NavHost
              ├── farmerGraph()
              ├── workerRoutes()
              ├── documentRoutes()
              └── FeatureStubScreen  # unimplemented modules
```

Session gate: `KrishiFarmsNavHost` observes `AuthViewModel.sessionState`.

---

## Do NOT do (common agent mistakes)

| Mistake | Why it's wrong | Do instead |
|---------|----------------|------------|
| Duplicate sync logic in feature workers | Central engine replaced ad-hoc sync | `OfflineSyncEngine.enqueue()` + `SyncHandler` |
| Use `feature/farmers` package | Does not exist; use `feature/farmer` | `feature/farmer/` |
| Skip `values-te/strings.xml` | App is bilingual (en + te) | Add every new `R.string.*` in both files |
| Hardcode user-facing text in Compose | Breaks i18n | `stringResource(R.string.*)` |
| Block UI on network for reads | Violates offline-first | Always read from Room `Flow` |
| Create parallel media upload pipeline | Document infra already exists | `DocumentRepository` + `DocumentUploadWorker` |
| Forget `SyncModule` handler registration | Ops sit in queue forever | `@Binds @IntoMap @StringKey("ENTITY")` |
| Wire nav without removing stub | Duplicate or dead routes | Replace `FeatureStubScreen` composable |
| Trust ARCHITECTURE.md package name | Doc says `com.krishifarms.crm` | Use `com.krishifarms.mobile` |
| Leave module status tables stale | Misleading for next agent session | Update `docs/AGENTS.md` + `README.md` |
| Add Gradle modules without team agreement | Repo is single-module monolith today | Keep code in `:app` until split is executed |

---

## Testing & build commands

**Prerequisites:** JDK 17, Android SDK 35, `local.properties` with `sdk.dir=...`

```bash
# Debug APK
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Unit tests
./gradlew test

# Lint (when configured)
./gradlew lint
```

Debug builds use the **staging** API. Release uses production URL and ProGuard.

No CI workflows (`.github/workflows`) are configured yet.

---

## Documentation maintenance contract

**Every code change must update relevant docs in the same PR/commit.** Cursor enforces this via `.cursor/rules/documentation-maintenance.mdc`.

| Change type | Update |
|-------------|--------|
| New / completed feature module | Module table in **this file** + `README.md` § Implemented Modules |
| New package or top-level dir | Repository map in **this file** |
| Navigation route added/removed | **this file** (status table) + `README.md` § Navigation |
| Sync engine behavior | `docs/SYNC_ENGINE.md` |
| Architecture / layer boundaries | `docs/ARCHITECTURE.md` |
| Build / SDK / API URL changes | `README.md` Quick Reference |
| New doc file | `README.md` Documentation section + link from **this file** |

**`docs/AGENTS.md` is the single source of truth for agent onboarding.** Keep the module status table accurate — never leave it stale after shipping or stubbing a feature.

---

## Related documentation

| Document | Purpose |
|----------|---------|
| [README.md](../README.md) | Human-readable overview, tech stack, build instructions |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Full target architecture: layers, navigation topology, Room schema, security |
| [SYNC_ENGINE.md](SYNC_ENGINE.md) | Sync queue, handlers, WorkManager, conflict resolution, UI components |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Contribution guidelines incl. doc update expectations for humans |

---

## Known open items

| Item | Detail |
|------|--------|
| Login field | App sends `mobile` in `LoginRequest` — confirm backend accepts `mobile` vs `email` |
| Sync debug route | Wire `SyncDebugScreen` at `Routes.SYNC` (currently `FeatureStubScreen`) |
| Settings | Stub only; needs profile, language, sync status |
| Multi-module Gradle split | Commented in `settings.gradle.kts`; not executed |
| CI | No GitHub Actions; Android SDK not in CI |
| ARCHITECTURE.md package name | Target doc uses `com.krishifarms.crm`; code uses `com.krishifarms.mobile` |

---

*Last verified against branch `initial-commit`. Update this file when module status or repo structure changes.*
