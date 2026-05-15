package com.sportlife.records.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.sportlife.records.data.local.SportLifeDatabase
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.entity.toEntity
import com.sportlife.records.data.local.relation.TrainingDaySectionWithExercises
import com.sportlife.records.data.local.relation.TrainingDayWithExercises
import com.sportlife.records.data.local.relation.TrainingPlanWithDays
import com.sportlife.records.data.repository.UserPreferencesRepository
import com.sportlife.records.domain.model.TrainingSplitType
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class TrainingPlanSnapshotRepository(
    context: Context,
    private val database: SportLifeDatabase,
) {
    private val preferences = context.getSharedPreferences(UserPreferencesRepository.PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val trainingPlanDao = database.trainingPlanDao()

    suspend fun saveActivePlanSnapshot() {
        database.withTransaction {
            val plan = trainingPlanDao.getActivePlan() ?: return@withTransaction
            saveSnapshot(plan.toSnapshotJson())
        }
    }

    suspend fun restoreSnapshotIfNeeded(): Boolean =
        database.withTransaction {
            val snapshot = currentSnapshotJson()?.let(::JSONObject) ?: return@withTransaction false
            val storedFingerprint = preferences.getString(KEY_TRAINING_PLAN_FINGERPRINT, null)
            val currentPlan = trainingPlanDao.getActivePlan()
            val shouldRestore = currentPlan == null || (
                storedFingerprint != null &&
                    currentPlan.fingerprint() != storedFingerprint &&
                    currentPlan.looksLikeGeneratedDefault()
                )

            if (!shouldRestore) return@withTransaction false
            restoreSnapshot(snapshot)
            true
        }

    fun currentSnapshotJson(): String? =
        preferences.getString(KEY_TRAINING_PLAN_SNAPSHOT, null)

    fun saveSnapshotJson(json: String?) {
        if (json.isNullOrBlank()) return
        val snapshot = JSONObject(json)
        saveSnapshot(snapshot)
    }

    private fun saveSnapshot(snapshot: JSONObject) {
        val fingerprint = snapshot.contentFingerprint()
        preferences.edit()
            .putString(KEY_TRAINING_PLAN_SNAPSHOT, snapshot.toString())
            .putString(KEY_TRAINING_PLAN_FINGERPRINT, fingerprint)
            .apply()
    }

    private suspend fun restoreSnapshot(snapshot: JSONObject) {
        val db = database.openHelper.writableDatabase
        db.execSQL("DELETE FROM `training_plan_exercises`")
        db.execSQL("DELETE FROM `training_day_sections`")
        db.execSQL("DELETE FROM `training_days`")
        db.execSQL("DELETE FROM `training_plans`")
        trainingPlanDao.upsertSplits(TrainingSplitType.entries.map { it.toEntity() })

        val planJson = snapshot.getJSONObject("plan")
        val newPlanId = trainingPlanDao.insertPlan(
            TrainingPlanEntity(
                name = planJson.optString("name", "自定义训练计划"),
                splitId = planJson.optString("splitId", TrainingSplitType.Custom.id),
                isActive = true,
                createdAtMillis = planJson.optLong("createdAtMillis", System.currentTimeMillis()),
                updatedAtMillis = System.currentTimeMillis(),
            ),
        )
        val days = snapshot.optJSONArray("days") ?: JSONArray()
        repeat(days.length()) { dayIndex ->
            restoreDay(days.getJSONObject(dayIndex), newPlanId)
        }
        saveSnapshot(snapshot)
    }

    private suspend fun restoreDay(dayJson: JSONObject, planId: Long) {
        val day = dayJson.getJSONObject("day")
        val dayId = trainingPlanDao.insertDay(
            TrainingDayEntity(
                planId = planId,
                dayIndex = day.optInt("dayIndex", 0),
                name = day.optString("name", "训练日"),
                focusBodyPart = day.optStringOrNull("focusBodyPart"),
            ),
        )

        val sections = dayJson.optJSONArray("sections") ?: JSONArray()
        repeat(sections.length()) { sectionIndex ->
            val sectionJson = sections.getJSONObject(sectionIndex)
            val section = sectionJson.getJSONObject("section")
            val sectionId = trainingPlanDao.insertSection(
                TrainingDaySectionEntity(
                    trainingDayId = dayId,
                    name = section.optString("name", "小板块"),
                    bodyPart = section.optStringOrNull("bodyPart"),
                    sortOrder = section.optInt("sortOrder", sectionIndex),
                ),
            )
            restoreExercises(sectionJson.optJSONArray("exercises") ?: JSONArray(), dayId, sectionId)
        }
        restoreExercises(dayJson.optJSONArray("unsectionedExercises") ?: JSONArray(), dayId, null)
    }

    private suspend fun restoreExercises(
        exercises: JSONArray,
        dayId: Long,
        sectionId: Long?,
    ) {
        repeat(exercises.length()) { index ->
            val exercise = exercises.getJSONObject(index)
            trainingPlanDao.insertPlanExercise(
                TrainingPlanExerciseEntity(
                    trainingDayId = dayId,
                    sectionId = sectionId,
                    exerciseName = exercise.optString("exerciseName", "动作"),
                    bodyPart = exercise.optString("bodyPart", "自定义"),
                    sets = exercise.optInt("sets", 4),
                    defaultWeightKg = exercise.optDouble("defaultWeightKg", 0.0),
                    defaultReps = exercise.optInt("defaultReps", 10),
                    note = exercise.optString("note", ""),
                    sortOrder = exercise.optInt("sortOrder", index),
                ),
            )
        }
    }

    private fun TrainingPlanWithDays.toSnapshotJson(): JSONObject =
        JSONObject()
            .put("formatVersion", 1)
            .put("savedAtMillis", System.currentTimeMillis())
            .put(
                "plan",
                JSONObject()
                    .put("name", plan.name)
                    .put("splitId", plan.splitId)
                    .put("createdAtMillis", plan.createdAtMillis),
            )
            .put(
                "days",
                JSONArray().apply {
                    days.sortedBy { it.day.dayIndex }.forEach { put(it.toJson()) }
                },
            )

    private fun TrainingDayWithExercises.toJson(): JSONObject {
        val sectionIds = sections.map { it.section.id }.toSet()
        return JSONObject()
            .put(
                "day",
                JSONObject()
                    .put("dayIndex", day.dayIndex)
                    .put("name", day.name)
                    .putNullable("focusBodyPart", day.focusBodyPart),
            )
            .put(
                "sections",
                JSONArray().apply {
                    sections.sortedBy { it.section.sortOrder }.forEach { put(it.toJson()) }
                },
            )
            .put(
                "unsectionedExercises",
                JSONArray().apply {
                    exercises
                        .filter { it.sectionId == null || it.sectionId !in sectionIds }
                        .sortedBy { it.sortOrder }
                        .forEach { put(it.toJson()) }
                },
            )
    }

    private fun TrainingDaySectionWithExercises.toJson(): JSONObject =
        JSONObject()
            .put(
                "section",
                JSONObject()
                    .put("name", section.name)
                    .putNullable("bodyPart", section.bodyPart)
                    .put("sortOrder", section.sortOrder),
            )
            .put(
                "exercises",
                JSONArray().apply {
                    exercises.sortedBy { it.sortOrder }.forEach { put(it.toJson()) }
                },
            )

    private fun TrainingPlanExerciseEntity.toJson(): JSONObject =
        JSONObject()
            .put("exerciseName", exerciseName)
            .put("bodyPart", bodyPart)
            .put("sets", sets)
            .put("defaultWeightKg", defaultWeightKg)
            .put("defaultReps", defaultReps)
            .put("note", note)
            .put("sortOrder", sortOrder)

    private fun TrainingPlanWithDays.fingerprint(): String =
        toSnapshotJson().contentFingerprint()

    private fun JSONObject.contentFingerprint(): String {
        val content = JSONObject(toString())
            .removeKey("savedAtMillis")
            .toString()
        return sha256(content)
    }

    private fun TrainingPlanWithDays.looksLikeGeneratedDefault(): Boolean {
        return plan.name in setOf(
            "三分化计划",
            "四分化计划",
            "自定义计划",
            "三分化默认计划",
            "四分化默认计划",
            "自定义默认计划",
        )
    }

    private fun JSONObject.putNullable(key: String, value: String?): JSONObject =
        if (value == null) put(key, JSONObject.NULL) else put(key, value)

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.removeKey(key: String): JSONObject =
        apply { remove(key) }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_TRAINING_PLAN_SNAPSHOT = "training_plan_snapshot"
        const val KEY_TRAINING_PLAN_FINGERPRINT = "training_plan_fingerprint"
    }
}
