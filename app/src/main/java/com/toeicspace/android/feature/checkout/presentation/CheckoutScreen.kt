package com.toeicspace.android.feature.checkout.presentation

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.ErrorState
import com.toeicspace.android.core.ui.components.LoadingState
import com.toeicspace.android.core.ui.components.MascotMessage
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BrandDestructive
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.course.presentation.components.amountText
import kotlinx.coroutines.launch

/** One readable column on phones and small tablets; the payment step widens on large screens. */
private val ReviewMaxWidth = 640.dp
private val PaymentWideMaxWidth = 1040.dp

data class CheckoutActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    val onCouponInputChange: (String) -> Unit,
    val onApplyCoupon: () -> Unit,
    val onRemoveCoupon: () -> Unit,
    val onCreateOrder: () -> Unit,
    val onSaveQr: () -> Unit,
    val onShareQr: () -> Unit,
    val onCopied: (messageRes: Int) -> Unit,
    val onCancelClick: () -> Unit,
    val onDismissCancel: () -> Unit,
    val onConfirmCancel: () -> Unit,
    val onStartNewOrder: () -> Unit,
    val onStartLearning: () -> Unit,
)

@Composable
internal fun CheckoutRoute(
    onBackClick: () -> Unit,
    onPaid: (courseId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Poll only while the order can still change (waiting for the transfer, or paid but the course
    // not granted yet): repeatOnLifecycle stops it in the background (the learner is in the banking
    // app) and starts it again, at once, when they come back.
    val lifecycleOwner = LocalLifecycleOwner.current
    val shouldPoll = state.shouldPoll
    LaunchedEffect(shouldPoll, lifecycleOwner) {
        if (shouldPoll) {
            lifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) { viewModel.pollPendingOrder() }
        }
    }

    // Android 9 needs the storage permission to write into Pictures; 10+ goes through MediaStore.
    val storagePermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.onSaveQr() else viewModel.onStoragePermissionDenied()
        }
    val onSaveQr = {
        val granted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) ==
                PackageManager.PERMISSION_GRANTED
        if (viewModel.needsStoragePermission && !granted) {
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.onSaveQr()
        }
    }

    val chooserTitle = stringResource(R.string.checkout_share_chooser)
    LaunchedEffect(state.pendingShareUri) {
        val uri = state.pendingShareUri?.toUri() ?: return@LaunchedEffect
        val send =
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(null, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(send, chooserTitle))
        viewModel.onShareHandled()
    }

    CheckoutScreen(
        state = state,
        actions =
            CheckoutActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onCouponInputChange = viewModel::onCouponInputChange,
                onApplyCoupon = viewModel::onApplyCoupon,
                onRemoveCoupon = viewModel::onRemoveCoupon,
                onCreateOrder = viewModel::onCreateOrder,
                onSaveQr = onSaveQr,
                onShareQr = viewModel::onShareQr,
                onCopied = viewModel::onCopied,
                onCancelClick = viewModel::onCancelClick,
                onDismissCancel = viewModel::onDismissCancel,
                onConfirmCancel = viewModel::onConfirmCancel,
                onStartNewOrder = viewModel::onStartNewOrder,
                onStartLearning = { onPaid(viewModel.courseId) },
            ),
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

@Composable
internal fun CheckoutScreen(
    state: CheckoutUiState,
    actions: CheckoutActions,
    modifier: Modifier = Modifier,
    onMessageShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.messageRes?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val onCopy: (CopyRequest) -> Unit = { request ->
        scope.launch {
            clipboard.setClipEntry(
                ClipData.newPlainText(request.label, request.value).toClipEntry(),
            )
            actions.onCopied(request.messageRes)
        }
    }
    val wide = currentWindowWidth().isAtLeastMedium

    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = { CheckoutTopBar(onBackClick = actions.onBackClick) },
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
            when (val phase = state.phase) {
                CheckoutPhase.Loading ->
                    LoadingState(message = stringResource(R.string.checkout_label_loading))
                is CheckoutPhase.LoadFailed ->
                    ErrorState(
                        error = phase.error,
                        onRetry = actions.onRetry,
                        title = stringResource(R.string.checkout_title_load_failed),
                        body = stringResource(phase.messageRes),
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.checkout_action_back_to_course),
                            onClick = actions.onBackClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                is CheckoutPhase.Review ->
                    ScrollingColumn(maxWidth = ReviewMaxWidth) {
                        ReviewContent(preview = phase.preview, state = state, actions = actions)
                    }
                is CheckoutPhase.AwaitingTransfer -> {
                    val transfer = phase.order.transfer
                    if (transfer != null) {
                        ScrollingColumn(
                            maxWidth = if (wide) PaymentWideMaxWidth else ReviewMaxWidth,
                        ) {
                            PaymentContent(
                                order = phase.order,
                                transfer = transfer,
                                state = state,
                                actions = actions,
                                sideBySide = wide,
                                onCopy = onCopy,
                            )
                        }
                    }
                }
                is CheckoutPhase.Paid -> PaidContent(order = phase.order, actions = actions)
                is CheckoutPhase.Closed -> ClosedContent(order = phase.order, actions = actions)
            }
        }
    }

    if (state.showCancelDialog) CancelOrderDialog(actions = actions)
}

@Composable
private fun CheckoutTopBar(onBackClick: () -> Unit) {
    AppTopBar(
        leadingContent = {
            ToeicIconButton(
                onClick = onBackClick,
                contentDescription = stringResource(R.string.common_action_back),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(R.string.checkout_title),
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
private fun ScrollingColumn(
    maxWidth: Dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth().padding(bottom = Spacing.xl),
        ) {
            content()
        }
    }
}

/** Paid (or owned already). Full-discount orders land here straight from "create order". */
@Composable
private fun PaidContent(
    order: CheckoutOrder?,
    actions: CheckoutActions,
) {
    val body =
        when {
            order == null -> stringResource(R.string.checkout_body_owned)
            order.fulfillment == FulfillmentStatus.NEEDS_REVIEW ->
                stringResource(R.string.checkout_body_paid_review, order.orderCode)
            order.fulfillment == FulfillmentStatus.COMPLETED ->
                stringResource(
                    R.string.checkout_body_paid,
                )
            else -> stringResource(R.string.checkout_body_paid_activating)
        }
    MascotMessage(
        mascotResId = R.drawable.oy2_success,
        title =
            stringResource(
                if (order ==
                    null
                ) {
                    R.string.checkout_title_owned
                } else {
                    R.string.checkout_title_paid
                },
            ),
        body = body,
    ) {
        if (order != null && order.amounts.finalAmount > 0) {
            Text(
                text =
                    stringResource(
                        R.string.checkout_label_paid_amount,
                        amountText(
                            order.amounts.finalAmount,
                            order.amounts.currency,
                        ),
                    ),
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (order?.fulfillment == FulfillmentStatus.NEEDS_REVIEW) {
            // The course was not granted, so there is nothing to start yet.
            SecondaryButton(
                text = stringResource(R.string.checkout_action_back_to_course),
                onClick = actions.onBackClick,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Disabled while the course is still being granted; the poll flips it when it is ready.
            PrimaryButton(
                text = stringResource(R.string.checkout_action_start_learning),
                onClick = actions.onStartLearning,
                isLoading = order?.isActivating == true,
                loadingText = stringResource(R.string.checkout_label_activating),
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().testTag(CheckoutTestTags.START_LEARNING),
            )
        }
    }
}

@Composable
private fun ClosedContent(
    order: CheckoutOrder,
    actions: CheckoutActions,
) {
    val expired = order.status == OrderStatus.EXPIRED
    MascotMessage(
        mascotResId = if (expired) R.drawable.oy2_waiting else R.drawable.oy2_empty,
        title =
            stringResource(
                if (expired) R.string.checkout_title_expired else R.string.checkout_title_cancelled,
            ),
        body =
            stringResource(
                if (expired) R.string.checkout_body_expired else R.string.checkout_body_cancelled,
            ),
    ) {
        PrimaryButton(
            text = stringResource(R.string.checkout_action_new_order),
            onClick = actions.onStartNewOrder,
            shape = PillShape,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = stringResource(R.string.checkout_action_back_to_course),
            onClick = actions.onBackClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CancelOrderDialog(actions: CheckoutActions) {
    AlertDialog(
        onDismissRequest = actions.onDismissCancel,
        title = { Text(text = stringResource(R.string.checkout_dialog_cancel_title)) },
        text = {
            Text(
                text = stringResource(R.string.checkout_dialog_cancel_body),
                style = AppTextStyles.ReadingBody,
            )
        },
        confirmButton = {
            TextButton(onClick = actions.onConfirmCancel) {
                Text(
                    text = stringResource(R.string.checkout_dialog_cancel_confirm),
                    color = BrandDestructive,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = actions.onDismissCancel) {
                Text(text = stringResource(R.string.checkout_dialog_cancel_dismiss))
            }
        },
    )
}
