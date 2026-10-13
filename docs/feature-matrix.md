# Feature coverage matrix

This matrix compares the kinds of workflows visible in the supplied APKs with the current Super Me implementation. “Implemented” means a local equivalent exists in this source build; it does not mean behaviour, design or backend parity with the original app has been verified.

## TrackIt — study and time management

| Feature area | Super Me status | Notes / difference |
|---|---|---|
| Main dashboard and daily summaries | Implemented | New Super Me layout; not the same original artwork or pixel-perfect screen. |
| Projects and syllabus hierarchy | Implemented (basic) | Projects contain topics/tasks and completion status; arbitrary deep hierarchy and publisher-provided syllabi are not replicated. |
| Time tracking | Implemented | Manual time entry and session history; not a replacement for any original cloud syncing. |
| Pomodoro focus and break cycles | Implemented | 25/5, 50/10, custom lengths, short/long break transitions and local session records. Background reliability and device-restart behaviour still require device testing. |
| Spaced-repetition flashcards | Implemented (basic) | Local decks/questions, review ratings and increasing intervals; not guaranteed to match the source app's exact scheduling algorithm. |
| Notes | Implemented | Create/edit/delete/share notes locally. |
| Coding and exam templates | Partial | Several built-in starter roadmaps can be imported and edited. This is not an online or 1,000+ template catalogue or AI template generator. |
| Flip clock | Implemented (basic) | 12/24-hour formats and background themes. |
| Progress charts and completion calendar | Partial | Basic time totals and 28-day task-completion view; detailed original visualisations/streak algorithms may differ. |
| Home-screen widgets | Not implemented | The source APK contains widget receiver classes; Super Me currently has in-app views only. |
| Source app account/cloud sync, remote content, premium features | Not replicated | Requires original backend access, APIs and entitlement system; local records here are separate. |

## Fasting Tracker — fasting, food and wellness

| Feature area | Super Me status | Notes / difference |
|---|---|---|
| Fasting plans and custom duration | Implemented (basic) | 12:12, 14:10, 16:8, 18:6, 20:4, 23:1 and custom fasting-hour targets. |
| Live timer, progress and history | Implemented | Local elapsed countdown and records when the user ends a fast. Needs device testing for restart/time changes. |
| Eating-window settings | Implemented (basic) | User-set local clock hours; this does not automatically enforce eating or fasting. |
| Local reminders | Implemented (basic) | Optional notifications for target finish and one planned eating-window time; not the original scheduling system. |
| Weight, body measurements and BMI estimate | Implemented (basic) | Weight/waist entry, height/goal settings and a rough BMI estimate; no medical assessment. |
| Hydration tracker | Implemented | Local drink log and adjustable daily target. |
| Food diary, calories and macronutrients | Implemented (manual) | Edit entries and see daily totals. Nutrition data accuracy depends on inputs. |
| Barcode scanning | Implemented (external database) | Looks up Open Food Facts when user scans; product availability and nutrition completeness vary. |
| Food-photo / AI food scanner | Partial only | A generic on-device image label can suggest a name. It does not reliably recognise portions or calculate calories/macros. |
| Starter food database | Partial | Small built-in list, not a comprehensive nutrition database. |
| Recipes and meal planning | Partial | Starter recipe cards and meal-planning prompts, not a full personalised plan or remote recipe library. |
| Tips/insights | Partial | Original educational cards; no claim of matching the source's medical or personalised guidance. |
| Challenges, achievements and history | Implemented (basic) | Local self-managed challenges and milestones based on locally logged actions. |
| Weight/water widgets, AI coaching, account sync, paid plans, server recommendations | Not replicated | Requires native widgets and/or the source publisher's account services, models, APIs and entitlement system. |

## Combined-app look and architecture

Super Me has its own dark dashboard, bottom navigation and separate Study, Fast, Health and More sections. The feature groups are available inside one Android package; the original APKs are not embedded, and their separate screens are not launched from a shell.

A literal “same” result would require the original source code, design assets, endpoint contracts, backend services and licenses—or substantial independent implementation and device testing to reach visual/behaviour parity. The supplied compiled APKs alone are not enough to guarantee an exact clone. This repository aims to build an independent combined app with similar user workflows while being explicit about missing or approximate features.
