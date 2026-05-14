package com.sportlife.records.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.entity.TrainingSplitEntity
import com.sportlife.records.data.local.relation.TrainingPlanWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    @Transaction
    @Query("SELECT * FROM training_plans WHERE isActive = 1 ORDER BY updatedAtMillis DESC LIMIT 1")
    fun observeActivePlan(): Flow<TrainingPlanWithDays?>

    @Query("SELECT COUNT(*) FROM training_plans")
    suspend fun planCount(): Int

    @Query("SELECT COUNT(*) FROM training_day_sections")
    suspend fun sectionCount(): Int

    @Query("SELECT * FROM training_days ORDER BY planId, dayIndex")
    suspend fun getAllDays(): List<TrainingDayEntity>

    @Query("SELECT * FROM training_plan_exercises WHERE trainingDayId = :dayId ORDER BY sortOrder")
    suspend fun getExercisesForDay(dayId: Long): List<TrainingPlanExerciseEntity>

    @Upsert
    suspend fun upsertSplits(splits: List<TrainingSplitEntity>)

    @Query("UPDATE training_plans SET isActive = 0")
    suspend fun deactivatePlans()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlan(plan: TrainingPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDay(day: TrainingDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSection(section: TrainingDaySectionEntity): Long

    @Delete
    suspend fun deleteSection(section: TrainingDaySectionEntity)

    @Query("UPDATE training_plan_exercises SET sectionId = NULL WHERE sectionId = :sectionId")
    suspend fun clearSectionFromExercises(sectionId: Long)

    @Query("UPDATE training_plan_exercises SET sectionId = :sectionId WHERE id = :exerciseId")
    suspend fun updateExerciseSection(exerciseId: Long, sectionId: Long?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlanExercise(exercise: TrainingPlanExerciseEntity): Long

    @Update
    suspend fun updatePlanExercise(exercise: TrainingPlanExerciseEntity)

    @Delete
    suspend fun deletePlanExercise(exercise: TrainingPlanExerciseEntity)
}
