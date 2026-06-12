package com.exacomtv.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.exacomtv.app.BuildConfig
import com.exacomtv.app.R
import com.exacomtv.app.ui.components.shell.StatusPill
import com.exacomtv.app.ui.design.AppColors
import com.exacomtv.data.sync.SyncProgressBus
import com.exacomtv.domain.repository.ProviderRepository
import com.exacomtv.domain.sync.Section
import com.exacomtv.domain.sync.SyncProgress
import com.exacomtv.domain.usecase.M3uProviderSetupCommand
import com.exacomtv.domain.usecase.ValidateAndAddProvider
import com.exacomtv.domain.usecase.XtreamProviderSetupCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Where the Welcome screen should navigate once startup work has finished. */
enum class WelcomeDestination { HOME, SETUP }

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val validateAndAddProvider: ValidateAndAddProvider,
    syncProgressBus: SyncProgressBus
) : ViewModel() {

    private val _destination = MutableStateFlow<WelcomeDestination?>(null)
    val destination: StateFlow<WelcomeDestination?> = _destination.asStateFlow()

    private val acceptingProgress = MutableStateFlow(true)

    val syncProgress: StateFlow<SyncProgress?> =
        combine(syncProgressBus.flow, acceptingProgress) { progress, accept ->
            if (accept) progress else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            maybeSeedDevProvider()

            val providers = providerRepository.getProviders().first()
            if (providers.isEmpty()) {
                acceptingProgress.value = false
                _destination.value = WelcomeDestination.SETUP
                return@launch
            }

            // Re-sync every configured provider on each app launch so newly
            // added channels/VOD/series show up without requiring the user
            // to manually trigger a sync from Settings.
            for (provider in providers) {
                runCatching {
                    providerRepository.refreshProviderData(provider.id, force = false)
                }
            }

            acceptingProgress.value = false
            _destination.value = WelcomeDestination.HOME
        }
    }

    private suspend fun maybeSeedDevProvider() {
        if (providerRepository.getProviders().first().isNotEmpty()) return

        val xtreamServer = BuildConfig.XTREAM_DEV_SERVER
        val xtreamUser = BuildConfig.XTREAM_DEV_USERNAME
        val xtreamPass = BuildConfig.XTREAM_DEV_PASSWORD
        if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank()) {
            validateAndAddProvider.loginXtream(
                XtreamProviderSetupCommand(
                    serverUrl = xtreamServer,
                    username = xtreamUser,
                    password = xtreamPass,
                    name = BuildConfig.XTREAM_DEV_NAME.ifBlank { "Dev (seeded)" },
                    xtreamFastSyncEnabled = true
                )
            )
            return
        }

        val m3uUrl = BuildConfig.M3U_DEV_URL
        if (m3uUrl.isNotBlank()) {
            validateAndAddProvider.addM3u(
                M3uProviderSetupCommand(
                    url = m3uUrl,
                    name = BuildConfig.M3U_DEV_NAME.ifBlank { "Dev M3U (seeded)" }
                )
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            WelcomeDestination.HOME -> onNavigateToHome()
            WelcomeDestination.SETUP -> onNavigateToSetup()
            null -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f),
                            AppColors.HeroTop,
                            AppColors.HeroBottom
                        )
                    )
                )
        )

        WelcomeLoadingCard(
            syncProgress = syncProgress,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        )
    }
}

@Composable
private fun WelcomeLoadingCard(
    syncProgress: SyncProgress?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pillLabel = if (syncProgress != null) {
                stringResource(sectionLabelRes(syncProgress.section))
            } else {
                stringResource(R.string.app_name)
            }
            val pillColor = if (syncProgress != null) {
                sectionColor(syncProgress.section)
            } else {
                AppColors.BrandMuted
            }
            StatusPill(
                label = pillLabel,
                containerColor = pillColor
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (syncProgress == null) {
                CircularProgressIndicator(color = AppColors.Brand)
                Spacer(modifier = Modifier.height(18.dp))
            }
            Text(
                text = stringResource(R.string.welcome_loading_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            val subtitle = if (syncProgress != null && syncProgress.currentLabel.isNotBlank()) {
                syncProgress.currentLabel
            } else {
                stringResource(R.string.welcome_loading_subtitle)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary
            )
            if (syncProgress != null) {
                Spacer(modifier = Modifier.height(14.dp))
                if (syncProgress.total > 0) {
                    LinearProgressIndicator(
                        progress = { syncProgress.current.toFloat() / syncProgress.total.toFloat() },
                        modifier = Modifier.width(260.dp),
                        color = AppColors.Brand,
                        trackColor = AppColors.BrandMuted
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.width(260.dp),
                        color = AppColors.Brand,
                        trackColor = AppColors.BrandMuted
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(
                        R.string.sync_items_indexed_format,
                        syncProgress.itemsIndexed
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

private fun sectionColor(section: Section): Color = when (section) {
    Section.LIVE -> AppColors.Brand
    Section.VOD -> AppColors.Success
    Section.SERIES -> AppColors.Warning
}

private fun sectionLabelRes(section: Section): Int = when (section) {
    Section.LIVE -> R.string.sync_section_live
    Section.VOD -> R.string.sync_section_vod
    Section.SERIES -> R.string.sync_section_series
}
