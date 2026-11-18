package com.toeicspace.android.feature.course.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.MascotMessage
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTint
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.ExamSpeakingWritingContent
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PriceGreen
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CoursePromotion
import com.toeicspace.android.feature.course.domain.CourseSummary
import com.toeicspace.android.feature.course.presentation.components.PreviewCourses
import com.toeicspace.android.feature.course.presentation.components.amountText
import com.toeicspace.android.feature.enrollment.presentation.learningMessageRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** A single column reads best up to this width; wider windows center it. */
private val SinglePaneMaxWidth = 760.dp

/** The enroll button beside the price on a wide price card (tablets in portrait). */
private val WideEnrollWidth = 280.dp
private val SidePaneWidth = 360.dp
private val EnrollBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val PromotionDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
private const val PERCENT = 100

data class CourseDetailActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    val onModuleToggle: (moduleId: String) -> Unit,
    val onPrimaryClick: () -> Unit = {},
    val onLessonClick: (lessonId: String) -> Unit = {},
    val onConfirmEnroll: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onRelatedCourseClick: (courseId: String) -> Unit = {},
)

/**
 * [onOpenCheckout] opens the checkout of a paid course for a signed-in learner.
 * [onSignInRequired] opens sign in for a guest, who comes back here once signed in.
 */
@Composable
internal fun CourseDetailRoute(
    onBackClick: () -> Unit,
    onSignInRequired: () -> Unit,
    onOpenCheckout: (courseId: String) -> Unit,
    onOpenLesson: (courseId: String, lessonId: String, preview: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onCourseClick: (courseId: String) -> Unit = {},
    viewModel: CourseDetailViewModel = hiltViewModel(),
    relatedViewModel: RelatedCoursesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val relatedCourses by relatedViewModel.courses.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    val event = state.event
    LaunchedEffect(event) {
        when (event) {
            null -> return@LaunchedEffect
            CourseDetailEvent.SignInRequired -> onSignInRequired()
            is CourseDetailEvent.OpenCheckout -> onOpenCheckout(event.courseId)
            is CourseDetailEvent.OpenLesson ->
                onOpenLesson(event.courseId, event.lessonId, event.preview)
        }
        viewModel.onEventHandled()
    }

    val message = state.message
    val messageText = message?.let { stringResource(it.textRes()) }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            viewModel.onMessageShown()
            snackbarHostState.showSnackbar(messageText)
        }
    }

    CourseDetailScreen(
        state = state,
        actions =
            CourseDetailActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onModuleToggle = viewModel::onModuleToggle,
                onPrimaryClick = viewModel::onPrimaryClick,
                onLessonClick = viewModel::onLessonClick,
                onConfirmEnroll = viewModel::onConfirmEnroll,
                onDialogDismiss = viewModel::onDialogDismiss,
                onRelatedCourseClick = onCourseClick,
            ),
        relatedCourses = relatedCourses,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

private fun CourseDetailMessage.textRes(): Int =
    when (this) {
        CourseDetailMessage.Enrolled -> R.string.course_msg_enrolled
        CourseDetailMessage.NoLessons -> R.string.course_msg_no_lessons
        CourseDetailMessage.LessonLocked -> R.string.course_msg_lesson_locked
        is CourseDetailMessage.Failed -> error.learningMessageRes()
    }

/**
 * One course from the public catalog. Phones get one column with the price pinned at the bottom;
 * wide windows put the price card in a side column. The button under the price enrolls, buys or
 * opens the first lesson, depending on the price and on whether the learner owns the course.
 */
@Composable
internal fun CourseDetailScreen(
    state: CourseDetailUiState,
    actions: CourseDetailActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    relatedCourses: List<CourseSummary> = emptyList(),
) {
    val enroll =
        EnrollUi(
            action = state.primaryAction,
            isEnrolling = state.isEnrolling,
            onClick = actions.onPrimaryClick,
        )
    val windowWidth = currentWindowWidth()
    val twoPane = windowWidth == WindowWidth.EXPANDED
    // Only phones pin the price to the bottom; wider windows show it as a card, which reads better
    // than a bar stretched across a tablet.
    val pinnedEnrollBar = windowWidth == WindowWidth.COMPACT
    val course = state.course

    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = { DetailTopBar(onBackClick = actions.onBackClick) },
        bottomBar = {
            if (course != null && pinnedEnrollBar) {
                EnrollBar(price = course.summary.price, enroll = enroll)
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
            contentAlignment = Alignment.TopCenter,
        ) {
            val error = state.error
            when {
                course != null && twoPane ->
                    TwoPaneContent(
                        course = course,
                        state = state,
                        actions = actions,
                        enroll = enroll,
                        relatedCourses = relatedCourses,
                    )
                course != null ->
                    SinglePaneContent(
                        course = course,
                        state = state,
                        actions = actions,
                        enroll = enroll,
                        showPriceCard = !pinnedEnrollBar,
                        relatedCourses = relatedCourses,
                    )
                state.isLoading ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                error != null ->
                    DetailError(
                        error = error,
                        notFound = state.isNotFound,
                        actions = actions,
                    )
            }
        }
    }

    if (course != null) {
        when (state.dialog) {
            CourseDetailDialog.ConfirmEnroll ->
                EnrollConfirmDialog(
                    course = course,
                    isEnrolling = state.isEnrolling,
                    onConfirm = actions.onConfirmEnroll,
                    onDismiss = actions.onDialogDismiss,
                )
            CourseDetailDialog.SoldOut ->
                SoldOutDialog(course = course, onDismiss = actions.onDialogDismiss)
            null -> Unit
        }
    }
}

/** What the enroll button shows and does, shared by the pinned bar and the price card. */
private data class EnrollUi(
    val action: CoursePrimaryAction,
    val isEnrolling: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun DetailTopBar(onBackClick: () -> Unit) {
    AppTopBar(
        leadingContent = {
            ToeicIconButton(
                onClick = onBackClick,
                contentDescription = stringResource(id = R.string.common_action_back),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(R.string.course_title_detail),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = Spacing.smd),
            )
        },
    )
}

@Composable
private fun SinglePaneContent(
    course: CourseDetail,
    state: CourseDetailUiState,
    actions: CourseDetailActions,
    enroll: EnrollUi,
    showPriceCard: Boolean,
    relatedCourses: List<CourseSummary>,
) {
    LazyColumn(
        modifier = Modifier.widthIn(max = SinglePaneMaxWidth).fillMaxSize(),
        contentPadding =
            PaddingValues(
                horizontal = if (showPriceCard) Spacing.lg else Spacing.md,
                vertical = Spacing.sm,
            ),
        verticalArrangement = Arrangement.spacedBy(if (showPriceCard) Spacing.md else Spacing.smd),
    ) {
        item(key = "hero") { CourseHeroCard(course = course, large = showPriceCard) }
        if (showPriceCard) {
            item(
                key = "price",
            ) { PriceCard(course = course, enroll = enroll, wide = true) }
        }
        readingSections(course = course, includeSkills = true)
        curriculum(course = course, state = state, actions = actions)
        relatedCourses(
            courses = relatedCourses,
            wide = false,
            onCourseClick = actions.onRelatedCourseClick,
        )
    }
}

/** Tablets in landscape: the course on the left, a price column on the right. */
@Composable
private fun TwoPaneContent(
    course: CourseDetail,
    state: CourseDetailUiState,
    actions: CourseDetailActions,
    enroll: EnrollUi,
    relatedCourses: List<CourseSummary>,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(top = Spacing.sm, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item(key = "hero") { CourseHeroCard(course = course, large = true) }
            readingSections(course = course, includeSkills = false)
            curriculum(course = course, state = state, actions = actions)
            relatedCourses(
                courses = relatedCourses,
                wide = true,
                onCourseClick = actions.onRelatedCourseClick,
            )
        }
        Column(
            modifier =
                Modifier
                    .width(SidePaneWidth)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(top = Spacing.sm, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            PriceCard(course = course, enroll = enroll)
            SkillsSection(skills = course.skills, prerequisites = course.prerequisites)
        }
    }
}

private fun LazyListScope.readingSections(
    course: CourseDetail,
    includeSkills: Boolean,
) {
    course.description?.takeIf { it.isNotBlank() }?.let {
        item(key = "intro") { IntroSection(description = it) }
    }
    if (course.objectives.isNotEmpty()) {
        item(key = "objectives") { ObjectivesSection(objectives = course.objectives) }
    }
    if (includeSkills && (course.skills.isNotEmpty() || course.prerequisites.isNotEmpty())) {
        item(key = "skills") {
            SkillsSection(skills = course.skills, prerequisites = course.prerequisites)
        }
    }
}

private fun LazyListScope.curriculum(
    course: CourseDetail,
    state: CourseDetailUiState,
    actions: CourseDetailActions,
) {
    if (course.modules.isEmpty()) return
    item(key = "curriculum") {
        CurriculumSection(
            modules = course.modules,
            expandedModuleIds = state.expandedModuleIds,
            onModuleToggle = actions.onModuleToggle,
            onLessonClick = actions.onLessonClick,
            unlocked = state.isEnrolled,
        )
    }
}

/** Pinned under the content on phones: what it costs, and the way in. */
@Composable
private fun EnrollBar(
    price: CoursePrice,
    enroll: EnrollUi,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().softShadow(EnrollBarShape),
        shape = EnrollBarShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Row(
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PriceBlock(price = price, large = false, modifier = Modifier.weight(1f))
            EnrollButton(enroll = enroll)
        }
    }
}

/**
 * Price, enroll button and what the course includes. [wide] puts the button beside the price, for a
 * card that spans a tablet in portrait; otherwise they stack, as in the landscape side column.
 */
@Composable
private fun PriceCard(
    course: CourseDetail,
    enroll: EnrollUi,
    wide: Boolean = false,
) {
    ToeicCard(modifier = Modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        if (wide) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceBlock(
                    price = course.summary.price,
                    large = true,
                    modifier = Modifier.weight(1f),
                )
                Column(
                    modifier = Modifier.width(WideEnrollWidth),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    EnrollButton(enroll = enroll, modifier = Modifier.fillMaxWidth())
                    EnrollNote(enroll = enroll)
                }
            }
        } else {
            PriceBlock(price = course.summary.price, large = true)
            EnrollButton(enroll = enroll, modifier = Modifier.fillMaxWidth())
            EnrollNote(enroll = enroll)
        }
        HorizontalDivider(color = SubtleBorder)
        CourseIncludes(course = course)
    }
}

/** Under the button on cards: "you own this". */
@Composable
private fun EnrollNote(enroll: EnrollUi) {
    if (enroll.action != CoursePrimaryAction.START) return
    Text(
        text = stringResource(R.string.course_note_enrolled),
        style = MaterialTheme.typography.labelSmall,
        color = HintText,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun EnrollButton(
    enroll: EnrollUi,
    modifier: Modifier = Modifier,
) {
    val action = enroll.action
    // Sold out stays tappable, as an outline button, so the learner can read why in a dialog.
    if (action == CoursePrimaryAction.SOLD_OUT) {
        SecondaryButton(
            text = stringResource(R.string.course_label_sold_out),
            onClick = enroll.onClick,
            shape = PillShape,
            modifier = modifier.testTag(TestTags.COURSE_ENROLL),
        )
        return
    }
    PrimaryButton(
        text =
            stringResource(
                when (action) {
                    CoursePrimaryAction.CHECKING -> R.string.course_label_checking_enrollment
                    CoursePrimaryAction.ENROLL -> R.string.course_action_enroll
                    CoursePrimaryAction.BUY -> R.string.course_action_buy
                    CoursePrimaryAction.START -> R.string.course_action_start
                    CoursePrimaryAction.SOLD_OUT -> R.string.course_label_sold_out
                },
            ),
        onClick = enroll.onClick,
        isLoading = enroll.isEnrolling || action == CoursePrimaryAction.CHECKING,
        loadingText =
            if (enroll.isEnrolling) {
                stringResource(R.string.course_label_enrolling)
            } else {
                stringResource(R.string.course_label_checking_enrollment)
            },
        shape = PillShape,
        modifier = modifier.testTag(TestTags.COURSE_ENROLL),
    )
}

/**
 * "Học phí", the price the learner pays in large green, then, with a promotion, the struck-through
 * original price, the discount and the promotion's name and end date.
 */
@Composable
private fun PriceBlock(
    price: CoursePrice,
    large: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(
            text = stringResource(R.string.course_label_price),
            style = AppTextStyles.HelperText,
            color = HintText,
        )
        Text(
            text =
                if (price.isFree) {
                    stringResource(R.string.course_price_free)
                } else {
                    amountText(price.payable, price.currency)
                },
            style = if (large) AppTextStyles.PriceLarge else AppTextStyles.PriceBar,
            color = PriceGreen,
            maxLines = 1,
        )
        val promotion = price.promotion
        if (promotion != null && promotion.originalPrice > price.payable) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val original = amountText(promotion.originalPrice, price.currency)
                Text(
                    text = original,
                    style = AppTextStyles.HelperText,
                    color = HintText,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                )
                DiscountTag(promotion = promotion)
            }
            if (large) {
                Text(
                    text = promotion.label(),
                    style = AppTextStyles.HelperText,
                    color = ExamSpeakingWritingContent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DiscountTag(promotion: CoursePromotion) {
    val percent =
        (
            (promotion.originalPrice - promotion.sellingPrice) * PERCENT.toDouble() /
                promotion.originalPrice
        ).roundToInt()
    if (percent <= 0) return
    Text(
        text = stringResource(R.string.course_label_discount, percent),
        style = MaterialTheme.typography.labelSmall,
        color = ExamSpeakingWritingContent,
        modifier =
            Modifier
                .background(BlushTint, MaterialTheme.shapes.extraSmall)
                .padding(horizontal = Spacing.xs + Spacing.space2, vertical = Spacing.space1),
    )
}

@Composable
private fun CoursePromotion.label(): String {
    val until = expiresAt?.let { PromotionDateFormat.withZone(ZoneId.systemDefault()).format(it) }
    return if (until !=
        null
    ) {
        stringResource(R.string.course_label_promotion_until, name, until)
    } else {
        name
    }
}

@Composable
private fun DetailError(
    error: AppError,
    notFound: Boolean,
    actions: CourseDetailActions,
) {
    if (notFound) {
        MascotMessage(
            mascotResId = R.drawable.oy2_404,
            title = stringResource(R.string.course_title_not_found),
            body = stringResource(R.string.course_body_not_found),
        ) {
            SecondaryButton(
                text = stringResource(R.string.course_action_back_to_catalog),
                onClick = actions.onBackClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        MascotMessage(
            mascotResId = R.drawable.oy2_error,
            title = stringResource(R.string.course_title_error),
            body = stringResource(error.messageRes()),
        ) {
            SecondaryButton(
                text = stringResource(R.string.course_action_retry),
                onClick = actions.onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val PreviewActions =
    CourseDetailActions(onBackClick = {}, onRetry = {}, onModuleToggle = {})

private fun previewLesson(
    id: String,
    title: String,
    seconds: Int?,
    preview: Boolean = false,
) = CourseLesson(
    id = id,
    title = title,
    description = null,
    durationSeconds = seconds,
    isFreePreview = preview,
)

internal val PreviewCourseDetail =
    CourseDetail(
        summary =
            PreviewCourses.first().copy(
                price =
                    CoursePrice(
                        amount = 699_000,
                        currency = "VND",
                        promotion =
                            CoursePromotion(
                                name = "Ưu đãi tháng 11",
                                originalPrice = 699_000,
                                sellingPrice = 499_000,
                                expiresAt = Instant.parse("2026-11-30T16:59:59Z"),
                            ),
                    ),
                capacity = 40,
                remainingSeats = 18,
            ),
        description =
            "Lộ trình 8 tuần tập trung vào Part 2, 3, 4 và Part 5, 7. Mỗi bài có video ngắn, bài " +
                "luyện và mẹo xử lý bẫy thường gặp để bạn tăng điểm đều cả hai kỹ năng.",
        skills = listOf("Nghe hiểu", "Ngữ pháp", "Từ vựng công sở", "Đọc lướt"),
        objectives =
            listOf(
                "Nghe hiểu câu hỏi WH- và câu hỏi đuôi trong Part 2",
                "Đoán trước nội dung hội thoại Part 3–4 từ câu hỏi",
                "Nắm chắc 12 chủ điểm ngữ pháp hay gặp ở Part 5",
            ),
        prerequisites =
            listOf(
                "Đã có nền khoảng 400–450 điểm",
                "Nắm các thì cơ bản trong tiếng Anh",
            ),
        modules =
            listOf(
                CourseModule(
                    id = "m1",
                    title = "Chương 1 · Làm quen Part 1–2",
                    description = null,
                    lessons =
                        listOf(
                            previewLesson("l1", "Mô tả tranh: người và vật", 750, preview = true),
                            previewLesson("l2", "Câu hỏi WH- thường gặp", 1080, preview = true),
                            previewLesson("l3", "Câu hỏi đuôi và câu đề nghị", 1335),
                            previewLesson("l4", "Bẫy từ đồng âm, gần âm", 940),
                        ),
                ),
                CourseModule(
                    id = "m2",
                    title = "Chương 2 · Hội thoại Part 3–4",
                    description = null,
                    lessons = listOf(previewLesson("l5", "Đoán ý từ câu hỏi", 2400)),
                ),
            ),
    )

private val PreviewLoadedState =
    CourseDetailUiState(
        isLoading = false,
        course = PreviewCourseDetail,
        expandedModuleIds = setOf("m1"),
        isSignedIn = true,
        enrollment = EnrollmentStatus.NOT_ENROLLED,
    )

private val PreviewEnrolledState =
    PreviewLoadedState.copy(
        course =
            PreviewCourseDetail.copy(
                summary =
                    PreviewCourseDetail.summary.copy(
                        price = CoursePrice(amount = 0, currency = "VND"),
                    ),
            ),
        enrollment = EnrollmentStatus.ENROLLED,
    )

@Preview(widthDp = 390, heightDp = 1400)
@Composable
private fun CourseDetailPhonePreview() {
    ToeicSpaceTheme {
        CourseDetailScreen(
            state = PreviewLoadedState,
            actions = PreviewActions,
            relatedCourses = PreviewCourses.take(RELATED_COURSES_LIMIT),
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun CourseDetailTabletPreview() {
    ToeicSpaceTheme {
        CourseDetailScreen(
            state = PreviewLoadedState,
            actions = PreviewActions,
            relatedCourses = PreviewCourses.take(RELATED_COURSES_LIMIT),
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun CourseDetailLoadingPreview() {
    ToeicSpaceTheme { CourseDetailScreen(state = CourseDetailUiState(), actions = PreviewActions) }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun CourseDetailNotFoundPreview() {
    ToeicSpaceTheme {
        CourseDetailScreen(
            state =
                CourseDetailUiState(
                    isLoading = false,
                    error = AppError.Server(code = null, status = 404),
                ),
            actions = PreviewActions,
        )
    }
}

@Preview(widthDp = 390, heightDp = 1400)
@Composable
private fun CourseDetailEnrolledPhonePreview() {
    ToeicSpaceTheme { CourseDetailScreen(state = PreviewEnrolledState, actions = PreviewActions) }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun CourseDetailEnrolledTabletPreview() {
    ToeicSpaceTheme { CourseDetailScreen(state = PreviewEnrolledState, actions = PreviewActions) }
}
