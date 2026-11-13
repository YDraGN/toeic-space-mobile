package com.toeicspace.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.ui.ToeicSpaceNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToeicSpaceTheme {
                // Exposes Modifier.testTag as the view resource id, so Maestro can find fields by id.
                Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                    ToeicSpaceNavHost()
                }
            }
        }
    }
}
