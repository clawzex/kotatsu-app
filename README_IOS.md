# Kotatsu iOS UI

A fork of [Kotatsu](https://github.com/KotatsuApp/Kotatsu) redesigned with a premium iOS-inspired interface while preserving all original functionality.

## Design Philosophy

This fork transforms Kotatsu's Material Design 3 interface into an experience inspired by Apple Books, Apple Music, and iOS Settings — without removing any features, breaking APIs, or changing the underlying architecture.

## Visual Changes

- **iOS color system** — System Blue accent, grouped backgrounds (#F2F2F7 light / #000000 AMOLED dark)
- **Floating tab bar** — Pill-shaped bottom navigation with soft shadow and spring animations
- **Floating search** — Rounded search field with iOS-style typography
- **Large titles** — SF Pro-style type scale (Large Title, Title 1–3, Body, Callout, Subhead)
- **Rounded cards** — 16–24dp corner radii on manga covers, list items, and settings rows
- **Grouped settings** — iOS Settings-style section cards
- **Spring animations** — iOS spring interpolators for navigation, press feedback, and transitions
- **Minimal dividers** — Spacing-based hierarchy instead of heavy borders

## Architecture

All business logic, manga sources, extensions, database, downloader, reader, backup, tracking, and settings remain unchanged. Only UI resources and presentation-layer utilities were modified:

- `values/ios_*.xml` — Design tokens (colors, dimens, typography, styles)
- `drawable/bg_ios_*.xml` — iOS-style backgrounds and cards
- `core/ui/util/IosUiHelper.kt` — Spring animations and blur utilities
- Updated layouts for main screens, library, details, search, and settings

## Requirements

- Android 10+ (API 29+), same as upstream Kotatsu
- minSdk 23 maintained for compatibility

## Building

```bash
./gradlew assembleDebug
```

## License

Same as upstream Kotatsu — see [LICENSE](LICENSE).
