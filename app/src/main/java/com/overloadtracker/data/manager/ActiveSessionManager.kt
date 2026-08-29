package com.overloadtracker.data.manager

import android.content.Context
import com.overloadtracker.ui.screens.workout.LiveExercise
import com.overloadtracker.ui.screens.workout.LiveSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveWorkoutSession(
    val groupId: Long,
    val groupName: String,
    val startTime: Long,
    val exercises: List<LiveExercise> = emptyList()
)

@Singleton
class ActiveSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("active_workout_session_prefs", Context.MODE_PRIVATE)
    private val _activeSession = MutableStateFlow<ActiveWorkoutSession?>(loadSavedSession())
    val activeSession: StateFlow<ActiveWorkoutSession?> = _activeSession.asStateFlow()

    fun startSession(groupId: Long, groupName: String, startTime: Long = System.currentTimeMillis()) {
        val session = ActiveWorkoutSession(
            groupId = groupId,
            groupName = groupName,
            startTime = startTime
        )
        _activeSession.value = session
        persistSession(session)
    }

    fun updateExercises(exercises: List<LiveExercise>) {
        val updated = _activeSession.value?.copy(exercises = exercises) ?: return
        _activeSession.value = updated
        persistSession(updated)
    }

    fun updateGroupName(groupName: String) {
        val updated = _activeSession.value?.copy(groupName = groupName) ?: return
        _activeSession.value = updated
        persistSession(updated)
    }

    fun clearSession() {
        _activeSession.value = null
        prefs.edit().remove("active_session_json").apply()
    }

    private fun persistSession(session: ActiveWorkoutSession) {
        try {
            val root = JSONObject()
            root.put("groupId", session.groupId)
            root.put("groupName", session.groupName)
            root.put("startTime", session.startTime)

            val exArray = JSONArray()
            for (ex in session.exercises) {
                val exObj = JSONObject()
                exObj.put("exerciseId", ex.exerciseId)
                exObj.put("exerciseName", ex.exerciseName)
                exObj.put("isCardio", ex.isCardio)
                exObj.put("isBodyweight", ex.isBodyweight)
                exObj.put("expanded", ex.expanded)
                exObj.put("prevBestLabel", ex.prevBestLabel ?: JSONObject.NULL)

                val setArray = JSONArray()
                for (s in ex.sets) {
                    val sObj = JSONObject()
                    sObj.put("setNumber", s.setNumber)
                    sObj.put("weightKg", s.weightKg)
                    sObj.put("reps", s.reps)
                    sObj.put("timeSeconds", s.timeSeconds ?: JSONObject.NULL)
                    sObj.put("count", s.count ?: JSONObject.NULL)
                    sObj.put("rpe", s.rpe ?: JSONObject.NULL)
                    sObj.put("isCompleted", s.isCompleted)
                    sObj.put("weightDisplay", s.weightDisplay)
                    sObj.put("repsDisplay", s.repsDisplay)
                    sObj.put("timeDisplay", s.timeDisplay)
                    sObj.put("countDisplay", s.countDisplay)
                    setArray.put(sObj)
                }
                exObj.put("sets", setArray)
                exArray.put(exObj)
            }
            root.put("exercises", exArray)

            prefs.edit().putString("active_session_json", root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedSession(): ActiveWorkoutSession? {
        val jsonStr = prefs.getString("active_session_json", null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val groupId = root.getLong("groupId")
            val groupName = root.getString("groupName")
            val startTime = root.getLong("startTime")

            val exArray = root.getJSONArray("exercises")
            val exercises = mutableListOf<LiveExercise>()
            for (i in 0 until exArray.length()) {
                val exObj = exArray.getJSONObject(i)
                val exerciseId = exObj.getString("exerciseId")
                val exerciseName = exObj.getString("exerciseName")
                val isCardio = exObj.optBoolean("isCardio", false)
                val isBodyweight = exObj.optBoolean("isBodyweight", false)
                val expanded = exObj.optBoolean("expanded", true)
                val prevBestLabel = if (exObj.isNull("prevBestLabel")) null else exObj.optString("prevBestLabel")

                val setArray = exObj.getJSONArray("sets")
                val sets = mutableListOf<LiveSet>()
                for (j in 0 until setArray.length()) {
                    val sObj = setArray.getJSONObject(j)
                    sets.add(
                        LiveSet(
                            setNumber = sObj.getInt("setNumber"),
                            weightKg = sObj.getDouble("weightKg"),
                            reps = sObj.getInt("reps"),
                            timeSeconds = if (sObj.isNull("timeSeconds")) null else sObj.optInt("timeSeconds"),
                            count = if (sObj.isNull("count")) null else sObj.optInt("count"),
                            rpe = if (sObj.isNull("rpe")) null else sObj.optInt("rpe"),
                            isCompleted = sObj.getBoolean("isCompleted"),
                            weightDisplay = sObj.optString("weightDisplay", ""),
                            repsDisplay = sObj.optString("repsDisplay", ""),
                            timeDisplay = sObj.optString("timeDisplay", ""),
                            countDisplay = sObj.optString("countDisplay", "")
                        )
                    )
                }
                exercises.add(
                    LiveExercise(
                        exerciseId = exerciseId,
                        exerciseName = exerciseName,
                        isCardio = isCardio,
                        isBodyweight = isBodyweight,
                        sets = sets,
                        expanded = expanded,
                        prevBestLabel = prevBestLabel
                    )
                )
            }
            ActiveWorkoutSession(
                groupId = groupId,
                groupName = groupName,
                startTime = startTime,
                exercises = exercises
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
