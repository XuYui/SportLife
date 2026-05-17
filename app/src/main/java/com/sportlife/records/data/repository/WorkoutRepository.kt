package com.sportlife.records.data.repository

import androidx.room.withTransaction
import com.sportlife.records.data.local.SportLifeDatabase
import com.sportlife.records.data.local.entity.RunningRecordEntity
import com.sportlife.records.data.local.entity.StrengthExerciseRecordEntity
import com.sportlife.records.data.local.entity.StrengthExerciseSetEntity
import com.sportlife.records.data.local.entity.StrengthRecordEntity
import com.sportlife.records.data.local.entity.WorkoutCheckInEntity
import com.sportlife.records.data.local.entity.toEntity
import com.sportlife.records.data.local.relation.RunningStatRow
import com.sportlife.records.data.local.relation.StrengthBodyPartCountRow
import com.sportlife.records.data.local.relation.StrengthRecordWithExercises
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.model.displayBodyPartName
import com.sportlife.records.domain.model.normalizeCustomFocus
import com.sportlife.records.domain.util.formatPace
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class RunningCheckInInput(
    val date: LocalDate,
    val distanceKm: Double,
    val paceSecondsPerKm: Int?,
    val note: String,
)

data class StrengthSetInput(
    val weightKg: Double,
    val reps: Int,
)

data class StrengthExerciseInput(
    val exerciseId: Long? = null,
    val exerciseName: String,
    val bodyPart: BodyPart,
    val sets: List<StrengthSetInput>,
    val note: String = "",
)

data class StrengthCheckInInput(
    val date: LocalDate,
    val primaryBodyPart: String,
    val exercises: List<StrengthExerciseInput> = emptyList(),
    val note: String,
)

interface WorkoutRepository {
    fun observeRecentCheckIns(limit: Int = 5): Flow<List<WorkoutWithSportType>>
    fun observeHistory(): Flow<List<WorkoutWithSportType>>
    fun observeTodayCheckInCount(today: LocalDate = LocalDate.now()): Flow<Int>
    fun observeRunningStats(): Flow<List<RunningStatRow>>
    fun observeStrengthBodyPartCounts(): Flow<List<StrengthBodyPartCountRow>>
    suspend fun getRunningRecord(checkInId: Long): RunningRecordEntity?
    suspend fun getStrengthRecord(checkInId: Long): StrengthRecordEntity?
    suspend fun getStrengthRecordWithExercises(checkInId: Long): StrengthRecordWithExercises?
    suspend fun saveRunningCheckIn(input: RunningCheckInInput): Long
    suspend fun saveStrengthCheckIn(input: StrengthCheckInInput): Long
    suspend fun updateRunningCheckIn(checkInId: Long, input: RunningCheckInInput)
    suspend fun updateStrengthCheckIn(checkInId: Long, input: StrengthCheckInInput)
    suspend fun deleteCheckIn(checkInId: Long)
}

class OfflineWorkoutRepository(
    private val database: SportLifeDatabase,
) : WorkoutRepository {
    private val workoutDao = database.workoutCheckInDao()
    private val strengthDao = database.strengthDao()

    override fun observeRecentCheckIns(limit: Int): Flow<List<WorkoutWithSportType>> =
        workoutDao.observeRecent(limit)

    override fun observeHistory(): Flow<List<WorkoutWithSportType>> =
        workoutDao.observeHistory()

    override fun observeTodayCheckInCount(today: LocalDate): Flow<Int> =
        workoutDao.observeCheckInCountForDate(today.toEpochDay())

    override fun observeRunningStats(): Flow<List<RunningStatRow>> =
        workoutDao.observeRunningStats()

    override fun observeStrengthBodyPartCounts(): Flow<List<StrengthBodyPartCountRow>> =
        workoutDao.observeStrengthBodyPartCounts()

    override suspend fun getRunningRecord(checkInId: Long): RunningRecordEntity? =
        workoutDao.getRunningRecord(checkInId)

    override suspend fun getStrengthRecord(checkInId: Long): StrengthRecordEntity? =
        workoutDao.getStrengthRecord(checkInId)

    override suspend fun getStrengthRecordWithExercises(checkInId: Long): StrengthRecordWithExercises? =
        strengthDao.getStrengthRecordWithExercises(checkInId)

    override suspend fun saveRunningCheckIn(input: RunningCheckInInput): Long =
        database.withTransaction {
            database.sportTypeDao().upsertAll(BuiltInSportTypes.all.map { it.toEntity() })
            val paceText = formatPace(input.paceSecondsPerKm).takeIf { it != "-" }
            val checkInId = workoutDao.insertCheckIn(
                WorkoutCheckInEntity(
                    sportTypeId = BuiltInSportTypes.Running.id,
                    dateEpochDay = input.date.toEpochDay(),
                    summary = listOfNotNull(
                        "跑步 ${"%.2f".format(input.distanceKm)} km",
                        paceText,
                    ).joinToString(" · "),
                    note = input.note,
                ),
            )
            workoutDao.insertRunningRecord(
                RunningRecordEntity(
                    checkInId = checkInId,
                    distanceKm = input.distanceKm,
                    paceSecondsPerKm = input.paceSecondsPerKm,
                ),
            )
            checkInId
        }

    override suspend fun updateRunningCheckIn(checkInId: Long, input: RunningCheckInInput) {
        database.withTransaction {
            val checkIn = workoutDao.getCheckIn(checkInId) ?: return@withTransaction
            val running = workoutDao.getRunningRecord(checkInId) ?: return@withTransaction
            val paceText = formatPace(input.paceSecondsPerKm).takeIf { it != "-" }
            workoutDao.updateCheckIn(
                checkIn.copy(
                    dateEpochDay = input.date.toEpochDay(),
                    summary = listOfNotNull(
                        "跑步 ${"%.2f".format(input.distanceKm)} km",
                        paceText,
                    ).joinToString(" · "),
                    note = input.note,
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            )
            workoutDao.updateRunningRecord(
                running.copy(
                    distanceKm = input.distanceKm,
                    paceSecondsPerKm = input.paceSecondsPerKm,
                ),
            )
        }
    }

    override suspend fun saveStrengthCheckIn(input: StrengthCheckInInput): Long =
        database.withTransaction {
            database.sportTypeDao().upsertAll(BuiltInSportTypes.all.map { it.toEntity() })
            val totalSets = input.exercises.sumOf { it.sets.size }
            val primaryBodyPart = normalizeCustomFocus(input.primaryBodyPart)
            val primaryBodyPartLabel = displayBodyPartName(primaryBodyPart)
            val summary = if (input.exercises.isEmpty()) {
                "$primaryBodyPartLabel 训练"
            } else {
                "$primaryBodyPartLabel 训练 ${input.exercises.size} 个动作 / $totalSets 组"
            }
            val checkInId = workoutDao.insertCheckIn(
                WorkoutCheckInEntity(
                    sportTypeId = BuiltInSportTypes.StrengthTraining.id,
                    dateEpochDay = input.date.toEpochDay(),
                    summary = summary,
                    note = input.note,
                ),
            )
            val strengthRecordId = strengthDao.insertStrengthRecord(
                StrengthRecordEntity(
                    checkInId = checkInId,
                    primaryBodyPart = primaryBodyPart,
                    note = input.note,
                ),
            )
            input.exercises.forEachIndexed { exerciseIndex, exercise ->
                val exerciseRecordId = strengthDao.insertExerciseRecord(
                    StrengthExerciseRecordEntity(
                        strengthRecordId = strengthRecordId,
                        exerciseId = exercise.exerciseId,
                        exerciseName = exercise.exerciseName,
                        bodyPart = exercise.bodyPart.name,
                        note = exercise.note,
                        sortOrder = exerciseIndex,
                    ),
                )
                strengthDao.insertExerciseSets(
                    exercise.sets.mapIndexed { setIndex, set ->
                        StrengthExerciseSetEntity(
                            exerciseRecordId = exerciseRecordId,
                            setIndex = setIndex,
                            weightKg = set.weightKg,
                            reps = set.reps,
                        )
                    },
                )
            }
            checkInId
        }

    override suspend fun updateStrengthCheckIn(checkInId: Long, input: StrengthCheckInInput) {
        database.withTransaction {
            val checkIn = workoutDao.getCheckIn(checkInId) ?: return@withTransaction
            val strength = workoutDao.getStrengthRecord(checkInId) ?: return@withTransaction
            val primaryBodyPart = normalizeCustomFocus(input.primaryBodyPart)
            workoutDao.updateCheckIn(
                checkIn.copy(
                    dateEpochDay = input.date.toEpochDay(),
                    summary = "${displayBodyPartName(primaryBodyPart)} 训练",
                    note = input.note,
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            )
            workoutDao.updateStrengthRecord(
                strength.copy(
                    primaryBodyPart = primaryBodyPart,
                    note = input.note,
                ),
            )
        }
    }

    override suspend fun deleteCheckIn(checkInId: Long) {
        workoutDao.deleteCheckIn(checkInId)
    }
}
