package com.toeicspace.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.splash.presentation.SplashNavTarget
import com.toeicspace.android.feature.splash.presentation.SplashRoute
import com.toeicspace.android.feature.splash.presentation.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToeicSpaceTheme {
                val splashViewModel: SplashViewModel = hiltViewModel()
                var currentDestination by remember { mutableStateOf<SplashNavTarget?>(null) }

                if (currentDestination == null) {
                    SplashRoute(
                        onNavigate = { target ->
                            currentDestination = target
                        },
                    )
                } else {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "🎉 Splash hoàn tất thành công!",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Đích đến: ${currentDestination?.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        PrimaryButton(
                            text = "Chạy lại Splash (Bình thường)",
                            onClick = {
                                splashViewModel.restart(simulateError = false)
                                currentDestination = null
                            },
                            modifier = Modifier.fillMaxWidth(0.85f),
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        SecondaryButton(
                            text = "Thử kịch bản: Lỗi mạng (Offline)",
                            onClick = {
                                splashViewModel.restart(simulateError = true)
                                currentDestination = null
                            },
                            modifier = Modifier.fillMaxWidth(0.85f),
                        )
                    }
                }
            }
        }
    }
}
