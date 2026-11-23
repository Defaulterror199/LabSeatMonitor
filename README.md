# Lab Seat Monitor (Case Study 21 - Computer Laboratory Seat Monitor)
INTPROG Final Project - Student ID 24258139. Kotlin, Views + ViewBinding, Material 3.

## Current status: PHASE 1 - Analysis, Design & Local Prototype
Checkpoint 1: **BUILD -> RUN -> NAVIGATE -> DISPLAY LOCAL/MOCK DATA**

### Open & run (in the LAB)
1. Android Studio -> *Open* -> select the `LabSeatMonitor` folder, wait for Gradle sync.
2. If prompted, accept the suggested Gradle/AGP upgrade, or set *Gradle JDK = 17* (Settings > Build > Gradle).
3. Run on an emulator / device (minSdk 24).
4. Log in as **Student** or **Laboratory staff** (any name).

### Try this (demo path)
- Student: Labs -> Computer Laboratory 1 -> PC-01 (Available) -> *Reserve* -> *Check in* -> *Release*. Then open another Available station: only one allowed at a time while holding one.
- Open PC-02 (Occupied) / PC-07 (Unavailable): no assign action is offered (the key business rule).
- Staff: open PC-01 -> *Mark as unavailable* (reason required) -> *Restore*; FAB **Add station**; Edit / Delete.
- Student menu -> **My reservations**.

### Code map
| Path | What |
|---|---|
| `model/Models.kt` | data classes + enums (Station, Lab, Reservation, StationStatus, ...) |
| `data/LabRepository.kt` | in-memory mock data **and all business rules** (`check()`) |
| `ui/*Activity.kt` | 6 screens |
| `ui/Adapters.kt` | 3 RecyclerView adapters |
| `docs/PHASE1_PROPOSAL.md` | proposal content (roles, FRs, BRs, screens, data, Firebase tree) |

## Roadmap
- **Phase 2** Firebase: uncomment plugin/deps in `app/build.gradle.kts`, add `google-services.json` (follow instructor guidance, don't commit secrets), replace login with Firebase Auth, make `LabRepository` write/read `stations` + `labs` (Create + Read).
- **Phase 3** Full CRUD in Firebase, state changes through **transactions**, realtime listeners.
- **Phase 4** 10+ test cases, APK, documentation, defense.

## Suggested Git commits
`Initial project setup` -> `Add models and mock data` -> `Add lab/station lists` -> `Add station detail + state rules` -> **tag** `Checkpoint 1 - Local Prototype Verified`
