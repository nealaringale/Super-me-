# Super Me — personal health & habit dashboard

Super Me is an original Android app project that brings together the ideas of habit/time tracking from TrackIt and a fasting timer from Fasting Tracker in one offline-first dashboard.

> This is a clean reimplementation, not a binary merge and not a copy of either third-party app's code, branding, paid features, or assets. The two supplied APKs use different packages/frameworks, and TrackIt appears to be missing the native Flutter ABI libraries from the uploaded APK set. Combining their compiled APKs directly would not produce a reliable installable app.

## Current features

- Daily habit checklist with add/remove and per-day completion reset.
- Activity timer with a custom session label and a daily tracked-time total.
- Fasting timer with 12, 14, 16, 18, or 20-hour target selection.
- Local persistence using Android SharedPreferences; no account or backend required.
- Single dashboard, single app ID (`com.nealaringale.superme`), and custom dark visual style.

## Build

Requirements:

- Android Studio with Android SDK Platform 35
- JDK 17
- Android Gradle Plugin 8.7.3 (Gradle 8.9 or compatible wrapper)

Open this repository as a project in Android Studio and let Gradle sync. Then select **Run** or run:

```bash
gradle assembleDebug
```

The debug APK should be produced at `app/build/outputs/apk/debug/app-debug.apk`.

This environment did not have an Android SDK or Gradle installed, so an APK build/install test has not been claimed. Build and test on an emulator or spare device before relying on it.

## Data and privacy

The current version stores habits, activity timing, and the active fasting timer locally on the device. It does not declare internet, advertising, analytics, location, or health sensor permissions. Uninstalling the app or clearing its app data removes the local data.

## Next steps

- Add automated UI/unit tests and a proper Gradle wrapper.
- Add export/import for local progress.
- Add optional reminders with explicit permission and user-controlled settings.
- Validate timer behaviour over device restarts and time-zone/day changes.

## Source APK notes

The source uploads were inspected as ZIP/APK containers. See [`docs/source-apk-notes.md`](docs/source-apk-notes.md) for identifying metadata and why they have not been bundled into this public repository.
