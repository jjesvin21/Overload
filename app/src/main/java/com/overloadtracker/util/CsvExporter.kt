/**
 * CSV export for LLM analysis — schema matches PRD §3.6.
 */
package com.overloadtracker.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.overloadtracker.data.model.CsvExportRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    /**
     * Builds CSV text from export rows.
     *
     * @param rows one row per set.
     * @return CSV document string including header.
     */
    fun buildCsv(rows: List<CsvExportRow>): String {
        val sb = StringBuilder()
        sb.appendLine(
            "date,workout_name,muscle_groups,exercise_name,equipment,set_number,weight_kg,reps,rpe,total_volume,notes"
        )
        rows.forEach { r ->
            sb.append(escape(r.date)).append(',')
                .append(escape(r.workoutName)).append(',')
                .append(escape(r.muscleGroups)).append(',')
                .append(escape(r.exerciseName)).append(',')
                .append(escape(r.equipment)).append(',')
                .append(r.setNumber).append(',')
                .append(r.weightKg).append(',')
                .append(r.reps).append(',')
                .append(r.rpe?.toString().orEmpty()).append(',')
                .append(r.totalVolume).append(',')
                .append(escape(r.notes))
                .appendLine()
        }
        return sb.toString()
    }

    /**
     * Saves CSV into the public Downloads collection via MediaStore (no legacy storage permission).
     *
     * @return content [Uri] of the written file, or null on failure.
     */
    fun saveToDownloads(context: Context, csv: String, prefix: String = "WorkoutHistory"): Uri? {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        val fileName = "${prefix}_$stamp.csv"
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val uri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(uri)?.use { out ->
            out.write(csv.toByteArray(Charsets.UTF_8))
        } ?: return null
        return uri
    }

    fun escape(field: String): String {
        return if (field.contains(',') || field.contains('"') || field.contains('\n')) {
            "\"${field.replace("\"", "\"\"")}\""
        } else field
    }
}
