package com.exacomtv.app.ui.screens.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.exacomtv.app.R
import com.exacomtv.app.ui.design.AppColors
import com.exacomtv.app.ui.interaction.TvButton
import com.exacomtv.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _languageSelected = MutableStateFlow<Boolean?>(null)
    val languageSelected: StateFlow<Boolean?> = _languageSelected.asStateFlow()

    init {
        viewModelScope.launch {
            _languageSelected.value = preferencesRepository.languageSelected.first()
        }
    }

    fun selectLanguage(languageTag: String) {
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(languageTag)
            preferencesRepository.setLanguageSelected(true)
            _languageSelected.value = true
        }
    }
}

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit,
    viewModel: LanguageSelectionViewModel = hiltViewModel()
) {
    val languageSelected by viewModel.languageSelected.collectAsStateWithLifecycle()

    LaunchedEffect(languageSelected) {
        if (languageSelected == true) {
            onLanguageSelected()
        }
    }

    // While the preference is loading (null) or already set (true), render
    // nothing — only show the picker once we know the language has NOT been
    // selected yet (false). This avoids a brief flash of this screen on
    // every app launch while the DataStore read completes.
    if (languageSelected != false) {
        return
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

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .widthIn(max = 720.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.language_select_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.language_select_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvButton(
                        onClick = { viewModel.selectLanguage("es") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.language_option_spanish))
                    }
                    TvButton(
                        onClick = { viewModel.selectLanguage("en") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.language_option_english))
                    }
                }
            }
        }
    }
}
