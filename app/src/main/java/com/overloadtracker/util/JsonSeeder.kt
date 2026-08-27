/**
 * Parses assets/data/exercises.json and seeds the local Room [Exercise] table.
 */
package com.overloadtracker.util

import android.content.Context
import com.overloadtracker.data.local.entity.Exercise
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

object JsonSeeder {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Reads the bundled exercises dataset and maps each record to an [Exercise] entity.
     *
     * @param context Android context used to open assets.
     * @return list of entities ready for Room insert.
     */
    fun loadExercisesFromAssets(context: Context): List<Exercise> {
        val raw = context.assets.open(Constants.EXERCISES_JSON_PATH)
            .bufferedReader()
            .use { it.readText() }
        return parseExercisesJson(raw)
    }

    /**
     * Pure parser used by unit tests — no Android dependency beyond JSON structure.
     *
     * @param rawJson full JSON array string from exercises.json.
     * @return mapped [Exercise] list.
     */
    fun parseExercisesJson(rawJson: String): List<Exercise> {
        // Prefer org.json for robust handling of nested instruction objects / arrays.
        val array = JSONArray(rawJson)
        val result = ArrayList<Exercise>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += mapExercise(obj)
        }
        return result
    }

    private fun mapExercise(obj: JSONObject): Exercise {
        val id = obj.getString("id")
        val instructions = extractEnglishInstructions(obj)
        val secondary = obj.optJSONArray("secondary_muscles") ?: JSONArray()
        val secondaryList = buildList {
            for (i in 0 until secondary.length()) add(secondary.getString(i))
        }
        return Exercise(
            id = id,
            name = obj.getString("name"),
            category = obj.optString("category", obj.optString("body_part", "")),
            equipment = obj.optString("equipment", ""),
            target = obj.optString("target", ""),
            muscleGroup = obj.optString("muscle_group", ""),
            secondaryMuscles = JSONArray(secondaryList).toString(),
            instructions = instructions,
            imagePath = obj.optString("image", ""),
            gifPath = obj.optString("gif_url", "")
        )
    }

    private fun extractEnglishInstructions(obj: JSONObject): String {
        val steps = obj.optJSONObject("instruction_steps")
        if (steps != null) {
            val enSteps = steps.optJSONArray("en")
            if (enSteps != null && enSteps.length() > 0) {
                return buildString {
                    for (i in 0 until enSteps.length()) {
                        append(i + 1).append(". ").append(enSteps.getString(i))
                        if (i < enSteps.length() - 1) append('\n')
                    }
                }
            }
        }
        val instructions = obj.optJSONObject("instructions")
        if (instructions != null) {
            return instructions.optString("en", "")
        }
        return obj.optString("instructions", "")
    }
}

@Serializable
data class ExerciseJsonDto(
    val id: String,
    val name: String,
    val category: String = "",
    @SerialName("body_part") val bodyPart: String = "",
    val equipment: String = "",
    val target: String = "",
    @SerialName("muscle_group") val muscleGroup: String = "",
    @SerialName("secondary_muscles") val secondaryMuscles: List<String> = emptyList(),
    val image: String = "",
    @SerialName("gif_url") val gifUrl: String = ""
)
