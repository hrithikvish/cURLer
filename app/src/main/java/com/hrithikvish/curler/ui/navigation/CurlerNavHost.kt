package com.hrithikvish.curler.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.hrithikvish.curler.ui.screens.home.HomeScreen
import com.hrithikvish.curler.ui.screens.newrequest.NewRequestScreen
import com.hrithikvish.curler.ui.screens.newrequest.RequestFlowViewModel
import com.hrithikvish.curler.ui.screens.response.ResponseScreen
import com.hrithikvish.curler.ui.screens.review.HistoryReviewViewModel
import com.hrithikvish.curler.ui.screens.review.ReviewScreen

@Composable
fun CurlerNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = CurlerRoute.Home,
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth / 3 } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth / 3 } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } },
    ) {
        composable<CurlerRoute.Home> {
            HomeScreen(
                onAddRequest = { navController.navigate(CurlerRoute.RequestFlowGraph) },
                onHistoryRowClick = { id -> navController.navigate(CurlerRoute.HistoryFlowGraph(id)) },
            )
        }

        navigation<CurlerRoute.RequestFlowGraph>(startDestination = CurlerRoute.NewRequest) {
            composable<CurlerRoute.NewRequest> { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry<CurlerRoute.RequestFlowGraph>() }
                val viewModel: RequestFlowViewModel = hiltViewModel(parentEntry)
                NewRequestScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToReview = { navController.navigate(CurlerRoute.RequestReview) },
                )
            }
            composable<CurlerRoute.RequestReview> { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry<CurlerRoute.RequestFlowGraph>() }
                val viewModel: RequestFlowViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ReviewScreen(
                    reviewCapable = viewModel,
                    isSending = uiState.isSending,
                    onBack = { navController.popBackStack() },
                    onSendComplete = { navController.navigate(CurlerRoute.RequestResponse) },
                )
            }
            composable<CurlerRoute.RequestResponse> { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry<CurlerRoute.RequestFlowGraph>() }
                val viewModel: RequestFlowViewModel = hiltViewModel(parentEntry)
                val request by viewModel.request.collectAsStateWithLifecycle()
                val response by viewModel.response.collectAsStateWithLifecycle()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ResponseScreen(
                    request = request,
                    response = response,
                    errorMessage = uiState.sendError,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        navigation<CurlerRoute.HistoryFlowGraph>(startDestination = CurlerRoute.HistoryReview) {
            composable<CurlerRoute.HistoryReview> { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry<CurlerRoute.HistoryFlowGraph>() }
                val viewModel: HistoryReviewViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(uiState.isLoading) {
                    if (!uiState.isLoading && !viewModel.didAutoOpenSavedResponse) {
                        viewModel.didAutoOpenSavedResponse = true
                        navController.navigate(CurlerRoute.HistoryResponse)
                    }
                }
                ReviewScreen(
                    reviewCapable = viewModel,
                    isSending = uiState.isSending,
                    onBack = { navController.popBackStack() },
                    onSendComplete = { navController.navigate(CurlerRoute.HistoryResponse) },
                )
            }
            composable<CurlerRoute.HistoryResponse> { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry<CurlerRoute.HistoryFlowGraph>() }
                val viewModel: HistoryReviewViewModel = hiltViewModel(parentEntry)
                val request by viewModel.request.collectAsStateWithLifecycle()
                val response by viewModel.response.collectAsStateWithLifecycle()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ResponseScreen(
                    request = request,
                    response = response,
                    errorMessage = uiState.sendError,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
