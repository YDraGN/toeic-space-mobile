package com.toeicspace.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.compose.rememberNavController
import com.toeicspace.android.core.analytics.AnalyticsHelper
import com.toeicspace.android.core.analytics.ProvideMonitoring
import com.toeicspace.android.core.analytics.TrackScreenViews
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.ui.ToeicSpaceNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var analytics: AnalyticsHelper

    @Inject lateinit var crashReporter: CrashReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToeicSpaceTheme {
                ProvideMonitoring(analytics = analytics, crashReporter = crashReporter) {
                    val navController = rememberNavController()
                    TrackScreenViews(navController)
                    // Exposes Modifier.testTag as the view resource id, so Maestro can find fields by id.
                    Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                        ToeicSpaceNavHost(navController = navController)
                    }
                }
            }
        }
    }
}
