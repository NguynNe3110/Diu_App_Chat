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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.auth.presentation.composable.Keypad
import com.uzuu.diuchat.feature.auth.presentation.composable.OtpCells
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    LaunchedEffect(uiState.otpState) {
        if (uiState.otpState is OtpState.Success) {
            onAuthSuccess()
        }
    }

    val isAwaitingOrVerifying = uiState.otpState is OtpState.AwaitingCode ||
        uiState.otpState is OtpState.Verifying ||
        (uiState.otpState is OtpState.Error && (uiState.otpState as OtpState.Error).phoneNumber.isNotEmpty())

    BackHandler(enabled = isAwaitingOrVerifying) {
        viewModel.onBackToPhone()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (!isAwaitingOrVerifying) {
            PhoneInputContent(
                uiState = uiState,
                onKeyPress = viewModel::onKeypadPhonePress,
                onSendOtp = viewModel::onSendOtpClicked,
            )
        } else {
            OtpVerificationContent(
                uiState = uiState,
                onKeyPress = { key -> viewModel.onKeypadOtpPress(key, onAuthSuccess) },
                onBack = viewModel::onBackToPhone,
                onResend = viewModel::onResendOtpClicked,
            )
        }
    }
}

@Composable
private fun PhoneInputContent(
    uiState: AuthUiState,
    onKeyPress: (String) -> Unit,
    onSendOtp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val isSending = uiState.otpState is OtpState.Sending

    Column(modifier = modifier.fillMaxSize()) {
        DiuTopBar(title = "")

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Số điện thoại\ncủa bạn",
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 40.sp,
                ),
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Chỉ để người thân tìm thấy bạn. dịu không bao giờ hiện số của bạn cho ai khác.",
                style = AppTheme.typography.bodyMedium,
                color = colors.inkSecondary,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Vietnamese phone number field (+84 chip + number display)
            Row(
                modifier = Modifier
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
                    modifier = Modifier
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
                        style = AppTheme.typography.titleMedium.copy(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            DiuButton(
                onClick = onSendOtp,
                enabled = uiState.isPhoneValid && !isSending,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.background,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Gửi mã xác nhận")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Keypad(
                onKeyPress = onKeyPress,
                enabled = !isSending,
            )

            Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nhập mã\nvừa gửi tới bạn",
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
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
                    style = AppTheme.typography.bodyMedium.copy(
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
                    style = AppTheme.typography.bodyMedium.copy(
                        color = colors.ink,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.clickable(onClick = onResend),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            if (isVerifying) {
                Box(
                    modifier = Modifier
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
