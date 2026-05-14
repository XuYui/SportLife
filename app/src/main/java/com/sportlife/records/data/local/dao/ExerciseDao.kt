package com.sportlife.records.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.sportlife.records.data.local.entity.ExerciseEntity
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_groups ORDER BY sortOrder, name")
    fun observeGroups(): Flow<List<ExerciseGroupEntity>>

    @Query("SELECT * FROM exercises ORDER BY bodyPart, name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercise_groups")
    suspend fun groupCount(): Int

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun exerciseCount(): Int

    @Query("SELECT * FROM exercise_groups WHERE bodyPart = :bodyPart ORDER BY sortOrder LIMIT 1")
    suspend fun firstGroupForBodyPart(bodyPart: String): ExerciseGroupEntity?

    @Query("SELECT * FROM exercises WHERE name = :name AND bodyPart = :bodyPart LIMIT 1")
    suspend fun findExercise(name: String, bodyPart: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGroup(group: ExerciseGroupEntity): Long

    @Upsert
    suspend fun upsertGroup(group: ExerciseGroupEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Upsert
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
}
