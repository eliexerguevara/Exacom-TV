package com.exacomtv.app.ui.screens.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.exacomtv.app.R
import com.exacomtv.app.ui.components.shell.StatusPill
import com.exacomtv.app.ui.design.AppColors
import com.exacomtv.app.ui.interaction.TvButton
import com.exacomtv.app.ui.screens.provider.ProviderSetupViewModel

/**
 * Base URL of the ExacomTV Portal that this app connects to.
 *
 * The full provider-setup flow has been removed for end users: the app now
 * authenticates directly against this fixed portal using only a
 * username/password (Xtream Codes credentials issued from the portal).
 */
private const val PORTAL_SERVER_URL = "http://38.252.74.30:9191"
private const val PORTAL_PROVIDER_NAME = "ExacomTV Portal"

@Composable
fun PortalLoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: ProviderSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.onboardingCompletion) {
        if (uiState.onboardingCompletion == ProviderSetupViewModel.OnboardingCompletion.READY ||
            uiState.onboardingCompletion == ProviderSetupViewModel.OnboardingCompletion.SAVED_RESUMING
        ) {
            onLoginSuccess()
        }
    }

    fun submit() {
        viewModel.loginXtream(
            serverUrl = PORTAL_SERVER_URL,
            username = username.trim(),
            password = password,
            name = PORTAL_PROVIDER_NAME,
            httpUserAgent = "",
            httpHeaders = ""
        )
    }

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
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.92f))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusPill(
                    label = stringResource(R.string.app_name),
                    containerColor = AppColors.BrandMuted
                )
                Text(
                    text = stringResource(R.string.portal_login_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.portal_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Material3Theme(colorScheme = darkColorScheme()) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(R.string.setup_user_hint)) },
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = portalTextFieldColors()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.setup_pass_hint)) },
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = portalTextFieldColors()
                    )
                }

                val errorMessage = uiState.validationError ?: uiState.error
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Warning,
                        textAlign = TextAlign.Center
                    )
                }

                TvButton(
                    onClick = ::submit,
                    enabled = !uiState.isLoading && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.colors(
                        containerColor = AppColors.Brand,
                        contentColor = Color.White,
                        disabledContainerColor = AppColors.Brand.copy(alpha = 0.35f),
                        disabledContentColor = AppColors.TextPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).widthIn(min = 20.dp, max = 20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = stringResource(R.string.portal_login_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun portalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.TextPrimary,
    unfocusedTextColor = AppColors.TextPrimary,
    disabledTextColor = AppColors.TextSecondary,
    focusedLabelColor = AppColors.TextPrimary,
    unfocusedLabelColor = AppColors.TextSecondary,
    disabledLabelColor = AppColors.TextSecondary,
    cursorColor = AppColors.TextPrimary,
    focusedBorderColor = AppColors.Brand,
    unfocusedBorderColor = AppColors.Outline,
    disabledBorderColor = AppColors.Outline,
    focusedPlaceholderColor = AppColors.TextSecondary,
    unfocusedPlaceholderColor = AppColors.TextSecondary
)
