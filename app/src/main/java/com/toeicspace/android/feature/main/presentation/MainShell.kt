package com.toeicspace.android.feature.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.BottomNavBar
import com.toeicspace.android.core.ui.components.BottomNavTab
import com.toeicspace.android.core.ui.components.ComingSoonState
import com.toeicspace.android.core.ui.components.SignInRequiredState
import com.toeicspace.android.core.ui.components.ToeicNavigationRail
import com.toeicspace.android.core.ui.components.oceanBackground
import com.toeicspace.android.feature.explore.presentation.ExploreTab
import com.toeicspace.android.feature.home.presentation.HomeTab
import com.toeicspace.android.feature.main.navigation.ExploreTabRoute
import com.toeicspace.android.feature.main.navigation.HomeTabRoute
import com.toeicspace.android.feature.main.navigation.MainGraph
import com.toeicspace.android.feature.main.navigation.PracticeTabRoute
import com.toeicspace.android.feature.main.navigation.ProfileTabRoute
import com.toeicspace.android.feature.main.navigation.VocabularyTabRoute
import com.toeicspace.android.feature.main.navigation.navigateToTab
import com.toeicspace.android.feature.main.navigation.route
import com.toeicspace.android.feature.main.navigation.startTab
import com.toeicspace.android.feature.main.navigation.toTab
import com.toeicspace.android.feature.profile.presentation.ProfileTab
import kotlinx.coroutines.launch

/** Windows at least this wide use a navigation rail instead of the bottom bar. */
private val RailMinWidth = 600.dp
private val ContentMaxWidth = 600.dp

@Composable
internal fun MainRoute(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState == MainUiState.SessionEnded) onSessionEnded()
    }

    when (val state = uiState) {
        is MainUiState.Ready ->
            MainShell(
                state = state,
                onSignInClick = onNavigateToSignIn,
                onSignUpClick = onNavigateToSignUp,
                onSignOutClick = viewModel::onSignOutClick,
                modifier = modifier,
            )
        // The session is read from local storage, so this lasts a frame or two.
        else -> Box(modifier = modifier.fillMaxSize().oceanBackground())
    }
}

/**
 * Top bar and bottom bar stay put while the tabs swap underneath them. Each tab keeps its own state
 * through [navigateToTab].
 */
@Composable
internal fun MainShell(
    state: MainUiState.Ready,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val viewer = state.viewer
    // Fixed for the life of the shell: signing in or out always rebuilds it from the root graph.
    val startTab = remember { viewer.startTab }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.destination.toTab() ?: startTab
    val onTabSelected: (BottomNavTab) -> Unit = { tab ->
        if (tab != currentTab) navController.navigateToTab(tab)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(id = R.string.common_msg_coming_soon)
    val onComingSoon: () -> Unit = {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(comingSoonMessage)
        }
    }

    val windowWidth =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
        }
    val useRail = windowWidth >= RailMinWidth

    Scaffold(
        modifier = modifier.oceanBackground(),
        containerColor = Color.Transparent,
        topBar = {
            MainTopBar(
                viewer = viewer,
                timeOfDay = state.timeOfDay,
                onAvatarClick = { onTabSelected(BottomNavTab.PROFILE) },
                onSearchClick = onComingSoon,
                onNotificationsClick = onComingSoon,
                onSignInClick = onSignInClick,
            )
        },
        bottomBar = {
            if (!useRail) {
                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            if (useRail) {
                ToeicNavigationRail(selectedTab = currentTab, onTabSelected = onTabSelected)
            }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter,
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startTab.route,
                    route = MainGraph::class,
                    modifier =
                        Modifier
                            .widthIn(max = ContentMaxWidth)
                            .fillMaxSize(),
                ) {
                    composable<HomeTabRoute> {
                        HomeTab(
                            viewer = viewer,
                            onSignUpClick = onSignUpClick,
                            onSignInClick = onSignInClick,
                            onExploreClick = { onTabSelected(BottomNavTab.EXPLORE) },
                        )
                    }
                    composable<ExploreTabRoute> {
                        ExploreTab(
                            viewer = viewer,
                            onSignUpClick = onSignUpClick,
                            onComingSoon = onComingSoon,
                        )
                    }
                    composable<PracticeTabRoute> {
                        MemberOnlyTab(
                            viewer = viewer,
                            title = stringResource(id = R.string.common_label_tab_practice),
                            guestTitle =
                                stringResource(
                                    id = R.string.main_title_practice_locked,
                                ),
                            guestBody =
                                stringResource(
                                    id = R.string.main_body_practice_locked,
                                ),
                            onSignUpClick = onSignUpClick,
                            onSignInClick = onSignInClick,
                        )
                    }
                    composable<VocabularyTabRoute> {
                        MemberOnlyTab(
                            viewer = viewer,
                            title = stringResource(id = R.string.common_label_tab_vocabulary),
                            guestTitle =
                                stringResource(
                                    id = R.string.main_title_vocabulary_locked,
                                ),
                            guestBody =
                                stringResource(
                                    id = R.string.main_body_vocabulary_locked,
                                ),
                            onSignUpClick = onSignUpClick,
                            onSignInClick = onSignInClick,
                        )
                    }
                    composable<ProfileTabRoute> {
                        ProfileTab(
                            viewer = viewer,
                            onSignUpClick = onSignUpClick,
                            onSignInClick = onSignInClick,
                            onChangePasswordClick = onComingSoon,
                            onSignOutClick = onSignOutClick,
                        )
                    }
                }
            }
        }
    }
}

/** Practice and Vocabulary: a sign-in prompt for guests, "coming soon" until their features are built. */
@Composable
private fun MemberOnlyTab(
    viewer: Viewer,
    title: String,
    guestTitle: String,
    guestBody: String,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    when (viewer) {
        Viewer.Guest ->
            SignInRequiredState(
                title = guestTitle,
                body = guestBody,
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
            )
        is Viewer.Member -> ComingSoonState(title = title)
    }
}
