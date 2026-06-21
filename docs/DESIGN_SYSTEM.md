# KrishiFarms Design System

**Inspiration:** [Canopia AI — Precision Agriculture Crop Intelligence App](https://dribbble.com/shots/27293594-Canopia-AI-Precision-Agriculture-Crop-Intelligence-App) (Dribbble concept). Colors, typography, and component styling are recreated from openly licensed sources — no Dribbble image assets are bundled.

**Implementation:** `app/src/main/java/com/krishifarms/mobile/core/ui/theme/` · `core/ui/components/KfCard.kt`

---

## Design philosophy

Precision agriculture UI with:

- Deep forest greens for brand trust and field context
- Sage/cream surfaces for outdoor readability
- Mint and lime accents for positive metrics and highlights
- Teal secondary for data/intelligence cues
- Generous 16dp card rounding (Canopia-style soft cards)
- Data-first hierarchy — bold KPI numbers, muted labels

---

## Color tokens

### Light theme

| Token | Hex | Usage |
|-------|-----|-------|
| `CanopiaGreen` / `primary` | `#1B4332` | Brand, buttons, icons |
| `CanopiaGreenBright` | `#40916C` | Hover/active states |
| `CanopiaGreenLight` | `#52B788` | Accents |
| `CanopiaMint` / `primaryContainer` | `#D8F3DC` | Icon backgrounds, chips |
| `CanopiaSageBackground` / `background` | `#F4F7F0` | Page background |
| `CanopiaSurface` / `surface` | `#FFFFFF` | Cards, sheets |
| `CanopiaSurfaceVariant` / `surfaceVariant` | `#E8F0E4` | Alternate surfaces |
| `CanopiaForestText` / `onSurface` | `#1A1F16` | Primary text |
| `CanopiaMutedText` / `onSurfaceVariant` | `#5C6B5E` | Secondary text |
| `CanopiaOutline` / `outline` | `#B8C4B8` | Borders, dividers |
| `CanopiaTeal` / `secondary` | `#2A9D8F` | Data/intelligence accent |
| `CanopiaLime` / `tertiary` | `#A7C957` | Positive KPI badges |
| `CanopiaAmber` | `#E9C46A` | Warnings, harvest accent |
| `CanopiaError` / `error` | `#D62828` | Validation, failures |

### Dark theme

| Token | Hex | Usage |
|-------|-----|-------|
| `CanopiaDarkBackground` / `background` | `#0C1404` | Page background |
| `CanopiaDarkSurface` / `surface` | `#162016` | Cards |
| `CanopiaDarkSurfaceVariant` / `surfaceVariant` | `#1E2A1E` | Elevated surfaces |
| `CanopiaDarkPrimary` / `primary` | `#74C69D` | Brand on dark |
| `CanopiaDarkOnSurface` / `onSurface` | `#E8F0E4` | Primary text |

All tokens are defined in `Color.kt` and wired through `Theme.kt` Material 3 color schemes.

---

## Typography

### Font families

| Role | Family | Weights | Source |
|------|--------|---------|--------|
| Display, Headline, Title | **Plus Jakarta Sans** | 400, 500, 600, 700 | [Google Fonts](https://fonts.google.com/specimen/Plus+Jakarta+Sans) |
| Body, Label | **Inter** | 400, 500, 600, 700 | [Google Fonts](https://fonts.google.com/specimen/Inter) |

Font files live in `app/src/main/res/font/`. License: **SIL Open Font License 1.1** — see `assets/fonts/OFL.txt`.

### Type scale

| Material role | Size | Weight | Font |
|---------------|------|--------|------|
| `displayLarge` | 32sp | Bold | Plus Jakarta Sans |
| `displaySmall` | 24sp | SemiBold | Plus Jakarta Sans |
| `headlineMedium` | 20sp | SemiBold | Plus Jakarta Sans |
| `headlineSmall` | 18sp | SemiBold | Plus Jakarta Sans |
| `titleLarge` | 20sp | SemiBold | Plus Jakarta Sans |
| `titleMedium` | 16sp | Medium | Plus Jakarta Sans |
| `bodyLarge` | 16sp | Regular | Inter |
| `bodyMedium` | 14sp | Regular | Inter |
| `bodySmall` | 12sp | Regular | Inter |
| `labelLarge` | 14sp | Medium | Inter |
| `labelMedium` | 12sp | Medium | Inter |

Defined in `Type.kt`. Telugu locale will add Noto Sans Telugu in a future i18n pass (see [PRODUCT_ARCHITECTURE.md §4.2](PRODUCT_ARCHITECTURE.md#42-material-3-theme)).

---

## Shapes

| Token | Radius | Usage |
|-------|--------|-------|
| `extraSmall` | 4dp | Chips, small badges |
| `small` | 8dp | Text fields, icon containers |
| `medium` | 16dp | Cards (`KfCard`), buttons |
| `large` | 20dp | Hero icon containers, bottom sheets |
| `extraLarge` | 28dp | Feature highlights |

Defined in `Shape.kt` as `KrishiFarmsShapes`.

---

## Components (implemented)

| Component | File | Spec |
|-----------|------|------|
| Theme | `Theme.kt` | `KrishiFarmsTheme` — light + dark M3 schemes |
| Card | `KfCard.kt` | 16dp radius, 1dp elevation, optional click |
| KPI tile | `DashboardStatCard.kt` | Icon in mint container, lime count badge, headline metric |

Full `Kf*` catalog target is in [PRODUCT_ARCHITECTURE.md §4.4](PRODUCT_ARCHITECTURE.md#44-component-catalog-coreui).

---

## Reference screens

| Screen | Path | Shows |
|--------|------|-------|
| Login | `feature/auth/presentation/login/LoginScreen.kt` | Sage background, agriculture icon hero, `KfCard` form, rounded fields |
| Dashboard | `feature/dashboard/presentation/DashboardScreen.kt` | Headline title, sage background, KPI grid with mint icon tiles |

---

## Font attribution (OFL 1.1)

```
Copyright 2020 The Plus Jakarta Sans Project Authors
(https://github.com/tokotype/PlusJakartaSans)

Copyright 2020 The Inter Project Authors
(https://github.com/rsms/inter)

Licensed under the SIL Open Font License, Version 1.1.
```

---

## Related docs

| Document | Purpose |
|----------|---------|
| [PRODUCT_ARCHITECTURE.md](PRODUCT_ARCHITECTURE.md) | Full component catalog, layout patterns, rollout phases |
| [AGENTS.md](AGENTS.md) | Agent onboarding — design system status |

*Last updated: Canopia-inspired theme on branch `initial-commit`.*
