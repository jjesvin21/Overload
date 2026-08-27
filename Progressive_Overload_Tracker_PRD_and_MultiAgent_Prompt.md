# 📋 PRODUCT REQUIREMENTS DOCUMENT (PRD)
## Progressive Overload Tracker — Android Application

**Product Owner:** You  
**Tech Stack:** Kotlin, Jetpack Compose, Room (SQLite), Material Design 3  
**Data Source:** [hasaneyldrm/exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset) (1,324 exercises with GIFs, thumbnails, muscle-group metadata)  
**Architecture:** MVVM + Clean Architecture + Repository Pattern  
**Storage:** 100% Local (No server, no cloud)

---

## 1. Executive Summary

A single-user Android application for tracking progressive overload in weight training. Users create reusable **Workout Groups** (e.g., "Chest Day", "Leg Day"), populate them with exercises from a built-in database of 1,324 exercises, and log sets with weight and rep counts during live workouts. The app visualizes workout history, tracks progression over time, and exports data as CSV for external LLM analysis.

---

## 2. User Persona

**"The Dedicated Lifter"** — Someone who follows a structured split (PPL, Upper/Lower, Bro-Split) and wants to see their bench/squat/deadlift numbers trend upward over weeks. They want zero friction during the workout, rich visual references for form, and the ability to feed their training history into Claude/Kimi for program optimization.

---

## 3. Feature Requirements

### 3.1 Onboarding & Data Seeding
- **FR-1.1:** On first launch, the app must parse `exercises.json` from the assets folder and seed a local Room database.
- **FR-1.2:** Exercise media (GIFs + thumbnails) must be bundled in `assets/` and referenced locally. No network calls for media.
- **FR-1.3:** The seeded database must contain: `id`, `name`, `category` (body part), `equipment`, `target`, `muscle_group`, `secondary_muscles`, `instructions_en`, `image_path`, `gif_path`.

### 3.2 Exercise Library (Browse & Discovery)
- **FR-2.1:** A dedicated "Exercise Library" screen showing all 1,324 exercises in a searchable, filterable grid.
- **FR-2.2:** **Search:** Real-time text search by exercise name (case-insensitive substring match).
- **FR-2.3:** **Filter Chips:** Horizontal scrollable chips for body parts: `Chest`, `Back`, `Upper Arms`, `Shoulders`, `Upper Legs`, `Lower Legs`, `Waist`, `Lower Arms`, `Cardio`, `Neck`. Multi-select OR single-select.
- **FR-2.4:** **Filter by Equipment:** Optional dropdown for `Barbell`, `Dumbbell`, `Cable`, `Body Weight`, etc.
- **FR-2.5:** Each exercise card shows: thumbnail image, name, target muscle, equipment icon.
- **FR-2.6:** Tapping a card opens an **Exercise Detail Bottom Sheet** showing:
  - Large animated GIF (auto-play, loop)
  - Full name, category, target, secondary muscles, equipment
  - Step-by-step instructions (English)
  - "Add to Group" button

### 3.3 Workout Group Management
- **FR-3.1:** "My Groups" screen listing all user-created workout groups (e.g., "Push A", "Leg Day", "Upper Power").
- **FR-3.2:** **Create Group:** FAB opens a dialog to enter group name + optional notes.
- **FR-3.3:** **Edit Group:** Rename, reorder exercises within group, delete exercises from group, delete entire group.
- **FR-3.4:** **Add Exercises to Group:** From Exercise Library or from a dedicated "Add Exercise" flow within group editing. User can multi-select exercises and bulk-add.
- **FR-3.5:** Groups persist in local database with a `group_exercises` junction table.

### 3.4 Live Workout Session (The Core Flow)
- **FR-4.1:** From "My Groups", tapping a group starts a **Workout Session**.
- **FR-4.2:** Session screen is a vertical list of exercises in the group order.
- **FR-4.3:** For each exercise, user can add multiple **Sets** dynamically:
  - Each set row has: `Set #`, `Weight (kg/lb)`, `Reps`, `RPE (optional 1-10)`, `Done checkbox`
  - **Previous Best Indicator:** If the user has done this exercise before, show a small chip: "Prev: 80kg × 8"
- **FR-4.4:** **Rest Timer:** After marking a set as "Done", a rest timer auto-starts (default 90s, configurable per-exercise 0s-300s). Shows a countdown notification + in-app circular timer. User can skip, add 30s, or reset.
- **FR-4.5:** **Exercise Detail During Workout:** Long-press or info icon on any exercise opens the detail bottom sheet with GIF and instructions (same as FR-2.6).
- **FR-4.6:** **Session Actions:**
  - "Finish Workout" — saves session with timestamp, all sets, total volume per exercise.
  - "Discard Workout" — confirmation dialog, deletes in-progress session.
  - "Add Exercise on the Fly" — search library and append to current session without saving to the group template.
- **FR-4.7:** **Volume Calculation:** Auto-calculate total volume (weight × reps) per exercise and per session.

### 3.5 Workout History & Analytics
- **FR-5.1:** "History" tab showing a chronological list of all completed sessions.
- **FR-5.2:** Each history card shows: Date, Group Name, Total Volume, Duration, Muscle Groups trained (chips).
- **FR-5.3:** Tapping a history card opens **Session Detail Screen** showing:
  - Full date and time
  - Every exercise with all sets (weight × reps)
  - Rest times (if logged)
  - Total volume per exercise and session total
  - Personal Record indicators (if any set was an all-time best for that exercise)
- **FR-5.4:** **Exercise-Specific History:** From Exercise Detail, view a graph/chart of weight progression over time for that specific exercise.

### 3.6 CSV Export for LLM Analysis
- **FR-6.1:** From History screen, an "Export All" button generates a CSV file saved to `Downloads/WorkoutHistory_YYYY-MM-DD.csv`.
- **FR-6.2:** CSV Schema:
  ```csv
  date,workout_name,muscle_groups,exercise_name,equipment,set_number,weight_kg,reps,rpe,total_volume,notes
  2026-08-26,Push A,"Chest,Shoulders,Triceps",Barbell Bench Press,Barbell,1,80,8,8,640,
  2026-08-26,Push A,"Chest,Shoulders,Triceps",Barbell Bench Press,Barbell,2,80,7,9,560,
  2026-08-26,Push A,"Chest,Shoulders,Triceps",Incline Dumbbell Press,Dumbbell,1,30,10,7,300,
  ```
- **FR-6.3:** Each row represents one set. Muscle groups are comma-separated. Date in ISO-8601.
- **FR-6.4:** Also support "Export Single Session" from the Session Detail screen.
- **FR-6.5:** Use Android Storage Access Framework (SAF) to save to Downloads without extra permissions.

### 3.7 Settings
- **FR-7.1:** Unit toggle: Kilograms / Pounds (affects input and CSV export).
- **FR-7.2:** Default rest timer duration (30s - 300s).
- **FR-7.3:** Theme: Light / Dark / System Default.
- **FR-7.4:** Clear all history (with confirmation).
- **FR-7.5:** Reset exercise database (re-seed from assets).

---

## 4. Data Model (Room Entities)

```kotlin
// Exercise (seeded from JSON, read-only for user)
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,        // body part: chest, back, etc.
    val equipment: String,
    val target: String,
    val muscleGroup: String,
    val secondaryMuscles: String, // JSON array stored as string
    val instructions: String,      // English instructions
    val imagePath: String,         // assets/images/0001-xxx.jpg
    val gifPath: String            // assets/videos/0001-xxx.gif
)

// Workout Group (user-created templates)
@Entity(tableName = "workout_groups")
data class WorkoutGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// Junction: Group -> Exercises (ordered)
@Entity(
    tableName = "group_exercises",
    primaryKeys = ["groupId", "exerciseId"],
    foreignKeys = [
        ForeignKey(entity = WorkoutGroup::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = CASCADE)
    ]
)
data class GroupExercise(
    val groupId: Long,
    val exerciseId: String,
    val sortOrder: Int // for reordering within group
)

// Workout Session (one per completed workout)
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long?, // nullable if ad-hoc workout
    val groupName: String, // snapshot name at time of workout
    val startTime: Long,
    val endTime: Long,
    val totalVolume: Double,
    val notes: String? = null
)

// Individual Set logged during a session
@Entity(
    tableName = "session_sets",
    foreignKeys = [
        ForeignKey(entity = WorkoutSession::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = CASCADE)
    ]
)
data class SessionSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String, // snapshot
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Int? = null,
    val isCompleted: Boolean = true,
    val restSeconds: Int? = null
)
```

---

## 5. UI/UX Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   ONBOARDING    │────▶│  EXERCISE LIB   │◄────│  EXERCISE DETAIL│
│  (Seed DB)      │     │  (Search/Filter)│     │  (GIF + Add)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   MY GROUPS     │────▶│  GROUP EDITOR   │────▶│  ADD EXERCISES  │
│ (Create/Select) │     │ (Reorder/Delete)│     │ (Search/Filter) │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ LIVE WORKOUT    │────▶│  SET LOGGING    │────▶│  REST TIMER     │
│ (Session Start) │     │ (Weight/Reps)   │     │ (Countdown)     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  HISTORY LIST   │────▶│ SESSION DETAIL  │────▶│  CSV EXPORT     │
│ (All Workouts)  │     │ (Sets/Volume/PR)│     │ (Downloads)     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## 6. Non-Functional Requirements
- **NFR-1:** App must work entirely offline. Zero network permissions.
- **NFR-2:** Cold start to "My Groups" screen in < 2 seconds on mid-range device.
- **NFR-3:** Room database queries must be asynchronous (Flow + Coroutines). No main-thread DB access.
- **NFR-4:** All Compose screens must handle empty states, loading states, and errors gracefully.
- **NFR-5:** Code must be extensively commented for readability by multi-agent systems and future developers.
- **NFR-6:** Follow SOLID principles. Separate concerns: UI layer (Compose) → ViewModel → Repository → DAO → Database.
- **NFR-7:** Use Hilt for dependency injection.
- **NFR-8:** Use Navigation Component with type-safe navigation (Kotlin Serialization).

---

## 7. File Structure

```
com.overloadtracker/
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── ExerciseDao.kt
│   │   │   ├── WorkoutGroupDao.kt
│   │   │   ├── WorkoutSessionDao.kt
│   │   │   └── Converters.kt
│   │   └── entity/
│   │       ├── Exercise.kt
│   │       ├── WorkoutGroup.kt
│   │       ├── GroupExercise.kt
│   │       ├── WorkoutSession.kt
│   │       └── SessionSet.kt
│   ├── repository/
│   │   ├── ExerciseRepository.kt
│   │   ├── WorkoutGroupRepository.kt
│   │   └── WorkoutSessionRepository.kt
│   └── model/
│       └── WorkoutSummary.kt
├── di/
│   └── AppModule.kt
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── theme/
│   │   └── Theme.kt
│   ├── components/
│   │   ├── ExerciseCard.kt
│   │   ├── SetRow.kt
│   │   ├── RestTimer.kt
│   │   └── FilterChips.kt
│   ├── screens/
│   │   ├── library/
│   │   │   ├── ExerciseLibraryScreen.kt
│   │   │   └── ExerciseLibraryViewModel.kt
│   │   ├── groups/
│   │   │   ├── MyGroupsScreen.kt
│   │   │   ├── GroupEditorScreen.kt
│   │   │   └── GroupsViewModel.kt
│   │   ├── workout/
│   │   │   ├── LiveWorkoutScreen.kt
│   │   │   └── LiveWorkoutViewModel.kt
│   │   ├── history/
│   │   │   ├── HistoryScreen.kt
│   │   │   ├── SessionDetailScreen.kt
│   │   │   └── HistoryViewModel.kt
│   │   └── settings/
│   │       └── SettingsScreen.kt
│   └── viewmodel/
│       └── SharedExerciseViewModel.kt
├── util/
│   ├── CsvExporter.kt
│   ├── JsonSeeder.kt
│   └── Constants.kt
└── MainActivity.kt
```

---

## 8. Asset Integration Strategy

Since the GitHub dataset contains 1,324 images + 1,324 GIFs (~200MB+), bundling all in APK is impractical. Recommended approach for the multi-agent build:

**Option A (Recommended):** Include a curated subset (~200 most common exercises) in `assets/`. Provide an in-app "Download Full Database" option that fetches from a CDN (violates offline-only constraint).

**Option B (Preferred for true offline):** The user must manually place the `data/`, `images/`, and `videos/` folders into `app/src/main/assets/` before building. The seeder reads from there. The prompt below assumes this.

**Option C (Hybrid):** Bundle only thumbnails in APK. GIFs are loaded on-demand... but this requires internet.

> **Recommendation:** Start with Option B. The prompt instructs the agent to build the seeder assuming assets exist, and documents where the user should place them.

---

# 🤖 MULTI-AGENT SYSTEM PROMPT
## Progressive Overload Tracker — Full Build Instructions

**Context:** You are a team of specialized agents building a Kotlin Android app from scratch. The Product Owner has provided the PRD above. You must implement every feature with production-quality code, extensive comments, and zero external network dependencies.

**Global Constraints:**
- Language: Kotlin
- UI: Jetpack Compose + Material Design 3
- Architecture: MVVM + Repository + Room
- DI: Hilt
- Navigation: Jetpack Navigation with type-safe routes
- Database: Room (SQLite) only, no server, no Firebase, no Retrofit
- All database operations via Coroutines + Flow
- Every class, function, and complex algorithm must have KDoc/JavaDoc comments
- Min SDK: 26, Target SDK: 35

---

## AGENT 1: Foundation & Data Layer
**Task:** Create the entire project scaffold, Gradle setup, and database layer.

**Instructions:**
1. Initialize a new Android project with Empty Compose Activity template.
2. Add dependencies: Room, Hilt, Navigation Compose, Kotlin Serialization, DataStore (for settings), Coil (for local asset image loading).
3. Create all Room entities exactly as defined in the PRD Section 4.
4. Create TypeConverters for `List<String>` (secondary muscles) and any complex types.
5. Create DAOs with full CRUD + Flow queries:
   - `ExerciseDao`: getAll, getById, searchByName, filterByCategory, getByEquipment
   - `WorkoutGroupDao`: getAllGroups, getGroupWithExercises, insertGroup, deleteGroup, updateGroupOrder
   - `WorkoutSessionDao`: getAllSessions, getSessionWithSets, getSetsForExercise, getPRForExercise
6. Create `AppDatabase` with all entities and a `Callback` for pre-population.
7. Create `JsonSeeder` utility that reads `assets/data/exercises.json` on first launch and inserts into `exercises` table. Handle JSON parsing with Gson/Kotlinx Serialization. Map `image` and `gif_url` to local asset paths.
8. Set up Hilt modules for Database and DAO injection.
9. Create Repository interfaces + implementations for all three domains (Exercise, Group, Session).

**Deliverable:** A compilable project with data layer fully functional. Include a test in `JsonSeederTest` to verify parsing.

---

## AGENT 2: Exercise Library & Discovery
**Task:** Build the Exercise Library screen, filtering, search, and detail bottom sheet.

**Instructions:**
1. Create `ExerciseLibraryScreen` with:
   - Top search bar (debounced 300ms)
   - Horizontal scrollable filter chips for all body parts from the dataset
   - Optional equipment filter dropdown
   - LazyVerticalGrid of `ExerciseCard` components
2. `ExerciseCard` must display:
   - Thumbnail loaded via Coil from `assets/` (use `rememberAsyncImagePainter` with `AssetUri`)
   - Exercise name, target muscle, equipment icon
3. Create `ExerciseDetailBottomSheet` triggered on card tap:
   - Full GIF display (looping, auto-play)
   - Name, category, target, secondary muscles as chips
   - Step-by-step instructions (scrollable)
   - "Add to Group" button that opens a dialog listing all user groups
4. ViewModel must hold `SearchQuery`, `SelectedCategories`, and `SelectedEquipment` as StateFlow.
5. Implement `getFilteredExercises()` that combines search + filters reactively via Flow combine.
6. Handle empty states ("No exercises match your filters") and loading states.

**Deliverable:** Fully navigable Exercise Library with working search, filters, and detail view.

---

## AGENT 3: Workout Group Management
**Task:** Build group creation, editing, and exercise assignment.

**Instructions:**
1. Create `MyGroupsScreen`:
   - List of all groups with card UI showing name, exercise count, last performed date
   - FAB to create new group (dialog with name + notes)
   - Swipe-to-delete with undo Snackbar
   - Tap to start workout (navigates to Live Workout)
   - Long-press or edit icon to enter Group Editor
2. Create `GroupEditorScreen`:
   - Reorderable list of exercises (use `androidx.compose.foundation.lazy.dragAndDrop` or a stable reorderable library if available, otherwise manual up/down arrows)
   - Delete exercise from group (swipe or trailing icon)
   - "Add Exercises" FAB navigates to a multi-select version of Exercise Library
   - Save button persists order to `group_exercises` junction table
3. Create `AddExercisesToGroupScreen`:
   - Same search/filter as Exercise Library
   - Checkbox selection mode
   - "Add Selected (N)" bottom bar
4. ViewModel must manage group CRUD and maintain sort order.

**Deliverable:** Users can create groups, add/remove/reorder exercises, and delete groups.

---

## AGENT 4: Live Workout Session (Core Feature)
**Task:** Build the most critical screen — the active workout logger.

**Instructions:**
1. Create `LiveWorkoutScreen`:
   - Top bar: Group name, elapsed workout timer (chronometer), "Finish" and "Discard" buttons
   - Vertical list of exercises in group order
   - Each exercise section is an expandable card containing:
     - Exercise header: thumbnail, name, info icon (opens detail sheet), "Add Set" button
     - List of set rows
2. `SetRow` component:
   - Set number label
   - Weight input field (numeric, respects kg/lb setting)
   - Reps input field (numeric)
   - Optional RPE dropdown (1-10, "—" for none)
   - Checkbox to mark done
   - When checked: trigger rest timer, show "Prev Best" comparison if applicable
3. **Rest Timer:**
   - Circular countdown timer overlay or inline
   - Configurable per-exercise (default from settings)
   - Actions: Skip, +30s, Reset
   - Sound/vibration on completion (use `RingtoneManager`)
4. **Previous Best Logic:**
   - When user opens an exercise, query `WorkoutSessionDao.getPRForExercise(exerciseId)` to get max weight ever lifted.
   - Show inline: "PR: 100kg" or "Last: 80kg × 8"
5. **Add Exercise on the Fly:**
   - Floating "Add Exercise" button opens Exercise Library in single-select mode
   - Appends to current session but does NOT save to group template
6. **Session State Management:**
   - ViewModel holds `LiveWorkoutState` data class with all sets
   - Auto-save draft to DataStore every 30 seconds (recovery if app killed)
   - On "Finish": calculate total volume, save to `workout_sessions` + `session_sets`, clear draft
   - On "Discard": show confirmation dialog, delete draft

**Deliverable:** A fully functional workout logger that feels responsive during a real gym session.

---

## AGENT 5: History, Analytics & CSV Export
**Task:** Build the history viewer, session detail, and CSV export.

**Instructions:**
1. Create `HistoryScreen`:
   - Reverse chronological list of completed sessions
   - Each card: Date (formatted), Group name, Total Volume, Duration, muscle group chips
   - Empty state illustration for first-time users
2. Create `SessionDetailScreen`:
   - Header: Date, duration, total volume, group name
   - Expandable list of exercises with all sets displayed as a table-like layout
   - PR badges on sets that exceed historical maximum
   - "Export This Session" button in top menu
3. Create `CsvExporter` utility:
   - Generate CSV exactly matching PRD Section 3.6 schema
   - Use `ContentResolver` + `MediaStore.Downloads` to save to Downloads folder without `WRITE_EXTERNAL_STORAGE` permission (Android 10+ scoped storage)
   - Filename: `WorkoutHistory_YYYY-MM-DD_HH-MM.csv`
   - Show Snackbar with "Open" action on success
4. Create `HistoryViewModel` with queries:
   - `getAllSessionsWithSummary()`
   - `getSessionDetail(sessionId)`
   - `exportAllHistory()` and `exportSession(sessionId)`
5. Add a simple line chart for exercise progression:
   - Use a lightweight charting library (e.g., `vico` or manual Canvas)
   - Accessible from Exercise Detail: "View Progress"
   - X-axis: Date, Y-axis: Max weight for that exercise

**Deliverable:** Users can browse history, inspect details, see PRs, and export CSVs.

---

## AGENT 6: Settings, Theming & Polish
**Task:** Final integration, settings screen, dark mode, and app-wide polish.

**Instructions:**
1. Create `SettingsScreen` with DataStore-backed preferences:
   - Unit toggle (kg/lb) — affects all weight inputs and CSV
   - Default rest timer duration slider (30s - 300s)
   - Theme selector (Light/Dark/System)
   - Clear all history (with confirmation + cascading delete)
   - Reset exercise database (re-run seeder)
2. Implement Material Design 3 dynamic theming or a custom fitness-themed color scheme (deep blue/orange accent).
3. Add app icon (use a generic dumbbell vector if no custom asset).
4. Add edge-to-edge display support, window insets handling.
5. Ensure all screens handle configuration changes (rotation) without losing state.
6. Add a one-time onboarding screen explaining:
   - How to create groups
   - How to start a workout
   - Where to place the dataset assets (critical!)
7. Review all screens for accessibility: content descriptions, touch targets min 48dp, color contrast.
8. Add ProGuard/R8 rules for Room entities if building release.

**Deliverable:** Production-ready app with settings, theming, onboarding, and polish.

---

## AGENT 7: Integration & QA Lead
**Task:** Wire everything together, fix navigation, and ensure end-to-end flow works.

**Instructions:**
1. Set up `AppNavigation` with type-safe routes connecting all screens.
2. Define route enums for: Library, Groups, GroupEditor, LiveWorkout, History, SessionDetail, Settings, ExerciseDetail (bottom sheet).
3. Pass only primitive arguments (IDs) between screens; ViewModels re-fetch data.
4. Run through the complete user flow:
   - Fresh install → seed DB → create group → add exercises → start workout → log sets → finish → view history → export CSV
5. Fix any navigation back-stack issues (e.g., finishing workout should pop to Groups, not back to Live Workout).
6. Verify CSV output by opening in a spreadsheet.
7. Add `README.md` at project root with:
   - How to build
   - Where to download and place the exercises-dataset assets
   - Architecture explanation
   - Screenshot placeholders

**Deliverable:** A fully integrated, buildable Android project with no broken flows.

---

## CRITICAL IMPLEMENTATION NOTES FOR ALL AGENTS

1. **Asset Loading for GIFs/Images:** Since media is in `assets/`, use Coil with custom fetchers or Android's `AssetManager` directly:
   ```kotlin
   // For images
   val painter = rememberAsyncImagePainter(
       model = ImageRequest.Builder(context)
           .data("file:///android_asset/${exercise.imagePath}")
           .build()
   )
   // For GIFs, ensure Coil GIF decoder is added
   ```

2. **Room Relationships:** Use `@Embedded` + `@Relation` for `GroupWithExercises` and `SessionWithSets`.

3. **CSV Generation:** Use `StringBuilder` with proper CSV escaping (quotes around fields with commas). Example:
   ```kotlin
   fun escapeCsv(field: String): String {
       return if (field.contains(",") || field.contains(""") || field.contains("\n")) {
           "\"${field.replace("\"", "\"\"")}\""
       } else field
   }
   ```

4. **Rest Timer Service:** For reliability when app is backgrounded, consider a `WorkManager` task or foreground `Service`. For MVP, a `CountDownTimer` in ViewModel with a notification is sufficient.

5. **Comments Standard:** Every file must start with a KDoc explaining its purpose. Every public function must have `@param` and `@return` tags. Complex algorithms need inline comments.

6. **No Hardcoded Strings:** All user-facing text in `strings.xml`. All dimensions in `dimens.xml`.

---

## FINAL CHECKLIST BEFORE SUBMISSION

- [ ] App compiles without errors
- [ ] All 1,324 exercises are searchable and filterable
- [ ] Groups can be created, edited, and deleted
- [ ] Live workout logs sets with weight, reps, RPE
- [ ] Rest timer works and is configurable
- [ ] History shows all past workouts with full detail
- [ ] CSV export works and opens correctly in Excel/Sheets
- [ ] Dark mode works
- [ ] All screens handle empty states
- [ ] No network permissions in manifest
- [ ] Database is fully local and survives process death

---

**Execute all agents sequentially. Do not skip steps. Comment every non-trivial decision. The output must be a complete, buildable Android Studio project.**
