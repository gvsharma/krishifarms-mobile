# KrishiFarms Mobile

Internal Android CRM for KrishiFarms field and office operations — farmer management, crop procurement, workforce, expenses, and document capture. Built **offline-first** for low-connectivity rural areas; syncs to a FastAPI backend when online.

> **🤖 AI agents (Cursor, Copilot, etc.):** Start every session with **[docs/AGENTS.md](docs/AGENTS.md)** — session checklist, repository map, module status, implementation patterns, and documentation maintenance contract.

> **Audience:** Engineers and agentic IDEs starting a new session on this repo.

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

Designed for 3–10 users initially, scaling to 100+ without architectural rework. See [docs/PRODUCT_ARCHITECTURE.md](docs/PRODUCT_ARCHITECTURE.md) for the product spec (design system, bottom nav, roles, phased rollout) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for layer-level technical design.

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
│   ├── AGENTS.md                 # Primary AI agent onboarding guide
│   ├── DESIGN_SYSTEM.md          # Colors, typography, shapes, KfCard, font attribution
│   ├── PRODUCT_ARCHITECTURE.md   # Product spec: design system, nav, phases, migration
│   ├── RBAC_IMPLEMENTATION_PLAN.md  # Permission-driven UI rollout plan
│   ├── ARCHITECTURE.md           # Layer architecture reference
│   └── SYNC_ENGINE.md            # Offline sync engine reference
├── .cursor/rules/
│   └── documentation-maintenance.mdc
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

Status reflects **code completeness** and **navigation wiring** in `MainNavGraph`. **Target phase** maps to [docs/PRODUCT_ARCHITECTURE.md](docs/PRODUCT_ARCHITECTURE.md) rollout plan.

| Module | Code | Nav wired | Target phase | Notes |
|--------|:----:|:---------:|:------------:|-------|
| **Auth** | ✅ | ✅ | 1 | JWT + `EncryptedSharedPreferences`; migrate to Proto DataStore + biometric |
| **Dashboard** | ✅ | ✅ | 1 | Refactor for bottom-nav Home tab |
| **Farmer** | ✅ | ✅ | 1–2 | Reference implementation; tabbed detail in Phase 2 |
| **Procurement** | ✅ | ✅ | 1 | List, detail, create + `ProcurementSyncHandler` |
| **Worker** | ✅ | ✅ | 1 | Workers, work orders, attendance |
| **Expense** | ✅ | ✅ | 2–3 | Approval flow Phase 3 |
| **Document** | ✅ | ✅ | 1 | CameraX capture, upload, preview |
| **Sync engine** | ✅ | — | 1–3 | See [docs/SYNC_ENGINE.md](docs/SYNC_ENGINE.md) |
| **Design system / Bottom nav** | 🔶 | ✅ | 1 | 5-tab `MainBottomNav` + dynamic RBAC drawer; full hub grids still stub |
| **Farms** | 🔶 | stub | 1 | Room entity exists; UI stub |
| **Collections** | ❌ | stub | 2 | Fast-entry — P0 Phase 2 |
| **Farmer payments / Payments** | ❌ / 🔶 | stub | 2–3 | Payments entity exists |
| **Vehicles / Trips / Assets / Rentals** | ❌ | stub | 4 | Fleet hub |
| **Settings / Sync UI** | ❌ / 🔶 | stub | 1 | Wire `SyncDebugScreen` |
| **Global search / Biometric / FCM** | ❌ | — | 2–4 | See product architecture doc |

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

**Main shell** (`MainNavGraph`): dynamic RBAC-filtered drawer + `MainBottomNav` (5 tabs) + guarded `NavHost`. Menu visibility from `accessibleModules`; route/action gates from `permissions`.

**RBAC:** Login/refresh responses include `roles`, `permissions`, `accessibleModules`. Debug builds use `RBAC_STRICT_MODE=false` (legacy: empty permissions → all modules). Release enforces strict mode.

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

**Full guide:** [docs/AGENTS.md](docs/AGENTS.md) — session startup checklist, repository map, module status table, step-by-step feature implementation, architecture invariants, do-not-do list, and documentation maintenance contract.

Quick pointers:

- **Reference modules:** `feature/farmer` (full CRUD) or `feature/procurement`
- **Navigation:** check `MainNavGraph.kt` for stubs vs wired routes
- **Sync:** `core/sync/SyncEngine.kt` + handlers in `core/sync/di/SyncModule.kt` — never duplicate sync logic
- **Docs rule:** `.cursor/rules/documentation-maintenance.mdc` — update docs with every code change

---

## 12. Documentation

| Document | Purpose |
|----------|---------|
| **[docs/AGENTS.md](docs/AGENTS.md)** | **Primary AI agent guide** — start here every session |
| **[docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md)** | **Design tokens** — Canopia-inspired colors, typography, shapes, components |
| **[docs/PRODUCT_ARCHITECTURE.md](docs/PRODUCT_ARCHITECTURE.md)** | **Product spec** — multi-module layout, design system, bottom nav, wireframes, roles, phased rollout, migration |
| **[docs/RBAC_IMPLEMENTATION_PLAN.md](docs/RBAC_IMPLEMENTATION_PLAN.md)** | **RBAC rollout** — permission-driven UI, session model, navigation guards, phased migration |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layer architecture: Clean Architecture, Room schema, security, sync protocol |
| [docs/SYNC_ENGINE.md](docs/SYNC_ENGINE.md) | Sync queue, handlers, WorkManager, conflict resolution |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guidelines and doc-update expectations for humans |
| [.cursor/rules/documentation-maintenance.mdc](.cursor/rules/documentation-maintenance.mdc) | Cursor rule enforcing doc updates with code changes |

---

## License & Deployment

Internal KrishiFarms use only. Not distributed via public app stores.
