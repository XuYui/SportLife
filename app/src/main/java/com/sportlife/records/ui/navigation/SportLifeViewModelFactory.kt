package com.sportlife.records.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sportlife.records.AppContainer
import com.sportlife.records.ui.screen.history.HistoryViewModel
import com.sportlife.records.ui.screen.home.HomeViewModel
import com.sportlife.records.ui.screen.backup.DataMigrationViewModel
import com.sportlife.records.ui.screen.plan.EditTrainingPlanViewModel
import com.sportlife.records.ui.screen.plan.TrainingPlanViewModel
import com.sportlife.records.ui.screen.running.RunningCheckInViewModel
import com.sportlife.records.ui.screen.stats.StatsViewModel
import com.sportlife.records.ui.screen.strength.StrengthCheckInViewModel

class SportLifeViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(appContainer.workoutRepository, appContainer.userPreferencesRepository)
            modelClass.isAssignableFrom(RunningCheckInViewModel::class.java) ->
                RunningCheckInViewModel(appContainer.workoutRepository)
            modelClass.isAssignableFrom(StrengthCheckInViewModel::class.java) ->
                StrengthCheckInViewModel(appContainer.workoutRepository, appContainer.trainingPlanRepository)
            modelClass.isAssignableFrom(TrainingPlanViewModel::class.java) ->
                TrainingPlanViewModel(appContainer.trainingPlanRepository)
            modelClass.isAssignableFrom(EditTrainingPlanViewModel::class.java) ->
                EditTrainingPlanViewModel(appContainer.trainingPlanRepository)
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(appContainer.workoutRepository)
            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(appContainer.workoutRepository, appContainer.trainingPlanRepository)
            modelClass.isAssignableFrom(DataMigrationViewModel::class.java) ->
                DataMigrationViewModel(appContainer.dataBackupRepository)
            else -> error("Unknown ViewModel: ${modelClass.name}")
        } as T
    }
}
