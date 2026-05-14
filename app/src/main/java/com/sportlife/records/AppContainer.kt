package com.sportlife.records

import android.content.Context
import androidx.room.Room
import com.sportlife.records.data.backup.DataBackupRepository
import com.sportlife.records.data.local.DefaultDataSeeder
import com.sportlife.records.data.local.MIGRATION_1_2
import com.sportlife.records.data.local.SportLifeDatabase
import com.sportlife.records.data.repository.OfflineTrainingPlanRepository
import com.sportlife.records.data.repository.OfflineWorkoutRepository
import com.sportlife.records.data.repository.TrainingPlanRepository
import com.sportlife.records.data.repository.UserPreferencesRepository
import com.sportlife.records.data.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: SportLifeDatabase = Room.databaseBuilder(
        context.applicationContext,
        SportLifeDatabase::class.java,
        "sport_life.db",
    ).addMigrations(MIGRATION_1_2).build()

    val workoutRepository: WorkoutRepository = OfflineWorkoutRepository(database)
    val trainingPlanRepository: TrainingPlanRepository = OfflineTrainingPlanRepository(database)
    val userPreferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context.applicationContext)
    val dataBackupRepository: DataBackupRepository = DataBackupRepository(database, userPreferencesRepository)

    init {
        applicationScope.launch {
            DefaultDataSeeder(database).seedIfNeeded()
        }
    }
}
