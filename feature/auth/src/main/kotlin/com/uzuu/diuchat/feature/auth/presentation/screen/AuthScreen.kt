package com.uzuu.diuchat.feature.auth.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.auth.BuildConfig
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.auth.presentation.composable.Keypad
import com.uzuu.diuchat.feature.auth.presentation.composable.OtpCells
import com.uzuu.diuchat.feature.auth.presentation.util.GoogleSignInHelper
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val currentOnAuthSuccess by rememberUpdatedState(onAuthSuccess)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.otpState) {
        if (uiState.otpState is OtpState.Success && !uiState.isChoosingUsername) {
            currentOnAuthSuccess()
        }
    }

    val isAwaitingOrVerifying =
        uiState.otpState is OtpState.AwaitingCode ||
            uiState.otpState is OtpState.Verifying ||
            (uiState.otpState is OtpState.Error && (uiState.otpState as OtpState.Error).phoneNumber.isNotEmpty())

    BackHandler(enabled = uiState.isChoosingUsername || isAwaitingOrVerifying) {
        viewModel.onBackToPhone()
    }

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when {
            uiState.isChoosingUsername -> {
                ChooseUsernameContent(
                    uiState = uiState,
                    onUsernameChange = viewModel::onUsernameInputChanged,
                    onConfirm = { viewModel.onConfirmUsernameClicked(onAuthSuccess) },
                )
            }

            isAwaitingOrVerifying -> {
                OtpVerificationContent(
                    uiState = uiState,
                    onKeyPress = { key -> viewModel.onKeypadOtpPress(key, onAuthSuccess) },
                    onBack = viewModel::onBackToPhone,
                    onResend = viewModel::onResendOtpClicked,
                )
            }

            else -> {
                PhoneInputContent(
                    uiState = uiState,
                    onKeyPress = viewModel::onKeypadPhonePress,
                    onSendOtp = viewModel::onSendOtpClicked,
                    onGoogleSignIn = {
                        coroutineScope.launch {
                            viewModel.onStartGoogleSignIn()
                            GoogleSignInHelper.getGoogleIdToken(
                                context = context,
                                serverClientId = BuildConfig.GRADLE_GOOGLE_WEB_CLIENT_ID,
                            ).onSuccess { idToken ->
                                viewModel.onGoogleIdTokenReceived(idToken, onAuthSuccess)
                            }.onFailure { error ->
                                if (error is CancellationException) {
                                    viewModel.onGoogleSignInError("Đã hủy đăng nhập Google.")
                                } else {
                                    viewModel.onGoogleSignInError(
                                        error.message ?: "Đăng nhập Google thất bại.",
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun PhoneInputContent(
    uiState: AuthUiState,
    onKeyPress: (String) -> Unit,
    onSendOtp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val isSending = uiState.otpState is OtpState.Sending

    Column(modifier = modifier.fillMaxSize()) {
        DiuTopBar(title = "")

        Column(
            modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đăng nhập\nvào dịu",
                style =
                AppTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DiuSerif,
                    lineHeight = 40.sp,
                ),
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nhanh chóng và bảo mật. dịu giữ trọn sự riêng tư của bạn.",
                style = AppTheme.typography.bodyMedium,
                color = colors.inkSecondary,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Google Sign-In button
            GoogleSignInButton(
                onClick = onGoogleSignIn,
                isLoading = uiState.isGoogleLoading,
            )

            if (uiState.googleErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.googleErrorMessage,
                    style = AppTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }

            OrDivider()

            // Vietnamese phone number field (+84 chip + number display)
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "+84",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.ink,
                    )
                }

                if (uiState.rawPhone.isEmpty()) {
                    Text(
                        text = "912 345 678",
                        style = AppTheme.typography.titleMedium,
                        color = colors.inkTertiary,
                    )
                } else {
                    Text(
                        text = uiState.formattedPhone,
                        style =
                        AppTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.ink,
                    )
                }
            }

            if (uiState.otpState is OtpState.Error) {
                val errorState = uiState.otpState as OtpState.Error
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorState.message,
                    style = AppTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }
        }

        // Bottom CTA & Keypad
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            DiuButton(
                onClick = onSendOtp,
                enabled = uiState.isPhoneValid && !isSending && !uiState.isGoogleLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.background,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Gửi mã xác nhận qua SMS")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Keypad(
                onKeyPress = onKeyPress,
                enabled = !isSending && !uiState.isGoogleLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.5.dp, colors.line, RoundedCornerShape(16.dp))
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.ink,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "G",
                    style =
                    AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                    ),
                    color = colors.ink,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Tiếp tục với Google",
                    style =
                    AppTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.ink,
                )
            }
        }
    }
}

@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.line),
        )
        Text(
            text = "hoặc số điện thoại",
            style = AppTheme.typography.bodySmall,
            color = colors.inkTertiary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Box(
            modifier =
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.line),
        )
    }
}

@Composable
private fun ChooseUsernameContent(
    uiState: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier =
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        DiuTopBar(title = "")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tạo tên định danh\ncủa bạn",
            style =
            AppTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DiuSerif,
                lineHeight = 40.sp,
            ),
            color = colors.ink,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mọi người có thể tìm thấy và gửi lời mời kết nối tới bạn qua @username duy nhất này.",
            style = AppTheme.typography.bodyMedium,
            color = colors.inkSecondary,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.5.dp, colors.line, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "@",
                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.inkTertiary,
            )

            BasicTextField(
                value = uiState.usernameInput,
                onValueChange = onUsernameChange,
                singleLine = true,
                textStyle =
                AppTheme.typography.titleMedium.copy(
                    color = colors.ink,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (uiState.usernameInput.isEmpty()) {
                        Text(
                            text = "minh_an",
                            style = AppTheme.typography.titleMedium,
                            color = colors.inkTertiary,
                        )
                    }
                    innerTextField()
                },
            )

            Text(
                text = "${uiState.canonicalUsername.length}/30",
                style = AppTheme.typography.bodySmall,
                color = colors.inkTertiary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "3–30 ký tự: chữ thường (a-z), chữ số (0-9) và dấu gạch dưới (_)",
            style = AppTheme.typography.bodySmall,
            color = colors.inkTertiary,
        )

        if (uiState.usernameErrorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.usernameErrorMessage,
                style = AppTheme.typography.bodySmall,
                color = colors.danger,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DiuButton(
            onClick = onConfirm,
            enabled = uiState.isUsernameValid && !uiState.isClaimingUsername,
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            if (uiState.isClaimingUsername) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.background,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Xác nhận & Vào Dịu")
            }
        }
    }
}

@Composable
private fun OtpVerificationContent(
    uiState: AuthUiState,
    onKeyPress: (String) -> Unit,
    onBack: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val awaitingState = uiState.otpState as? OtpState.AwaitingCode
    val verifyingState = uiState.otpState as? OtpState.Verifying
    val code = awaitingState?.code ?: verifyingState?.code ?: ""
    val isVerifying = verifyingState != null

    Column(modifier = modifier.fillMaxSize()) {
        DiuTopBar(
            title = "",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.ink,
                    )
                }
            },
        )

        Column(
            modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nhập mã\nvừa gửi tới bạn",
                style =
                AppTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DiuSerif,
                    lineHeight = 40.sp,
                ),
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gửi tới +84 ${uiState.formattedPhone} · ",
                    style = AppTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                )
                Text(
                    text = "đổi số",
                    style =
                    AppTheme.typography.bodyMedium.copy(
                        color = colors.ink,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.clickable(onClick = onBack),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OtpCells(code = code)

            Spacer(modifier = Modifier.height(18.dp))

            if (awaitingState?.error != null) {
                Text(
                    text = awaitingState.error,
                    style = AppTheme.typography.bodyMedium,
                    color = colors.danger,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.resendSecondsRemaining > 0) {
                Text(
                    text = "Gửi lại mã sau 0:${uiState.resendSecondsRemaining.toString().padStart(2, '0')}",
                    style = AppTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                )
            } else {
                Text(
                    text = "Gửi lại mã",
                    style =
                    AppTheme.typography.bodyMedium.copy(
                        color = colors.ink,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.clickable(onClick = onResend),
                )
            }
        }

        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            if (isVerifying) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.accent,
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                Keypad(
                    onKeyPress = onKeyPress,
                    enabled = !isVerifying,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPhoneInputPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            PhoneInputContent(
                uiState = AuthUiState(
                    otpState = OtpState.EditingPhone(phoneNumber = "901234567"),
                ),
                onKeyPress = {},
                onSendOtp = {},
                onGoogleSignIn = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenChooseUsernamePreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            ChooseUsernameContent(
                uiState = AuthUiState(
                    isChoosingUsername = true,
                    usernameInput = "minh_an",
                ),
                onUsernameChange = {},
                onConfirm = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenOtpVerificationPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            OtpVerificationContent(
                uiState = AuthUiState(
                    otpState = OtpState.AwaitingCode(
                        phoneNumber = "901234567",
                        code = "123",
                        resendDeadlineEpochMs = 0L,
                    ),
                    resendSecondsRemaining = 45,
                ),
                onKeyPress = {},
                onBack = {},
                onResend = {},
            )
        }
    }
}
