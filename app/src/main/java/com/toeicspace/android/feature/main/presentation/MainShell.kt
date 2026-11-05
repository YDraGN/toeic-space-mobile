package com.toeicspace.android.feature.main.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.BottomNavBar
import com.toeicspace.android.core.ui.components.BottomNavTab
import com.toeicspace.android.core.ui.components.ComingSoonState
import com.toeicspace.android.core.ui.components.NavigationMenuButton
import com.toeicspace.android.core.ui.components.SignInRequiredState
import com.toeicspace.android.core.ui.components.ToeicNavigationRail
import com.toeicspace.android.core.ui.components.oceanBackground
import com.toeicspace.android.core.ui.components.rememberComingSoonAction
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.BarSurface
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

/** Content stays readable on wide screens; past this it is centered with sea on both sides. */
private val ContentMaxWidth = 840.dp
private const val RAIL_SCRIM_ALPHA = 0.32f

@Composable
internal fun MainRoute(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
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
                onAccountInfoClick = onNavigateToAccountInfo,
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
    onAccountInfoClick: () -> Unit,
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
    val onComingSoon = rememberComingSoonAction(snackbarHostState)

    val windowWidth = currentWindowWidth()
    val useRail = windowWidth.isAtLeastMedium
    // On medium widths the expanded rail floats over the content; on expanded widths it sits beside it.
    val railOverlays = windowWidth == WindowWidth.MEDIUM
    var railExpanded by rememberSaveable { mutableStateOf(false) }
    val showRailOverlay = useRail && railExpanded && railOverlays
    BackHandler(enabled = showRailOverlay) { railExpanded = false }

    val rail: @Composable (expanded: Boolean, floating: Boolean) -> Unit = { expanded, floating ->
        ToeicNavigationRail(
            selectedTab = currentTab,
            onTabSelected = { tab ->
                if (railOverlays) railExpanded = false
                onTabSelected(tab)
            },
            expanded = expanded,
            containerColor = if (floating) MaterialTheme.colorScheme.surface else BarSurface,
            account = { isExpanded ->
                RailAccount(
                    viewer = viewer,
                    isSelected = currentTab == BottomNavTab.PROFILE,
                    expanded = isExpanded,
                    onClick = {
                        if (railOverlays) railExpanded = false
                        onTabSelected(BottomNavTab.PROFILE)
                    },
                )
            },
        )
    }

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
                navigationMenu =
                    if (useRail) {
                        {
                            NavigationMenuButton(expanded = railExpanded, onClick = {
                                railExpanded =
                                    !railExpanded
                            })
                        }
                    } else {
                        null
                    },
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) rail(railExpanded && !railOverlays, false)
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    MainTabsNavHost(
                        navController = navController,
                        startTab = startTab,
                        viewer = viewer,
                        onTabSelected = onTabSelected,
                        onSignUpClick = onSignUpClick,
                        onSignInClick = onSignInClick,
                        onAccountInfoClick = onAccountInfoClick,
                        onSignOutClick = onSignOutClick,
                        onComingSoon = onComingSoon,
                        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxSize(),
                    )
                }
            }
            if (showRailOverlay) {
                // Tapping outside, like Back, folds the rail again.
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.scrim.copy(alpha = RAIL_SCRIM_ALPHA),
                            ).clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = { railExpanded = false },
                            ),
                )
                rail(true, true)
            }
        }
    }
}

/** One destination per tab inside the nested [MainGraph]. */
@Composable
private fun MainTabsNavHost(
    navController: NavHostController,
    startTab: BottomNavTab,
    viewer: Viewer,
    onTabSelected: (BottomNavTab) -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startTab.route,
        route = MainGraph::class,
        modifier = modifier,
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
            ExploreTab(viewer = viewer, onSignUpClick = onSignUpClick, onComingSoon = onComingSoon)
        }
        composable<PracticeTabRoute> {
            MemberOnlyTab(
                viewer = viewer,
                titleRes = R.string.common_label_tab_practice,
                guestTitleRes = R.string.main_title_practice_locked,
                guestBodyRes = R.string.main_body_practice_locked,
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
            )
        }
        composable<VocabularyTabRoute> {
            MemberOnlyTab(
                viewer = viewer,
                titleRes = R.string.common_label_tab_vocabulary,
                guestTitleRes = R.string.main_title_vocabulary_locked,
                guestBodyRes = R.string.main_body_vocabulary_locked,
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
            )
        }
        composable<ProfileTabRoute> {
            ProfileTab(
                viewer = viewer,
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
                onAccountInfoClick = onAccountInfoClick,
                onComingSoon = onComingSoon,
                onSignOutClick = onSignOutClick,
            )
        }
    }
}

/** Practice and Vocabulary: a sign-in prompt for guests, "coming soon" until their features are built. */
@Composable
private fun MemberOnlyTab(
    viewer: Viewer,
    @StringRes titleRes: Int,
    @StringRes guestTitleRes: Int,
    @StringRes guestBodyRes: Int,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    when (viewer) {
        Viewer.Guest ->
            SignInRequiredState(
                title = stringResource(id = guestTitleRes),
                body = stringResource(id = guestBodyRes),
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
            )
        is Viewer.Member -> ComingSoonState(title = stringResource(id = titleRes))
    }
}
