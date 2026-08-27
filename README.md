# Progressive Overload Tracker

Offline Android workout logger built with Kotlin, Jetpack Compose, Room, and Hilt.

## Features

- 1,324 exercises with local thumbnails + GIFs
- Searchable / filterable exercise library
- Reusable workout groups (Push, Legs, etc.)
- Live set logging with weight, reps, RPE, rest timer
- History, PR indicators, progression chart
- CSV export to Downloads for LLM analysis
- Units (kg/lb), theme, and rest defaults in Settings

## Requirements

- Android Studio Ladybug+ / AGP 8.7
- JDK 17 or 21
- Android SDK 35
- Min SDK 26

## Build

```bash
./gradlew :app:assembleDebug
```

Install:

```bash
./gradlew :app:installDebug
```

## Assets

Exercise data and media live under `app/src/main/assets/`:

```
assets/
  data/exercises.json   # 1,324 exercises
  images/               # thumbnails
  videos/               # animation GIFs
```

Source dataset: [hasaneyldrm/exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset)

Media © Gym visual — see dataset NOTICE/LICENSE.

## Architecture

```
UI (Compose) → ViewModel → Repository → Room DAO → SQLite
```

100% local — no network permission.

## CSV schema

```
date,workout_name,muscle_groups,exercise_name,equipment,set_number,weight_kg,reps,rpe,total_volume,notes
```
# Overload
# Overload
# Overload
