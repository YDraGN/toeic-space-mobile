package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
) {
    Image(
        painter = painterResource(id = R.drawable.ic_brand_logo),
        contentDescription = "ToeicSpace Logo",
        modifier = modifier.height(height),
        contentScale = ContentScale.Fit,
    )
}

@Preview(name = "Brand Logo", showBackground = true, backgroundColor = 0xFFFAF9FF, widthDp = 360)
@Composable
private fun BrandLogoPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.smd)) {
            BrandLogo()
        }
    }
}
