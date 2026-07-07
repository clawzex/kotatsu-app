# Kotatsu — 19 Source Edition

Dedicated manga/manhwa reader fork containing **only** these sources:

| Source | URL | Parser |
|--------|-----|--------|
| MangaGeko | https://mgeko.cc | Custom |
| OmegaScans | https://omegascans.org | HeanCMS |
| Manhwa18.cc | https://manhwa18.cc | Madara |
| Manhwa18.net | https://manhwa18.net | Custom |
| Manhwa18.com | https://manhwa18.com | Custom |
| TooMics English | https://toomics.com/en | HotComics |
| ToonGod | https://toongod.org | Madara |
| Toonily | https://toonily.com | Madara |
| Toonily.Me | https://toonily.me | Madtheme |
| HotComics | https://hotcomics.me/en | HotComics |
| CoComic | https://cocomic.co | Madara |
| KissManga | https://kissmanga.in | Madara |
| LikeManga | https://likemanga.ink | LikeManga |
| KaliScan | https://kaliscan.io | Madtheme |
| ManhwaDen | https://manhwaden.com | Madara |
| MadaraDex | https://madaradex.org | Madara |
| RavenScans | https://ravenscans.org | MangaReader |
| MgRead | https://mgread.io | Custom (WP REST + HTML) |
| HeyToon | https://toonhey.com | Custom |

## Architecture

- **App**: `/home/codespace/kotatsu-ios` — Kotatsu Android app (View-based UI)
- **Parsers**: `/home/codespace/kotatsu-parsers` — trimmed fork (19 sources, ~24 parser files + 6 base frameworks)

The app uses Gradle composite build (`includeBuild('../kotatsu-parsers')`) instead of JitPack.

## Build

```bash
export JAVA_HOME=$SDKMAN_DIR/candidates/java/17.0.19-amzn
export ANDROID_HOME=/home/codespace/Android/Sdk

cd /home/codespace/kotatsu-parsers && ./gradlew publishToMavenLocal -x test
cd /home/codespace/kotatsu-ios && ./gradlew assembleDebug
```

Use **JDK 17** to run Gradle; parsers compile to **JVM 11** for app compatibility.

## Changes from upstream

- Removed **1,237** parser source implementations
- Removed **1,169** deep-link host entries from `AndroidManifest.xml`
- Added **MgRead** parser (WordPress REST API + init-manga HTML)
- Updated domains: `mgeko.cc`, `toonhey.com`, `ravenscans.org`, etc.
- Renamed display titles: KaliScan, TooMics English

## Pruning scripts

- `kotatsu-parsers/scripts/prune_sources.py` — remove unwanted parser sources
- `kotatsu-ios/scripts/prune_manifest_hosts.py` — clean deep-link hosts
