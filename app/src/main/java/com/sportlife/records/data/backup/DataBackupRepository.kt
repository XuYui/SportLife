package com.sportlife.records.data.backup

import android.database.Cursor
import androidx.room.withTransaction
import com.sportlife.records.data.local.DefaultDataSeeder
import com.sportlife.records.data.local.SportLifeDatabase
import com.sportlife.records.data.repository.UserPreferencesRepository
import org.json.JSONArray
import org.json.JSONObject

data class BackupImportResult(
    val importedTables: Int,
    val importedRows: Int,
)

class DataBackupRepository(
    private val database: SportLifeDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val trainingPlanSnapshotRepository: TrainingPlanSnapshotRepository,
) {
    suspend fun exportJson(): String =
        database.withTransaction {
            val db = database.openHelper.writableDatabase
            JSONObject()
                .put("app", "SportLife")
                .put("formatVersion", 1)
                .put("roomSchemaVersion", 2)
                .put("exportedAtMillis", System.currentTimeMillis())
                .put(
                    "preferences",
                    JSONObject()
                        .put("homeSlogan", userPreferencesRepository.currentHomeSlogan())
                        .put("trainingPlanSnapshot", trainingPlanSnapshotRepository.currentSnapshotJson()),
                )
                .put(
                    "tables",
                    JSONObject().apply {
                        backupTables.forEach { table ->
                            put(table, db.query("SELECT * FROM `$table`").useRows())
                        }
                    },
                )
                .toString(2)
        }

    suspend fun importJson(json: String): BackupImportResult =
        database.withTransaction {
            val root = JSONObject(json)
            val tables = root.getJSONObject("tables")
            val preferences = root.optJSONObject("preferences")
            val db = database.openHelper.writableDatabase
            db.execSQL("PRAGMA foreign_keys=OFF")
            try {
                deleteOrder.forEach { table ->
                    db.execSQL("DELETE FROM `$table`")
                }

                var importedTables = 0
                var importedRows = 0
                backupTables.forEach { table ->
                    if (tables.has(table)) {
                        val rows = tables.getJSONArray(table)
                        val columns = currentColumns(table)
                        repeat(rows.length()) { index ->
                            val row = rows.getJSONObject(index)
                            insertRow(table, row, columns)
                            importedRows += 1
                        }
                        importedTables += 1
                    }
                }
                db.execSQL("PRAGMA foreign_keys=ON")
                DefaultDataSeeder(database).seedIfNeeded()
                val importedPlanRows = tables.optJSONArray("training_plans")?.length() ?: 0
                val importedPlanSnapshot = preferences
                    ?.optString("trainingPlanSnapshot")
                    ?.takeIf { it.isNotBlank() }
                preferences
                    ?.optString("homeSlogan")
                    ?.takeIf { it.isNotBlank() }
                    ?.let(userPreferencesRepository::saveHomeSlogan)
                importedPlanSnapshot?.let(trainingPlanSnapshotRepository::saveSnapshotJson)
                if (importedPlanRows == 0 && importedPlanSnapshot != null) {
                    trainingPlanSnapshotRepository.restoreSnapshotIfNeeded()
                } else {
                    trainingPlanSnapshotRepository.saveActivePlanSnapshot()
                }
                BackupImportResult(importedTables = importedTables, importedRows = importedRows)
            } catch (throwable: Throwable) {
                db.execSQL("PRAGMA foreign_keys=ON")
                throw throwable
            }
        }

    private fun Cursor.useRows(): JSONArray =
        use { cursor ->
            JSONArray().apply {
                while (cursor.moveToNext()) {
                    put(cursor.toJsonObject())
                }
            }
        }

    private fun Cursor.toJsonObject(): JSONObject =
        JSONObject().apply {
            for (index in 0 until columnCount) {
                val name = getColumnName(index)
                when (getType(index)) {
                    Cursor.FIELD_TYPE_NULL -> put(name, JSONObject.NULL)
                    Cursor.FIELD_TYPE_INTEGER -> put(name, getLong(index))
                    Cursor.FIELD_TYPE_FLOAT -> put(name, getDouble(index))
                    Cursor.FIELD_TYPE_STRING -> put(name, getString(index))
                    Cursor.FIELD_TYPE_BLOB -> put(name, android.util.Base64.encodeToString(getBlob(index), android.util.Base64.NO_WRAP))
                }
            }
        }

    private fun currentColumns(table: String): List<String> {
        val db = database.openHelper.writableDatabase
        return db.query("PRAGMA table_info(`$table`)").use { cursor ->
            buildList {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
    }

    private fun insertRow(
        table: String,
        row: JSONObject,
        currentColumns: List<String>,
    ) {
        val columns = currentColumns.filter { row.has(it) && !row.isNull(it) }
        if (columns.isEmpty()) return

        val placeholders = columns.joinToString(", ") { "?" }
        val columnSql = columns.joinToString(", ") { "`$it`" }
        val args: Array<Any?> = columns.map { column ->
            val value = row.get(column)
            when (value) {
                is Boolean -> if (value) 1L else 0L
                is Int -> value.toLong()
                is Long -> value
                is Double -> value
                is Number -> value.toString()
                else -> value.toString()
            }
        }.toTypedArray()
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO `$table` ($columnSql) VALUES ($placeholders)",
            args,
        )
    }

    private companion object {
        val backupTables = listOf(
            "sport_types",
            "exercise_groups",
            "exercises",
            "training_splits",
            "workout_check_ins",
            "running_records",
            "strength_records",
            "strength_exercise_records",
            "strength_exercise_sets",
            "training_plans",
            "training_days",
            "training_day_sections",
            "training_plan_exercises",
        )

        val deleteOrder = listOf(
            "training_plan_exercises",
            "training_day_sections",
            "training_days",
            "training_plans",
            "training_splits",
            "strength_exercise_sets",
            "strength_exercise_records",
            "strength_records",
            "running_records",
            "workout_check_ins",
            "exercises",
            "exercise_groups",
            "sport_types",
        )
    }
}
