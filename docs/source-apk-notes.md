# Source APK inspection notes

These notes record the observed package metadata used to plan Super Me. The original APK files and proprietary code/assets are intentionally not committed here.

| Input file | Package | Version | Observed archive structure |
|---|---|---|---|
| TRACK IT.apk | app.vinztech.trackit | 1.5.6 (version code 156) | Three root DEX files, AndroidX/Compose-related libraries and widget receiver classes. The supplied archive contained no root lib/<abi>/*.so entries. |
| Fasting Tracker.apk | bodyfast.zero.fastingtracker.weightloss | 2.5.8 (version code 158) | Three root DEX files, Kotlin/Android-style activity/DAO/service classes, food and barcode modules, and an embedded audience-network DEX. No root lib/<abi>/*.so entries were found. |

Both archives passed Python's ZIP integrity check. Their package IDs, activities, permissions, dependencies and resources differ. Their ZIP contents cannot safely be concatenated into one APK: component names/resources would collide or refer to incompatible resources, and Android packaging/signing is still required.

## What the supplied code suggests

The TrackIt DEX/resource names include the main activity, time-spent, completion and calendar-widget receivers, plus components consistent with projects, syllabus progress, flashcards, notes, templates, Pomodoro and clock features.

The Fasting Tracker archive includes classes whose names indicate fasting plan/detail/custom-plan/history and processing features, weight/water widgets, food diary/nutrition, barcode scanning, photo-assisted food, recipes, meal-plan, challenges and achievements.

These names show that code/resources exist in the archive; they do not prove that every feature works offline or can be reproduced in this project. Obfuscated code, account services, server-issued configuration, subscriptions, remote content and third-party SDK behaviour cannot be faithfully reconstructed merely from class names.

## TrackIt package completeness

The supplied TrackIt APK had no native .so files, and its manifest declares split-related configuration. It may therefore be only one member of an installed APK set or rely on a distribution-specific packaging structure. Obtain all installed APK paths (for example using adb shell pm path app.vinztech.trackit) before relying on this file as a complete installable package. Its classes/resources do not establish that it is a Flutter app; the observed AndroidX/Compose-related entries instead suggest a native Android UI implementation.

## Super Me implementation boundary

Super Me independently implements similar workflows: study/project tracking, timer and time history, flashcards, notes, clock and starter templates; fasting schedules and history; body and water logging; food and macros; barcode-based product lookup; limited on-device image-label suggestions; recipes; meal planning; insights; challenges; achievements; local reminders and backup import/export.

This is a functional approximation, not a pixel-perfect or feature-for-feature reproduction. It does not include the original publishers' binaries, artwork, server credentials, account data, premium plans, subscription state or server-side personalised services. A generic image-label model does not infer reliable calories/macros. Barcode lookup works only if the scanned code exists in the external database and a network connection is available.

Use only software you are authorised to inspect or modify.
