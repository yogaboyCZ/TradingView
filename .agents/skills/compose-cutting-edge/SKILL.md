---
name: compose-cutting-edge
description: Apply this project's cutting-edge Jetpack Compose UI policy. Use for creating, changing, reviewing, or debugging Compose screens, navigation, adaptive/foldable layouts, motion, loading states, carousels, Material 3 theming, and responsive UI dependencies.
---

# Compose Cutting Edge

Prefer the newest suitable official Compose and Material 3 APIs. Keep experimental APIs unless they fail to compile with the project's toolchain or cause a demonstrated severe runtime, performance, accessibility, or UX defect.

## Workflow

1. Inspect the current Compose BOM, Kotlin, AGP, min SDK, dependencies, and existing architecture before choosing an API.
2. Verify unstable API names and artifacts against current official Android documentation or AndroidX release notes. Do not rely on remembered alpha APIs.
3. Choose the canonical component that matches the product structure. Do not add a component merely because it is new.
4. Add the newest compatible artifact through `gradle/libs.versions.toml`; use `@OptIn` locally at the smallest reasonable scope.
5. Preserve theme tokens, accessibility semantics, state restoration, predictive back, fold posture handling, and runtime window resizing.
6. Compile affected modules and the app. Exercise compact, expanded/foldable, light, and dark configurations when the change affects layout or visuals.
7. Fall back only after recording the concrete compiler error or severe defect. Prefer the nearest working alpha/beta over an unnecessarily old stable API.

## Adaptive UI

- Default to Material 3 Adaptive canonical layouts rather than manual width branching.
- For this app's list-to-stock-detail flow, prefer `NavigableListDetailPaneScaffold`/`ListDetailPaneScaffold` with adaptive directives, hinge/posture awareness, pane navigation, predictive back, and pane motion.
- Use `NavigationSuiteScaffold` only after the app has multiple top-level destinations. It is not a replacement for list-detail navigation. Let its adaptive defaults choose bar versus rail before overriding layout type.
- Respond to live window changes, split screen, font scaling, tabletop/book posture, and occluding hinges. Avoid a fixed breakpoint as the sole foldable signal.
- Preserve deliberate custom shared-element or flip motion only when the canonical scaffold cannot express the required transition cleanly.

## Motion

- Prefer the current Material 3 expressive `MotionScheme` and component motion defaults over hand-tuned damping/easing constants.
- Use motion to communicate selection, hierarchy, navigation, expansion, loading, and continuity. Keep scroll-linked interactions continuous rather than threshold toggles.
- Respect reduced-motion/accessibility behavior and avoid perpetual decorative work when content is not visible.
- Validate spring-heavy or simultaneous animations on a constrained device/profile; reduce complexity only when measured jank or a severe defect exists.

## Components

- Prefer expressive Material loading indicators over legacy flat `CircularProgressIndicator` when the current dependency exposes a suitable API. Keep compact indicators legible and stable in constrained cards.
- Use Material carousels only for a genuine browsable visual collection. Choose multi-browse, uncontained, hero, or full-screen by content hierarchy; use supported snapping/state APIs for predictable tablet behavior. Do not turn the stock grid into a carousel without a product requirement.
- Start with component defaults and theme roles. Customize only to meet this app's glass/aurora visual system, contrast, interaction, or layout requirements.

## Theme and State

- Use `MaterialTheme.colorScheme`, typography, shapes, and motion scheme for standard semantic roles.
- Use `MaterialTheme.tradingColors` and `MaterialTheme.auroraColors` only for project-specific semantics and effects. Do not hardcode ARGB colors in feature composables.
- Hoist UI state, use lifecycle-aware collection, stable keys, saveable navigation state, and immutable models where appropriate.
- Enforce a strict MVI route/screen boundary. `*Route` composables own ViewModel lookup, lifecycle-aware state collection, `LaunchedEffect`, effect collection, navigation, focus/IME commands, and other imperative work. `*Screen` and child UI components must be pure state-to-UI functions that receive immutable state and propagate user events outward. Never add `LaunchedEffect` below a route composable.
- Keep feature screens small and componentized. Move substantial headers, search controls, grids, cards, charts, and similar reusable or independently testable UI into focused files instead of growing a monolithic `*Screen.kt`.
- Never declare product catalogs, demo entities, fallback market values, or other data-source content in a screen or presentation file. Define the contract/model in the domain layer and provide production or demo data through the appropriate data-layer repository.
- Never hardcode user-visible text in Kotlin. Put shared application strings in `core:design` resources, provide supported locale variants, and use typed domain values (for example enums) rather than localized strings across data/domain boundaries.

## Backend Boundary

- Do not introduce GraphQL, gRPC, sockets, or a backend rewrite merely to support UI polish.
- Keep the current REST repositories and Room cache unless product/API requirements justify a transport change. Prevent animation jank through cached state, asynchronous loading, and efficient rendering.

## Current Audit Baseline

- The project uses Compose BOM `2026.06.01`, Navigation 3, custom list-detail branching at `840.dp`, manually tuned motion, and legacy circular progress indicators.
- It does not currently use Material 3 Adaptive scaffolds, adaptive navigation suite, expressive motion presets, or Material carousel APIs.
- Treat this baseline as discoverable context, not permanent truth: re-inspect the code and catalog before every relevant change.
