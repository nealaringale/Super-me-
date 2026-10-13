# Source APK inspection notes

These notes record the metadata used to plan Super Me. The original APK files themselves and their proprietary code/assets are intentionally not committed here.

| Input file | Package | Version | Technical observation |
|---|---|---|---|
| TRACK IT.apk | app.vinztech.trackit | 1.5.6 (version code 156) | Flutter asset directory and three root DEX files found; the supplied file has no .so entries and manifest declares ABI/density split requirements. |
| Fasting Tracker.apk | bodyfast.zero.fastingtracker.weightloss | 2.5.8 (version code 158) | Native Android/Kotlin-style package with three root DEX files; no .so entries found in the supplied APK. |

Both archives passed Python's ZIP integrity check. Their manifests include different package identities, application components, permissions, and SDK requirements. Their package IDs and original app components cannot safely be concatenated into a single APK by simply combining ZIP files.

TrackIt's manifest requires ABI and density split types. Its missing native libraries may be distributed in companion split APKs. If the goal later changes to reproducing the original TrackIt interface or preserving every original feature, obtain the full installed APK split set first. Static inspection of the supplied files is not proof of the apps' complete runtime architecture.

Super Me implements an independently authored baseline: daily habits, a local activity timer, and a fasting countdown. It does not include either original APK, extracted proprietary assets, network credentials, SDK configuration, or original app source code.
