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

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

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
            "date,workout_name,muscle_groups,exercise_name,equipment,set_number,weight_kg,reps,time_seconds,count,rpe,total_volume,notes"
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
                .append(r.timeSeconds?.toString().orEmpty()).append(',')
                .append(r.count?.toString().orEmpty()).append(',')
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

    /**
     * Writes CSV to cache directory and launches Android share intent chooser.
     * Allows sharing directly to apps like Claude, Gemini, WhatsApp, etc.
     *
     * @return content [Uri] of the shared file, or null on failure.
     */
    fun shareCsvFile(context: Context, csv: String, prefix: String = "WorkoutHistory"): Uri? {
        return try {
            val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
            val fileName = "${prefix}_$stamp.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val file = File(exportDir, fileName)
            file.writeText(csv, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Workout History CSV Export")
                putExtra(Intent.EXTRA_TEXT, "Here is my workout history CSV export.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Workout CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun escape(field: String): String {
        return if (field.contains(',') || field.contains('"') || field.contains('\n')) {
            "\"${field.replace("\"", "\"\"")}\""
        } else field
    }
}
