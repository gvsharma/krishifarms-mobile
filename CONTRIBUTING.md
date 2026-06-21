# Contributing to KrishiFarms Mobile

Internal KrishiFarms project. Coordinate with the team before large architectural changes.

## Getting started

1. Clone the repo and check out `initial-commit`.
2. Read [`docs/AGENTS.md`](docs/AGENTS.md) — primary guide for humans and AI agents.
3. Create `local.properties` with your Android SDK path (see `README.md`).

## Code conventions

- Follow existing feature package layout (`feature/farmer` is the reference).
- Offline-first: Room writes first, sync via `OfflineSyncEngine`.
- Bilingual strings: always update both `values/strings.xml` and `values-te/strings.xml`.
- Use Hilt modules per feature; register sync handlers in `core/sync/di/SyncModule.kt`.

## Documentation updates (required)

**Update docs in the same PR as code changes.** This is enforced for AI agents via `.cursor/rules/documentation-maintenance.mdc`.

| If you change… | Also update… |
|----------------|--------------|
| Feature completeness or navigation wiring | `docs/AGENTS.md` module table + `README.md` § Implemented Modules |
| Sync engine behavior | `docs/SYNC_ENGINE.md` |
| Architecture or package structure | `docs/ARCHITECTURE.md` + `docs/AGENTS.md` repository map |
| Build / API / SDK settings | `README.md` |

`docs/AGENTS.md` is the canonical agent onboarding doc — keep its status table current.

## Build & test

```bash
./gradlew assembleDebug
./gradlew test
```

See `README.md` for full build instructions.
