# Release Notes - Overload Tracker v1.0.0 🚀

> **Train smarter. Progress forever.**  
> *Version 1.0.0 (Build 1) — Official Release*

---

## 🌟 Overview

We are thrilled to announce the launch of **Overload Tracker v1.0.0**! Designed for serious lifters, strength athletes, and fitness enthusiasts, Overload Tracker brings a privacy-first, offline-ready progressive overload logging experience with modern UI aesthetics, deep training analytics, and seamless AI integration.

---

## 🚀 What's New in v1.0.0

### 🎨 Liquid Glass & "Sunset Rose" Redesign
* **Sunset Rose Theme**: Experience a sleek visual overhaul featuring vibrant orange accents, warm dark modes, and refined typography.
* **Glassmorphic Components**: Beautiful frosted glass surface cards (`GlassCard`, `GlassSurface`), backdrop blur dynamics, and smooth micro-animations.
* **Refined Navigation**: Re-engineered bottom bar and screen transitions for fluid single-handed navigation.

### 📊 Granular Data Export & Native AI Sharing
* **Time-Filtered CSV Exports**: Filter and export workout logs by custom timeframes:
  * Last 7 Days
  * Last 30 Days
  * Last 90 Days
  * Year-to-Date (YTD)
  * All Time
* **Single-Session Export**: Export individual workout session details directly from the session review screen.
* **Native Share Sheet Integration**: Effortlessly send exported workout reports to external apps—including AI assistants (Claude, ChatGPT, Gemini), personal trainers, or messaging tools (WhatsApp, Drive, Email) for workout analysis and programming feedback.

### 🔥 GitHub-Style Workout Heatmap & Consistency Grid
* **Visual Activity Heatmap**: Track training consistency over time with a dynamic GitHub-style contribution grid.
* **Intensity Color Gradient**: Color intensity scales dynamically based on workout volume and set completion density.
* **Interactive Day Inspection**: Tap any square on the grid to inspect workouts completed on that date, view streak counts, and review volume breakdown.

### 🏋️ Workout Splits & Organization
* **Terminology Alignment**: Updated organization from "Groups" to "Splits" (e.g., Push / Pull / Legs, Upper / Lower, Full Body) for intuitive routine structuring.
* **Custom Split Management**: Easily reorder exercises within a split, adjust default target sets/reps, and add new movements seamlessly.

### 🔍 Rapid Exercise Library & Filtered Search
* **Real-Time Debounced Search**: Upgraded search pipeline to filter 100+ exercises smoothly without keyboard lag.
* **Multi-Attribute Filters**: Filter exercise library by target muscle group, equipment (Barbell, Dumbbell, Machine, Bodyweight, etc.), and exercise category.

### ⏱️ Live Session Logging & Rest Timer
* **Streamlined Set Entry**: Rapidly log Weight, Reps, and RPE (Rate of Perceived Exertion).
* **PR & Previous Best Tracking**: Instant visibility into past performance (`Prev: Weight × Reps`) and automatic PR badges upon hitting new records.
* **Interactive Rest Timer**: Built-in rest timer with quick controls (`+30s`, `Skip`, `Reset`) to keep workout pacing tight.

### ⚙️ Architecture & Multiplatform Readiness
* **KMP Architecture**: Modularized data and domain layers preparing the engine for upcoming iOS compatibility (Kotlin Multiplatform).
* **100% Offline & Private**: All data resides securely on device in Room SQLite database. No mandatory account signup required.

---

## 🛠️ Enhancements & Bug Fixes

* **Fix Search Bar**: Separated search query UI state from debounced database query pipeline to resolve freeze issues during rapid typing.
* **FileProvider Integration**: Configured secure Android `FileProvider` (`file_paths.xml`) for zero-friction file sharing.
* **Room Database Optimization**: Added robust fallback handling and indices for query performance on history logs.

---

## 📦 Distribution Artifacts

* **Release Package**: `app/release/app-release.apk`
* **Target SDK**: Android 15 (API level 35)
* **Min SDK**: Android 8.0 (API level 26)

---

*Thank you for training with Overload Tracker! If you have any feedback or feature requests, feel free to reach out.*
