package com.toeicspace.android.core.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val BadgeSize = 36.dp
private val BadgeIconSize = 16.dp

/**
 * The user's avatar with a camera badge that opens the system photo picker. The picker needs no
 * storage permission. [onPhotoPicked] gets null when the user backs out without choosing.
 */
@Composable
fun EditableAvatar(
    onPhotoPicked: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    val pickPhoto =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            onPhotoPicked(uri)
        }
    val openPicker = {
        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(modifier = modifier) {
        UserAvatar(onClick = openPicker, size = size)
        Surface(
            onClick = openPicker,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = Spacing.xs, y = Spacing.xs)
                    .size(BadgeSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = stringResource(id = R.string.common_cd_change_avatar),
                    modifier = Modifier.size(BadgeIconSize),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF)
@Composable
private fun EditableAvatarPreview() {
    ToeicSpaceTheme {
        EditableAvatar(onPhotoPicked = {}, modifier = Modifier.padding(Spacing.md))
    }
}
