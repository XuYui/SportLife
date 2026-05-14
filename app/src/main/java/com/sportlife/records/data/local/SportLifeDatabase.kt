package com.sportlife.records.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportlife.records.data.local.dao.ExerciseDao
import com.sportlife.records.data.local.dao.SportTypeDao
import com.sportlife.records.data.local.dao.StrengthDao
import com.sportlife.records.data.local.dao.TrainingPlanDao
import com.sportlife.records.data.local.dao.WorkoutCheckInDao
import com.sportlife.records.data.local.entity.ExerciseEntity
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import com.sportlife.records.data.local.entity.RunningRecordEntity
import com.sportlife.records.data.local.entity.SportTypeEntity
import com.sportlife.records.data.local.entity.StrengthExerciseRecordEntity
import com.sportlife.records.data.local.entity.StrengthExerciseSetEntity
import com.sportlife.records.data.local.entity.StrengthRecordEntity
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.entity.TrainingSplitEntity
import com.sportlife.records.data.local.entity.WorkoutCheckInEntity

@Database(
    entities = [
        SportTypeEntity::class,
        WorkoutCheckInEntity::class,
        RunningRecordEntity::class,
        StrengthRecordEntity::class,
        StrengthExerciseRecordEntity::class,
        StrengthExerciseSetEntity::class,
        ExerciseEntity::class,
        ExerciseGroupEntity::class,
        TrainingSplitEntity::class,
        TrainingPlanEntity::class,
        TrainingDayEntity::class,
        TrainingDaySectionEntity::class,
        TrainingPlanExerciseEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class SportLifeDatabase : RoomDatabase() {
    abstract fun sportTypeDao(): SportTypeDao
    abstract fun workoutCheckInDao(): WorkoutCheckInDao
    abstract fun strengthDao(): StrengthDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingPlanDao(): TrainingPlanDao
}
