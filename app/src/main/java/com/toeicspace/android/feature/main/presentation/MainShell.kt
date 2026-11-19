package com.toeicspace.android.feature.main.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.toeicspace.android.core.analytics.TrackScreenViews
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
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.presentation.home.VocabularyTab
import com.toeicspace.android.feature.vocabulary.presentation.review.ReviewDueCard

/** Content stays readable on wide screens; past this it is centered with sea on both sides. */
private val ContentMaxWidth = 840.dp
private const val RAIL_SCRIM_ALPHA = 0.32f

@Composable
internal fun MainRoute(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToCourseDetail: (courseId: String) -> Unit,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToMyCourses: () -> Unit = {},
    onNavigateToMyClasses: () -> Unit = {},
    onNavigateToVocabularySet: (setId: String) -> Unit = {},
    onNavigateToReview: (source: ReviewSource) -> Unit = {},
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
                onChangePasswordClick = onNavigateToChangePassword,
                onCourseClick = onNavigateToCourseDetail,
                onMyCoursesClick = onNavigateToMyCourses,
                onMyClassesClick = onNavigateToMyClasses,
                onVocabularySetClick = onNavigateToVocabularySet,
                onStartReview = onNavigateToReview,
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
    onChangePasswordClick: () -> Unit,
    onCourseClick: (courseId: String) -> Unit,
    modifier: Modifier = Modifier,
    onMyCoursesClick: () -> Unit = {},
    onMyClassesClick: () -> Unit = {},
    onVocabularySetClick: (setId: String) -> Unit = {},
    onStartReview: (source: ReviewSource) -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    // The tabs have their own nav controller, so their screen views are logged here.
    TrackScreenViews(navController)
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
    var railExpanded by rememberSaveable { mutableStateOf(false) }
    // The expanded rail always opens over the content, with a scrim, like a drawer: the page keeps
    // its layout instead of being squeezed sideways.
    val showRailOverlay = useRail && railExpanded
    BackHandler(enabled = showRailOverlay) { railExpanded = false }

    val rail: @Composable (expanded: Boolean, floating: Boolean) -> Unit = { expanded, floating ->
        ToeicNavigationRail(
            selectedTab = currentTab,
            onTabSelected = { tab ->
                railExpanded = false
                onTabSelected(tab)
            },
            expanded = expanded,
            // Same tone as the top bar above it, so header and open panel read as one surface.
            containerColor = if (floating) MaterialTheme.colorScheme.background else BarSurface,
            account = { isExpanded ->
                RailAccount(
                    viewer = viewer,
                    isSelected = currentTab == BottomNavTab.PROFILE,
                    expanded = isExpanded,
                    onClick = {
                        railExpanded = false
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
        val layoutDirection = LocalLayoutDirection.current
        // Only the top bar pushes everything down. The rail runs on to the bottom edge, behind the
        // gesture bar (it pads its own items), so it reads as part of the screen, not a floating card.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) rail(false, false)
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(
                                start =
                                    if (useRail) {
                                        0.dp
                                    } else {
                                        innerPadding.calculateStartPadding(
                                            layoutDirection,
                                        )
                                    },
                                end = innerPadding.calculateEndPadding(layoutDirection),
                                bottom = innerPadding.calculateBottomPadding(),
                            ).consumeWindowInsets(innerPadding),
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
                        onChangePasswordClick = onChangePasswordClick,
                        onCourseClick = onCourseClick,
                        onMyCoursesClick = onMyCoursesClick,
                        onMyClassesClick = onMyClassesClick,
                        onVocabularySetClick = onVocabularySetClick,
                        onStartReview = onStartReview,
                        onSignOutClick = onSignOutClick,
                        onComingSoon = onComingSoon,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.fillMaxSize(),
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
    onChangePasswordClick: () -> Unit,
    onCourseClick: (courseId: String) -> Unit,
    onMyCoursesClick: () -> Unit,
    onMyClassesClick: () -> Unit,
    onVocabularySetClick: (setId: String) -> Unit,
    onStartReview: (source: ReviewSource) -> Unit,
    onSignOutClick: () -> Unit,
    onComingSoon: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startTab.route,
        route = MainGraph::class,
        modifier = modifier,
    ) {
        composable<HomeTabRoute> {
            ReadableWidth {
                HomeTab(
                    viewer = viewer,
                    onSignUpClick = onSignUpClick,
                    onSignInClick = onSignInClick,
                    onExploreClick = { onTabSelected(BottomNavTab.EXPLORE) },
                )
            }
        }
        composable<ExploreTabRoute> {
            ExploreTab(onCourseClick = onCourseClick)
        }
        composable<PracticeTabRoute> {
            ReadableWidth {
                MemberOnlyTab(
                    viewer = viewer,
                    titleRes = R.string.common_label_tab_practice,
                    guestTitleRes = R.string.main_title_practice_locked,
                    guestBodyRes = R.string.main_body_practice_locked,
                    onSignUpClick = onSignUpClick,
                    onSignInClick = onSignInClick,
                )
            }
        }
        // Guests can study too; the tab lays itself out on wide screens, like Explore.
        composable<VocabularyTabRoute> {
            VocabularyTab(
                onSetClick = onVocabularySetClick,
                header = { windowWidth ->
                    ReviewDueCard(
                        windowWidth = windowWidth,
                        onStartReview = onStartReview,
                        onSignInClick = onSignInClick,
                        onSignUpClick = onSignUpClick,
                    )
                },
            )
        }
        composable<ProfileTabRoute> {
            ReadableWidth {
                ProfileTab(
                    viewer = viewer,
                    onSignUpClick = onSignUpClick,
                    onSignInClick = onSignInClick,
                    onAccountInfoClick = onAccountInfoClick,
                    onChangePasswordClick = onChangePasswordClick,
                    onComingSoon = onComingSoon,
                    onSignOutClick = onSignOutClick,
                    snackbarHostState = snackbarHostState,
                    onMyCoursesClick = onMyCoursesClick,
                    onMyClassesClick = onMyClassesClick,
                )
            }
        }
    }
}

/** Home, practice and profile read best in a column; the course catalog and vocabulary use the full width. */
@Composable
private fun ReadableWidth(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxSize()) { content() }
    }
}

/** Practice: a sign-in prompt for guests, "coming soon" until its features are built. */
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
