<div align="center">

# Kotatsu — 19 Source Edition

**A focused Kotatsu fork with an iOS-inspired interface and 19 curated manga/manhwa sources.**

![Android CI](https://github.com/Admin-Dev-git/kotatsu-ios/actions/workflows/android-build.yml/badge.svg)
![Android 6.0+](https://img.shields.io/badge/android-6.0%2B-brightgreen)
![Sources](https://img.shields.io/badge/sources-19-E9321C)
![License](https://img.shields.io/github/license/Admin-Dev-git/kotatsu-ios)

Fork of [Kotatsu](https://github.com/KotatsuApp/Kotatsu) · Parsers in [kotatsu-parsers](https://github.com/Admin-Dev-git/kotatsu-parsers)

</div>

---

## Overview

This repository ships a trimmed, maintainable build of Kotatsu:

- **19 online sources** — manhwa/webtoon-focused catalogues only (see [Sources](#sources))
- **iOS-inspired UI** — floating tab bar, grouped cards, spring animations, and an Apple-like type scale on top of the existing View/Material stack
- **Composite parser build** — no JitPack dependency; parsers are built from the sibling [`kotatsu-parsers`](https://github.com/Admin-Dev-git/kotatsu-parsers) repo
- **Full Kotatsu feature set** — library, offline downloads, reader, tracking, sync, and settings are unchanged at the architecture level

See [README_IOS.md](README_IOS.md) for UI design details and [README_SOURCES.md](README_SOURCES.md) for parser maintenance notes.

## Screenshots

<div align="center">
  <img src="./metadata/en-US/images/phoneScreenshots/1.png" alt="Home" width="220"/>
  <img src="./metadata/en-US/images/phoneScreenshots/2.png" alt="Explore" width="220"/>
  <img src="./metadata/en-US/images/phoneScreenshots/3.png" alt="Library" width="220"/>
  <img src="./metadata/en-US/images/phoneScreenshots/4.png" alt="Details" width="220"/>
  <img src="./metadata/en-US/images/phoneScreenshots/5.png" alt="Reader" width="220"/>
  <img src="./metadata/en-US/images/phoneScreenshots/6.png" alt="Settings" width="220"/>
</div>

> Screenshots are from upstream Kotatsu metadata; the fork applies the iOS visual layer on the same screens.

## Features

- Browse and search manga from 19 built-in sources
- Favorites, reading history, bookmarks, and incognito mode
- Offline downloads and CBZ archive support
- Standard and Webtoon reader modes with gestures
- MAL, AniList, Shikimori, and Kitsu tracking integration
- Backup, sync, and optional app lock
- Android 6.0+ (`minSdk 23`), targets API 36

## Sources

| Source | Site | Parser base |
|--------|------|-------------|
| MangaGeko | [mgeko.cc](https://mgeko.cc) | Custom |
| OmegaScans | [omegascans.org](https://omegascans.org) | HeanCMS |
| Manhwa18.cc | [manhwa18.cc](https://manhwa18.cc) | Madara |
| Manhwa18.net | [manhwa18.net](https://manhwa18.net) | Custom |
| Manhwa18.com | [manhwa18.com](https://manhwa18.com) | Custom |
| TooMics English | [toomics.com/en](https://toomics.com/en) | HotComics |
| ToonGod | [toongod.org](https://toongod.org) | Madara |
| Toonily | [toonily.com](https://toonily.com) | Madara |
| Toonily.Me | [toonily.me](https://toonily.me) | Madtheme |
| HotComics | [hotcomics.me/en](https://hotcomics.me/en) | HotComics |
| CoComic | [cocomic.co](https://cocomic.co) | Madara |
| KissManga | [kissmanga.in](https://kissmanga.in) | Madara |
| LikeManga | [likemanga.ink](https://likemanga.ink) | LikeManga |
| KaliScan | [kaliscan.io](https://kaliscan.io) | Madtheme |
| ManhwaDen | [manhwaden.com](https://manhwaden.com) | Madara |
| MadaraDex | [madaradex.org](https://madaradex.org) | Madara |
| RavenScans | [ravenscans.org](https://ravenscans.org) | MangaReader |
| MgRead | [mgread.io](https://mgread.io) | Custom |
| HeyToon | [toonhey.com](https://toonhey.com) | Custom |

## Getting started

### Requirements

| Tool | Version |
|------|---------|
| JDK (Gradle) | 17 |
| JDK (bytecode) | 11 (parsers) |
| Android SDK | API 36, Build-Tools 35.0.0 |
| Gradle | 9.0 (wrapper included) |

### Clone

Both repositories must sit as **sibling directories** — the app uses `includeBuild('../kotatsu-parsers')`.

```bash
git clone https://github.com/Admin-Dev-git/kotatsu-parsers.git
git clone https://github.com/Admin-Dev-git/kotatsu-ios.git
```

Expected layout:

```text
workspace/
├── kotatsu-parsers/
└── kotatsu-ios/
```

### Configure SDK

Create `kotatsu-ios/local.properties` (not committed):

```properties
sdk.dir=/path/to/Android/Sdk
```

### Build

```bash
# 1. Publish parsers to Maven Local
cd kotatsu-parsers
./gradlew publishToMavenLocal -x test

# 2. Build debug APK
cd ../kotatsu-ios
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Release builds:

```bash
./gradlew assembleRelease
```

## CI

Every push and pull request to `devel` runs [`.github/workflows/android-build.yml`](.github/workflows/android-build.yml):

1. Checks out `kotatsu-ios` and `kotatsu-parsers` as siblings
2. Installs JDK 17 and Android SDK 36
3. Runs `publishToMavenLocal` on parsers, then `assembleDebug` on the app
4. Uploads the debug APK as a workflow artifact (retained 14 days)

Download artifacts from the **Actions** tab on a green workflow run.

## Project layout

```text
kotatsu-ios/
├── app/                    # Android application module
├── scripts/                # Manifest pruning utilities
├── README_IOS.md           # UI redesign documentation
├── README_SOURCES.md       # Source list and parser notes
└── .github/workflows/      # Android CI
```

Key integration points:

- `settings.gradle` — composite build of `../kotatsu-parsers`
- `app/build.gradle` — `implementation('org.koitharu:kotatsu-parsers:1.0')`
- `app/src/main/res/values/ios_*.xml` — iOS design tokens

## Contributing

1. Fork both [kotatsu-ios](https://github.com/Admin-Dev-git/kotatsu-ios) and [kotatsu-parsers](https://github.com/Admin-Dev-git/kotatsu-parsers)
2. Keep the sibling directory layout locally
3. Open PRs against `devel` — CI must pass before merge

Parser-only changes belong in the parsers repo. UI changes stay in this repo.

## Upstream

Based on [KotatsuApp/Kotatsu](https://github.com/KotatsuApp/Kotatsu) and [KotatsuApp/kotatsu-parsers](https://github.com/KotatsuApp/kotatsu-parsers). This fork removes unused sources and applies a custom interface; it is not affiliated with the original Kotatsu team.

## License

Licensed under the [GNU General Public License v3.0](LICENSE). You may copy, modify, and distribute this software under the same license. Build and install instructions are provided above.

## Disclaimer

This app does not host manga content. It loads publicly available pages from third-party websites. Copyright claims should be directed to the respective site operators.
