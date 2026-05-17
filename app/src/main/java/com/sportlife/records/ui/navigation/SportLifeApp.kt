package com.sportlife.records.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sportlife.records.AppContainer
import com.sportlife.records.ui.component.EvolveBottomBar
import com.sportlife.records.ui.screen.backup.DataMigrationScreen
import com.sportlife.records.ui.screen.backup.DataMigrationViewModel
import com.sportlife.records.ui.screen.history.HistoryScreen
import com.sportlife.records.ui.screen.history.HistoryViewModel
import com.sportlife.records.ui.screen.home.HomeScreen
import com.sportlife.records.ui.screen.home.HomeViewModel
import com.sportlife.records.ui.screen.plan.EditTrainingPlanScreen
import com.sportlife.records.ui.screen.plan.EditTrainingPlanViewModel
import com.sportlife.records.ui.screen.plan.TrainingPlanScreen
import com.sportlife.records.ui.screen.plan.TrainingPlanViewModel
import com.sportlife.records.ui.screen.running.RunningCheckInScreen
import com.sportlife.records.ui.screen.running.RunningCheckInViewModel
import com.sportlife.records.ui.screen.stats.StatsScreen
import com.sportlife.records.ui.screen.stats.StatsViewModel
import com.sportlife.records.ui.screen.strength.StrengthCheckInScreen
import com.sportlife.records.ui.screen.strength.StrengthCheckInViewModel

private enum class Route(val path: String) {
    Home("home"),
    Running("running"),
    Strength("strength"),
    Plan("plan"),
    EditPlan("edit_plan"),
    History("history"),
    Stats("stats"),
    DataMigration("data_migration"),
}

@Composable
fun SportLifeApp(
    appContainer: AppContainer,
) {
    val navController = rememberNavController()
    val factory = remember(appContainer) { SportLifeViewModelFactory(appContainer) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Route.Home.path
    val selectedTab = when (currentRoute) {
        Route.Home.path -> "home"
        Route.Plan.path, Route.EditPlan.path -> "train"
        Route.Running.path, Route.Strength.path -> "workout"
        Route.History.path -> "history"
        Route.Stats.path, Route.DataMigration.path -> "stats"
        else -> "home"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            EvolveBottomBar(
                selected = selectedTab,
                onHome = { navController.navigateSingleTop(Route.Home.path) },
                onTrain = { navController.navigateSingleTop(Route.Plan.path) },
                onWorkout = { navController.navigateSingleTop(Route.Strength.path) },
                onHistory = { navController.navigateSingleTop(Route.History.path) },
                onStats = { navController.navigateSingleTop(Route.Stats.path) },
            )
        },
    ) { appPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.path,
            modifier = Modifier.padding(appPadding),
            enterTransition = {
                fadeIn(tween(180)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(240),
                )
            },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = {
                fadeIn(tween(180)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(240),
                )
            },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
        composable(Route.Home.path) {
            val viewModel: HomeViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            HomeScreen(
                uiState = uiState,
                onRunningClick = { navController.navigate(Route.Running.path) },
                onStrengthClick = { navController.navigate(Route.Strength.path) },
                onPlanClick = { navController.navigate(Route.Plan.path) },
                onHistoryClick = { navController.navigate(Route.History.path) },
                onStatsClick = { navController.navigate(Route.Stats.path) },
                onDataMigrationClick = { navController.navigate(Route.DataMigration.path) },
                onEditSloganClick = viewModel::startEditSlogan,
                onSloganDraftChange = viewModel::updateSloganDraft,
                onSaveSlogan = viewModel::saveSlogan,
                onCancelSlogan = viewModel::cancelEditSlogan,
            )
        }
        composable(Route.Running.path) {
            val viewModel: RunningCheckInViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            RunningCheckInScreen(
                uiState = uiState,
                onDistanceChange = viewModel::updateDistance,
                onPaceChange = viewModel::updatePace,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSave = { viewModel.save { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.Strength.path) {
            val viewModel: StrengthCheckInViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            StrengthCheckInScreen(
                uiState = uiState,
                onDateChange = viewModel::updateDate,
                onSplitChange = viewModel::updateSelectedSplit,
                onNoteChange = viewModel::updateNote,
                onSave = { viewModel.save { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.Plan.path) {
            val viewModel: TrainingPlanViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            TrainingPlanScreen(
                uiState = uiState,
                onSplitSelected = viewModel::activateSplit,
                onEditClick = { navController.navigate(Route.EditPlan.path) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.EditPlan.path) {
            val viewModel: EditTrainingPlanViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            EditTrainingPlanScreen(
                uiState = uiState,
                onStartAddDay = viewModel::startAddDay,
                onStartEditDay = viewModel::startEditDay,
                onDeleteDay = viewModel::deleteDay,
                onDayNameChange = viewModel::updateDayName,
                onDayFocusChange = viewModel::updateDayFocus,
                onSaveDay = viewModel::saveDayForm,
                onClearDayForm = viewModel::clearDayForm,
                onStartAdd = viewModel::startAdd,
                onStartEdit = viewModel::startEdit,
                onDeleteExercise = viewModel::deleteExercise,
                onStartAddSection = viewModel::startAddSection,
                onDeleteSection = viewModel::deleteSection,
                onSectionNameChange = viewModel::updateSectionName,
                onSectionBodyPartChange = viewModel::updateSectionBodyPart,
                onSaveSection = viewModel::saveSectionForm,
                onClearSectionForm = viewModel::clearSectionForm,
                onNameChange = viewModel::updateName,
                onBodyPartChange = viewModel::updateBodyPart,
                onSectionChange = viewModel::updateSection,
                onSetsChange = viewModel::updateSets,
                onWeightChange = viewModel::updateWeight,
                onRepsChange = viewModel::updateReps,
                onNoteChange = viewModel::updateNote,
                onSaveForm = viewModel::saveForm,
                onClearForm = viewModel::clearForm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.History.path) {
            val viewModel: HistoryViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            HistoryScreen(
                uiState = uiState,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onDateSelected = viewModel::selectDate,
                onEdit = viewModel::startEdit,
                onDelete = viewModel::delete,
                onEditDateChange = viewModel::updateEditDate,
                onEditNoteChange = viewModel::updateEditNote,
                onEditDistanceChange = viewModel::updateEditDistance,
                onEditPaceChange = viewModel::updateEditPace,
                onEditBodyPartChange = viewModel::updateEditBodyPart,
                onSaveEdit = viewModel::saveEdit,
                onCancelEdit = viewModel::clearEdit,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.Stats.path) {
            val viewModel: StatsViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            StatsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.DataMigration.path) {
            val viewModel: DataMigrationViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            DataMigrationScreen(
                uiState = uiState,
                onExportRequested = viewModel::exportBackup,
                onImportText = viewModel::importBackup,
                onExportSaved = viewModel::notifyExportSaved,
                onExportCancelled = viewModel::notifyExportCancelled,
                onBack = { navController.popBackStack() },
            )
        }
    }
    }
}

private fun NavController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Route.Home.path) {
            saveState = true
        }
    }
}
