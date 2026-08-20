# Void Ecosystem

A modular, multi-app Android suite built to replace the stock OEM apps
entirely — one shared design system, one navigation surface, one signing
identity, across every pillar: system tools, AI, media, productivity,
utilities, and communication.

## Module map

```
void-ecosystem/
├── app/                        # Thin composition root: Application, MainActivity, NavHost.
│                                #   The ONLY module that depends on every other module.
├── core/
│   ├── designsystem/            # VoidTheme, colors, type scale, PillarCard and shared components
│   ├── model/                   # Pure-Kotlin cross-module models (EcosystemModule, Pillar)
│   ├── common/                  # VoidResult and other framework-free utilities
│   └── ui/                      # Shared composables built on top of designsystem (EmptyState, ...)
├── feature/
│   ├── dashboard/                # The gateway/home screen — deliverable #4, see below
│   ├── theming/  terminal/  sysmonitor/            # System & Developer Tools
│   ├── omniassistant/  automation/  routines/       # AI & Automation
│   ├── musicplayer/  gallery/                       # Media & Entertainment
│   ├── focushub/  todo/  journal/  finance/ pantry/ # Productivity & Life Tracking
│   ├── calculator/  notes/  calendar/ filemanager/  # Core OS Utilities
│   └── dialer/  browser/                            # Communication & Connectivity
├── gradle/libs.versions.toml    # Single version catalog — every module reads from here
└── keystore/README.md           # How to generate + wire up your release keystore
```

**Dependency direction is one-way and enforced by convention:** `feature/*`
modules depend on `core/*` only — never on each other. `app` is the single
module allowed to depend on every `feature/*` module, because it's the only
place that needs to know all of them exist (see
`app/src/main/kotlin/com/voidecosystem/app/navigation/VoidNavHost.kt`).
This is what keeps 20+ apps from turning into a dependency tangle: add a
new pillar app by adding one `feature/<name>` module and one `composable()`
line in `VoidNavHost`, nothing else changes.

Every feature module currently ships a placeholder `<Name>Screen.kt` — a
real `Scaffold` wired into shared theming, with a route object other
modules navigate to by name. That's intentionally where you start building
each pillar's actual functionality; the scaffolding (Gradle module, theme,
nav route) is already wired end to end.

## Why apps demanded a manual uninstall before updating

Two independent causes, both fixed in `app/build.gradle.kts`:

1. **Signature mismatch.** Android refuses to install an APK over an
   existing one unless both are signed with the *same* certificate. This
   happens the moment you (a) let Android Studio's ephemeral debug
   keystore sign one build and a different keystore sign another, or (b)
   let CI generate a fresh keystore on every run instead of reusing one.
   The fix: one persistent keystore, generated once (`keystore/README.md`),
   read locally from a gitignored `keystore.properties`, and reconstructed
   in CI from a base64 GitHub Secret — so every APK you ever produce, from
   any machine, carries the same signature. Debug builds also get
   `applicationIdSuffix = ".debug"`, so a debug build and a release build
   are literally different apps that never collide on-device in the first
   place.
2. **Non-increasing versionCode.** Even with identical signatures, Android
   rejects a new APK if its `versionCode` isn't strictly greater than the
   installed one. `app/build.gradle.kts` derives `versionCode` from
   `git rev-list --count HEAD` — the total commit count — so it only ever
   goes up, automatically, without you tracking a number by hand.
   `versionName` (`1.0.<versionCode>`) is separate and purely cosmetic.

With both fixed, installing a new signed release APK over an old one is a
true in-place update — no uninstall required.

## Building a signed release locally

```bash
# One-time setup — see keystore/README.md for full detail.
keytool -genkeypair -v -keystore keystore/void-ecosystem-release.jks \
  -alias void-ecosystem -keyalg RSA -keysize 4096 -validity 10000
cat > keystore.properties <<EOF
storeFile=keystore/void-ecosystem-release.jks
storePassword=...
keyAlias=void-ecosystem
keyPassword=...
EOF

./gradlew :app:assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

## CI/CD

`.github/workflows/build-and-release.yml` builds a signed release APK on
every push to `main` and on any `v*.*.*` tag, then publishes it as a
GitHub Release with the APK attached. It needs four repository secrets —
`KEYSTORE_BASE64`, `KEYSTORE_STORE_PASSWORD`, `KEYSTORE_KEY_ALIAS`,
`KEYSTORE_KEY_PASSWORD` — set up per `keystore/README.md`. Tag a commit
`v1.2.0` for a named release, or just push to `main` for a rolling
`build-<run number>` release.

## Tech stack

- Kotlin + Jetpack Compose, Material 3
- MVVM, strict multi-module architecture (`core:*` shared, `feature:*`
  isolated, `app` as composition root)
- Gradle version catalog (`gradle/libs.versions.toml`) — every module's
  dependency versions come from one place
- AGP 8.6.1 / Kotlin 2.0.21 / compileSdk 35 / minSdk 26
