package com.sportlife.records.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sportlife.records.data.local.entity.RunningRecordEntity
import com.sportlife.records.data.local.entity.StrengthRecordEntity
import com.sportlife.records.data.local.entity.WorkoutCheckInEntity
import com.sportlife.records.data.local.relation.RunningStatRow
import com.sportlife.records.data.local.relation.StrengthBodyPartCountRow
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutCheckInDao {
    @Transaction
    @Query(
        """
        SELECT * FROM workout_check_ins
        ORDER BY dateEpochDay DESC, createdAtMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<WorkoutWithSportType>>

    @Transaction
    @Query(
        """
        SELECT * FROM workout_check_ins
        ORDER BY dateEpochDay DESC, createdAtMillis DESC
        """,
    )
    fun observeHistory(): Flow<List<WorkoutWithSportType>>

    @Query("SELECT COUNT(*) FROM workout_check_ins WHERE dateEpochDay = :dateEpochDay")
    fun observeCheckInCountForDate(dateEpochDay: Long): Flow<Int>

    @Query(
        """
        SELECT c.dateEpochDay AS dateEpochDay,
               r.distanceKm AS distanceKm,
               r.paceSecondsPerKm AS paceSecondsPerKm
        FROM running_records r
        INNER JOIN workout_check_ins c ON c.id = r.checkInId
        ORDER BY c.dateEpochDay ASC, c.createdAtMillis ASC
        """,
    )
    fun observeRunningStats(): Flow<List<RunningStatRow>>

    @Query(
        """
        SELECT primaryBodyPart AS bodyPart,
               COUNT(*) AS count
        FROM strength_records
        GROUP BY primaryBodyPart
        ORDER BY count DESC
        """,
    )
    fun observeStrengthBodyPartCounts(): Flow<List<StrengthBodyPartCountRow>>

    @Query("SELECT * FROM workout_check_ins WHERE id = :checkInId LIMIT 1")
    suspend fun getCheckIn(checkInId: Long): WorkoutCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckIn(checkIn: WorkoutCheckInEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRunningRecord(record: RunningRecordEntity)

    @Query("SELECT * FROM running_records WHERE checkInId = :checkInId LIMIT 1")
    suspend fun getRunningRecord(checkInId: Long): RunningRecordEntity?

    @Query("SELECT * FROM strength_records WHERE checkInId = :checkInId LIMIT 1")
    suspend fun getStrengthRecord(checkInId: Long): StrengthRecordEntity?

    @Update
    suspend fun updateCheckIn(checkIn: WorkoutCheckInEntity)

    @Update
    suspend fun updateRunningRecord(record: RunningRecordEntity)

    @Update
    suspend fun updateStrengthRecord(record: StrengthRecordEntity)

    @Query("DELETE FROM workout_check_ins WHERE id = :checkInId")
    suspend fun deleteCheckIn(checkInId: Long)
}
