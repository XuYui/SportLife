package com.sportlife.records.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sportlife.records.data.local.entity.StrengthExerciseRecordEntity
import com.sportlife.records.data.local.entity.StrengthExerciseSetEntity
import com.sportlife.records.data.local.entity.StrengthRecordEntity
import com.sportlife.records.data.local.relation.StrengthRecordWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface StrengthDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStrengthRecord(record: StrengthRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExerciseRecord(record: StrengthExerciseRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExerciseSets(sets: List<StrengthExerciseSetEntity>)

    @Transaction
    @Query("SELECT * FROM strength_records WHERE checkInId = :checkInId LIMIT 1")
    fun observeStrengthRecord(checkInId: Long): Flow<StrengthRecordWithExercises?>

    @Transaction
    @Query("SELECT * FROM strength_records WHERE checkInId = :checkInId LIMIT 1")
    suspend fun getStrengthRecordWithExercises(checkInId: Long): StrengthRecordWithExercises?
}
