package io.github.nwma_fywf.mineword.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.animation.ExperimentalAnimationApi
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.ui.screen.addword.AddWordScreen
import io.github.nwma_fywf.mineword.ui.screen.addword.AddWordViewModel
import io.github.nwma_fywf.mineword.ui.screen.editword.EditWordScreen
import io.github.nwma_fywf.mineword.ui.screen.editword.EditWordViewModel
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizHomeScreen
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizHomeViewModel
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizScreen
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel
import io.github.nwma_fywf.mineword.ui.screen.review.ReviewScreen
import io.github.nwma_fywf.mineword.ui.screen.review.ReviewViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.SettingsScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.SettingsViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.ThemeSettingScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.ThemeSettingViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.FontSettingScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.FontSettingViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.DataManagementScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.DataManagementViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.LearningSettingScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.LearningSettingViewModel
import io.github.nwma_fywf.mineword.ui.screen.stats.StatsScreen
import io.github.nwma_fywf.mineword.ui.screen.stats.StatsViewModel
import io.github.nwma_fywf.mineword.ui.screen.worddetail.WordDetailScreen
import io.github.nwma_fywf.mineword.ui.screen.worddetail.WordDetailViewModel
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListScreen
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListViewModel
import io.github.nwma_fywf.mineword.ui.screen.wronganswer.WrongAnswerScreen
import io.github.nwma_fywf.mineword.ui.screen.wronganswer.WrongAnswerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    repository: WordRepository,
    themePreferences: ThemePreferences
) {
    val animationDuration = 200
    val bottomNavRoutes = setOf(
        Screen.WordList.route,
        Screen.Review.route,
        Screen.QuizMode.route,
        Screen.Stats.route,
        Screen.Settings.route
    )

    fun AnimatedContentTransitionScope<NavBackStackEntry>.getSlideDirection(): AnimatedContentTransitionScope.SlideDirection {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        return if (initialRoute in bottomNavRoutes && targetRoute in bottomNavRoutes) {
            val bottomNavOrder = listOf(
                Screen.WordList.route,
                Screen.Review.route,
                Screen.QuizMode.route,
                Screen.Stats.route,
                Screen.Settings.route
            )
            val initialIndex = bottomNavOrder.indexOf(initialRoute)
            val targetIndex = bottomNavOrder.indexOf(targetRoute)

            if (targetIndex > initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
        } else if (targetRoute in bottomNavRoutes) {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }
    }

    val context = navController.context

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Screen.WordList.route,
        enterTransition = {
            val direction = getSlideDirection()
            slideIntoContainer(
                towards = direction,
                animationSpec = tween(animationDuration)
            )
        },
        exitTransition = {
            val direction = getSlideDirection()
            slideOutOfContainer(
                towards = direction,
                animationSpec = tween(animationDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationDuration)
            )
        },
    ) {
        composable(Screen.WordList.route) {
            val wordListViewModel: WordListViewModel = viewModel(
                factory = WordListViewModel.provideFactory(repository)
            )
            WordListScreen(
                viewModel = wordListViewModel,
                onNavigateToAddWord = { navController.navigate(Screen.AddWord.route) },
                onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) },
            )
        }
        composable(Screen.AddWord.route) {
            val viewModel: AddWordViewModel = viewModel(
                factory = AddWordViewModel.provideFactory(repository)
            )
            AddWordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.WordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            val viewModel: WordDetailViewModel = viewModel(
                factory = WordDetailViewModel.provideFactory(repository)
            )
            WordDetailScreen(
                viewModel = viewModel,
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditWord.createRoute(id)) },
            )
        }
        composable(
            route = Screen.EditWord.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            val viewModel: EditWordViewModel = viewModel(
                factory = EditWordViewModel.provideFactory(repository)
            )
            EditWordScreen(
                viewModel = viewModel,
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.QuizMode.route) {
            val viewModel: QuizHomeViewModel = viewModel(
                factory = QuizHomeViewModel.provideFactory(repository)
            )
            QuizHomeScreen(
                viewModel = viewModel,
                onSelectMode = { mode, count, tag -> navController.navigate(Screen.QuizPlay.createRoute(mode.name, count, tag)) },
                onNavigateToWrongAnswer = { navController.navigate(Screen.WrongAnswer.route) }
            )
        }
        composable(
            route = Screen.QuizPlay.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("count") { type = NavType.IntType },
                navArgument("tag") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode") ?: return@composable
            val count = backStackEntry.arguments?.getInt("count") ?: -1
            val tag = backStackEntry.arguments?.getString("tag")?.takeIf { it != "all" }
            val viewModel: QuizViewModel = viewModel(
                factory = QuizViewModel.provideFactory(repository)
            )
            val mode = try {
                QuizViewModel.QuizMode.valueOf(modeString)
            } catch (e: Exception) {
                QuizViewModel.QuizMode.EN_TO_CN
            }
            if (mode == QuizViewModel.QuizMode.REVIEW) {
                viewModel.loadReviewWords()
            } else if (mode == QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS) {
                viewModel.loadWrongAnswerWords()
            } else {
                viewModel.setModeAndStart(mode, count, tag)
            }
            QuizScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Review.route) {
            val viewModel: ReviewViewModel = viewModel(
                factory = ReviewViewModel.provideFactory(repository)
            )
            ReviewScreen(
                viewModel = viewModel,
                onStartReview = { mode -> navController.navigate(Screen.QuizPlay.createRoute(mode.name, -1, null)) }
            )
        }
        composable(Screen.WrongAnswer.route) {
            val viewModel: WrongAnswerViewModel = viewModel(
                factory = WrongAnswerViewModel.provideFactory(repository)
            )
            WrongAnswerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) },
                onNavigateToQuiz = { navController.navigate(Screen.QuizPlay.createRoute(QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS.name, -1, null)) }
            )
        }
        composable(Screen.Stats.route) {
            val viewModel: StatsViewModel = viewModel(
                factory = StatsViewModel.provideFactory(repository)
            )
            StatsScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(
                    themePreferences,
                    repository,
                    context
                )
            )
            SettingsScreen(
                viewModel = settingsVm,
                onNavigateToThemeSetting = { navController.navigate(Screen.ThemeSetting.route) },
                onNavigateToFontSetting = { navController.navigate(Screen.FontSetting.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToDataManagement = { navController.navigate(Screen.DataManagement.route) },
                onNavigateToLearningSetting = { navController.navigate(Screen.LearningSetting.route) }
            )
        }
        composable(Screen.ThemeSetting.route) {
            val viewModel: ThemeSettingViewModel = viewModel(
                factory = ThemeSettingViewModel.provideFactory(themePreferences)
            )
            ThemeSettingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FontSetting.route) {
            val viewModel: FontSettingViewModel = viewModel(
                factory = FontSettingViewModel.provideFactory(themePreferences, context)
            )
            FontSettingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DataManagement.route) {
            val viewModel: DataManagementViewModel = viewModel(
                factory = DataManagementViewModel.provideFactory(themePreferences, repository, context)
            )
            DataManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LearningSetting.route) {
            val viewModel: LearningSettingViewModel = viewModel(
                factory = LearningSettingViewModel.provideFactory(themePreferences, context)
            )
            LearningSettingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
