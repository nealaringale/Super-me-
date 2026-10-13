# Super Me — unified study, productivity, fasting and nutrition app

Super Me is an original Android project that brings useful workflows from a study/time-tracking app and a fasting/food diary app into one local-first experience.

**Important:** this is not a literal APK merger and it is not a pixel-perfect clone. The two source APKs have different package identities and compiled internals. Super Me independently implements similar user-facing workflows without copying their proprietary source, artwork, paid features, account systems, or private backend services.

## Modules in this version

### Study and productivity
- Home dashboard with daily summaries.
- Projects and syllabus roadmaps with custom topics/tasks, completion toggles and logged time.
- Built-in starter templates for Python, data structures and algorithms, C/C++, web development, full-stack, machine learning, JEE/CET, GATE, project launch, and general study planning.
- Pomodoro focus/break cycles (25/5, 50/10 and custom), timer persistence, completed rounds, manual time logging and recent session history.
- Flashcards with decks, recall rating, and local spaced-review intervals.
- Editable notes with share actions.
- Full-page clock with 12/24-hour formatting and several background themes.
- Study progress cards and a 28-day completion calendar.

### Fasting and wellbeing
- Selectable fasting targets (12, 14, 16, 18, 20 or 23 hours) and a custom 1–23-hour timer.
- Live fasting countdown, elapsed time, previous-session history and editable eating-window hours.
- Opt-in local notifications for the fasting target and the planned eating-window time.
- Weight and optional waist measurements, height and goal settings, and a rough BMI estimate.
- Water intake log and a configurable personal reminder target.
- Food diary with editable calories/protein/carbohydrates/fat values and daily totals.
- Barcode scanning with optional Open Food Facts lookup when the user scans a product.
- Photo-assisted food entry: an on-device image-label suggestion can prefill a name, but it does not estimate nutrition. Check calories and macros yourself.
- Starter food list, starter recipe cards, meal-planning prompts, educational insights, self-managed challenges and locally calculated milestones.
- JSON export/import for supported local records.

## Build a debug APK

Requirements:
- Android Studio / Android SDK Platform 35
- JDK 17
- Gradle 8.9 (the GitHub Actions workflow installs it)

The main activity source is stored as ordered text files under app/src/main/java-parts/ to keep individual GitHub commits manageable. The Gradle generateMainActivity task joins those parts into a Java source file before compilation.

Run gradle assembleDebug from a checkout.

Output: app/build/outputs/apk/debug/app-debug.apk

A GitHub Actions workflow also builds and uploads the debug APK artifact for each push to main. A successful compile does not replace installing and testing on a real device. Barcode scanning, photo selection, permission prompts, notification delivery, timer persistence and backup import/export should all be tested on a supported device.

## Data, privacy and limits

- Most records are stored locally in Android SharedPreferences. Cloud sync, account login and remote AI coaching are not implemented.
- Android app backup is disabled in this build.
- Internet permission is used only for a user-initiated barcode lookup to Open Food Facts.
- The app requests camera access at the platform level for camera integration, but image capture uses the external camera app; no account, location, advertising, or analytics SDK has been added.
- The photo-label feature uses a generic ML Kit image-labeling model, not a dedicated nutrition model. It must not be treated as an AI calorie scanner.
- Calorie values in starter recipes and foods are estimates. They vary by brands, portions and preparation.
- Fasting and body-tracking features are informational only, not medical advice.
- TrackIt may require companion split APKs for all of its original runtime libraries. Premium/online catalogues, remote templates, account-synced records, native widgets and server-side personalised features from the source apps are not automatically reproduced here.

## Feature coverage and source notes

See docs/feature-matrix.md for a source-to-Super-Me feature mapping and what is not yet equivalent. See docs/source-apk-notes.md for observed package metadata and architecture notes.

## License and attribution

Super Me is an independent reimplementation. The input APKs are not redistributed by this repository, and no claim of affiliation with the original publishers is made. Respect each original app's terms and only analyse software you are authorised to inspect.
