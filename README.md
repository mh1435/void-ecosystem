# Void Ecosystem

A suite of independent Android apps built to replace the stock OEM apps
entirely — each pillar ships as its **own separately installable APK**
with its own package name, so it updates, uninstalls, and runs
independently of the rest. `void-app` is the dashboard/launcher: a home
screen that shows every other app as a tile and launches it. Every app
shares one design system and one signing identity, so the suite still
*feels* like one cohesive OS even though nothing shares a process.

No music player here — [Void Music](#) already covers that pillar as its
own app.

## Module map

```
void-ecosystem/
├── app/                          # void-app — the dashboard/launcher, applicationId com.voidecosystem.app
├── core/
│   ├── designsystem/               # VoidTheme, colors, type scale, PillarCard, shared launcher icon
│   ├── model/                      # Pure-Kotlin cross-module models (EcosystemModule, Pillar)
│   ├── common/                     # VoidResult and other framework-free utilities
│   └── ui/                         # Shared composables built on top of designsystem (EmptyState, ...)
├── feature/
│   ├── dashboard/                   # Supplies :app's home-screen grid UI (not its own APK)
│   ├── theming/  terminal/  sysmonitor/             # System & Developer Tools — each its own APK
│   ├── omniassistant/  automation/  routines/        # AI & Automation — each its own APK
│   ├── gallery/                                      # Media & Entertainment — its own APK
│   ├── focushub/  todo/  journal/  finance/  pantry/ # Productivity & Life Tracking — each its own APK
│   ├── calculator/  notes/  calendar/  filemanager/  # Core OS Utilities — each its own APK
│   └── dialer/  browser/                             # Communication & Connectivity — each its own APK
├── gradle/libs.versions.toml    # Single version catalog — every module reads from here
└── keystore/README.md           # How to generate + wire up your release keystore
```

**Every `feature/<name>` module except `dashboard` is a `com.android.application`
module**, not a library — it builds to its own APK with applicationId
`com.voidecosystem.<name>` (e.g. `com.voidecosystem.calculator`), its own
launcher icon and label, and its own `MainActivity`. `feature/dashboard`
is the one exception: it's a library that supplies `void-app`'s
home-screen composable, since the dashboard itself isn't a separate
installable thing — it *is* `void-app`.

`:app` depends on nothing but `core:*` and `feature:dashboard` — it has no
idea any other pillar app exists at compile time. Tapping a dashboard
tile hands the tapped app's applicationId to `MainActivity`, which either
launches it via `PackageManager.getLaunchIntentForPackage()` (if
installed) or opens this repo's GitHub Releases page (if not). See
`app/src/main/kotlin/com/voidecosystem/app/MainActivity.kt`.

Which apps have real functionality vs. a placeholder screen right now:

- **Real, working apps:** Calculator, System Monitor, Browser (no
  persistence needed), and To-Do, Notes, Journal, Finance Tracker,
  Calendar, Pantry & Flavor Tracker (each backed by its own local Room
  database — data survives restarts).
- **Placeholder apps** (scaffolded, functionality not yet built — each
  needs something only you can provide first): Omni-Assistant (needs your
  LLM API keys), Theming Engine (needs Shizuku pairing), Terminal Sandbox
  (needs choosing a Python-on-Android runtime), Accessibility Automation
  and Dialer (need dangerous permissions granted deliberately, not
  silently), Routine Scheduler (needs DND policy access), Smart Gallery,
  Focus Hub, File Manager.

## Why apps demanded a manual uninstall before updating

Two independent causes, both fixed centrally in the root `build.gradle.kts`
and applied identically by every app module:

1. **Signature mismatch.** Android refuses to install an APK over an
   existing one unless both are signed with the *same* certificate. This
   happens the moment you (a) let Android Studio's ephemeral debug
   keystore sign one build and a different keystore sign another, or (b)
   let CI generate a fresh keystore on every run instead of reusing one.
   The fix: one persistent keystore, generated once (`keystore/README.md`),
   read locally from a gitignored `keystore.properties`, and reconstructed
   in CI from a base64 GitHub Secret — computed once at the root and read
   by every app module's `signingConfigs`, so every APK any of them ever
   produces, from any machine, carries the same signature. Debug builds
   also get `applicationIdSuffix = ".debug"`, so a debug build and a
   release build of the same app are literally different apps that never
   collide on-device in the first place.
2. **Non-increasing versionCode.** Even with identical signatures, Android
   rejects a new APK if its `versionCode` isn't strictly greater than the
   installed one. The root `build.gradle.kts` derives `versionCode` from
   `git rev-list --count HEAD` — the total commit count — once, and every
   app module shares that same value, so it only ever goes up,
   automatically, across the whole ecosystem.

With both fixed, installing a new signed release APK over an old one — for
any app in the suite — is a true in-place update, no uninstall required.

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

./gradlew assembleRelease
# Builds every app at once. APKs land under each module's own
# build/outputs/apk/release/, e.g.:
#   app/build/outputs/apk/release/app-release.apk
#   feature/calculator/build/outputs/apk/release/calculator-release.apk

# Or build just one app:
./gradlew :feature:calculator:assembleRelease
```

## CI/CD

`.github/workflows/build-and-release.yml` runs `assembleRelease` with no
module prefix — which builds *every* application module in the project at
once — on every push to `main` and on any `v*.*.*` tag, collects every
resulting APK, and publishes them all as assets on one GitHub Release
(`void-app-release.apk`, `void-calculator-release.apk`, ...). It needs
four repository secrets — `KEYSTORE_BASE64`, `KEYSTORE_STORE_PASSWORD`,
`KEYSTORE_KEY_ALIAS`, `KEYSTORE_KEY_PASSWORD` — set up per
`keystore/README.md`. Tag a commit `v1.2.0` for a named release, or just
push to `main` for a rolling `build-<run number>` release.

## Tech stack

- Kotlin + Jetpack Compose, Material 3
- MVVM; each pillar is its own `com.android.application` module depending
  only on `core:*` — no app knows any other app exists except `:app`,
  which only knows how to launch them by package name
- Room for the apps with local persistence (To-Do, Notes, Journal,
  Finance Tracker, Calendar, Pantry & Flavor Tracker), via KSP
- Gradle version catalog (`gradle/libs.versions.toml`) — every module's
  dependency versions come from one place
- AGP 8.6.1 / Kotlin 2.0.21 / compileSdk 35 / minSdk 26
