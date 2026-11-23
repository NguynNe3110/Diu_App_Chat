package com.uzuu.diuchat.feature.onboarding.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.feature.base.R as BaseR
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.onboarding.presentation.composable.OnBoardingIllustration
import com.uzuu.diuchat.feature.onboarding.presentation.composable.OnBoardingIndicator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─── Nội dung 3 slide ─────────────────────────────────────────────────────────
private data class OnBoardingPage(
    val title: String,
    val subtitle: String,
)

private val pages =
    listOf(
        OnBoardingPage(
            title = "Một góc nhỏ\ncho người thương",
            subtitle = "dịu là nơi chỉ dành cho vài người thân thiết nhất — không đám đông, không ồn ào.",
        ),
        OnBoardingPage(
            title = "Nói bằng màu\nthay vì biểu tượng",
            subtitle = "Mỗi lời nhắn mang một sắc cảm xúc. Thương, vui, bình yên, nhớ, trầm — người kia cảm được ngay.",
        ),
        OnBoardingPage(
            title = "Không vội.\nKhông làm phiền.",
            subtitle = "Gửi chậm để lời nhắn đến đúng lúc. Giờ yên lặng giữ cho giấc ngủ của cả hai được trọn vẹn.",
        ),
    )

// ─── Màn hình chính ───────────────────────────────────────────────────────────
@Composable
fun OnBoardingScreen(
    onFinish: () -> Unit,
    viewModel: OnBoardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val context = LocalContext.current

    val notificationLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            viewModel.onNotificationsToggled(isGranted)
        }

    val microphoneLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            viewModel.onMicrophoneToggled(isGranted)
        }

    LaunchedEffect(uiState.showPermissionsStep) {
        if (uiState.showPermissionsStep) {
            val hasNotificationPermission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            val hasMicrophonePermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
            viewModel.onNotificationsToggled(hasNotificationPermission)
            viewModel.onMicrophoneToggled(hasMicrophonePermission)
        }
    }

    BackHandler(enabled = uiState.showPermissionsStep) {
        viewModel.onBackFromPermissions()
    }

    OnBoardingContent(
        uiState = uiState,
        onPageChange = viewModel::onPageChanged,
        onSkipOrReachPermissions = viewModel::onOpenPermissionsStep,
        onBackFromPermissions = viewModel::onBackFromPermissions,
        onNotificationsToggle = { enabled ->
            if (!enabled) {
                viewModel.onNotificationsToggled(false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    viewModel.onNotificationsToggled(true)
                } else {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                viewModel.onNotificationsToggled(true)
            }
        },
        onMicrophoneToggle = { enabled ->
            if (!enabled) {
                viewModel.onMicrophoneToggled(false)
            } else {
                val granted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    viewModel.onMicrophoneToggled(true)
                } else {
                    microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        },
        onQuietHoursToggle = viewModel::onQuietHoursToggled,
        onContinue = { viewModel.onCompleteOnboarding(onFinish) },
    )
}

@Composable
private fun OnBoardingContent(
    uiState: OnBoardingUiState,
    onPageChange: (Int) -> Unit = {},
    onSkipOrReachPermissions: () -> Unit = {},
    onBackFromPermissions: () -> Unit = {},
    onNotificationsToggle: (Boolean) -> Unit = {},
    onMicrophoneToggle: (Boolean) -> Unit = {},
    onQuietHoursToggle: (Boolean) -> Unit = {},
    onContinue: () -> Unit = {},
) {
    AnimatedContent(
        targetState = uiState.showPermissionsStep,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(350)) { it / 3 } + fadeIn(tween(350))) togetherWith
                    (slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(350)) { -it / 3 } + fadeIn(tween(350))) togetherWith
                    (slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(200)))
            }
        },
        label = "onboarding_stage",
    ) { showPermissions ->
        if (showPermissions) {
            PermissionsStepContent(
                uiState = uiState,
                onBack = onBackFromPermissions,
                onNotificationsToggle = onNotificationsToggle,
                onMicrophoneToggle = onMicrophoneToggle,
                onQuietHoursToggle = onQuietHoursToggle,
                onContinue = onContinue,
            )
        } else {
            IntroSlidesContent(
                onPageChange = onPageChange,
                onFinishSlides = onSkipOrReachPermissions,
            )
        }
    }
}

@Composable
private fun IntroSlidesContent(
    onPageChange: (Int) -> Unit,
    onFinishSlides: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val colors = AppTheme.colors

    // Đồng bộ sau khi trang dừng hẳn để không gây recomposition giữa nhịp trượt
    LaunchedEffect(pagerState, onPageChange) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChange(page)
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // ── Top Bar: Logo + Bỏ qua ──────────────────────────────────────
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Logo "dịu" dùng Serif display italic
                Text(
                    text = "dịu",
                    style =
                    AppTheme.typography.displaySmall.copy(
                        fontSize = 26.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = colors.ink,
                )

                // Nút Bỏ qua → chuyển tới bước #06 Quyền truy cập
                TextButton(onClick = onFinishSlides) {
                    Text(
                        text = "Bỏ qua",
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = colors.inkSecondary,
                    )
                }
            }

            // ── Art Zone: Minh họa 3 slide ──────────────────────────────────
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = pages.size - 1,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                val p = pages[page]
                Column(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    // Illustration area
                    OnBoardingIllustration(
                        page = page,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(top = 10.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Text nội dung ────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = p.title,
                            style = AppTheme.typography.displayMedium,
                            color = colors.ink,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        Text(
                            text = p.subtitle,
                            style = AppTheme.typography.bodyMedium,
                            color = colors.inkSecondary,
                        )
                    }
                }
            }

            // ── Footer: Dots + Nút Next tròn ────────────────────────────────
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Indicator dots
                OnBoardingIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    onDotClick = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                )

                // Nút Next tròn (CTA)
                Button(
                    onClick = {
                        val next = pagerState.currentPage + 1
                        if (next < pages.size) {
                            scope.launch { pagerState.animateScrollToPage(next) }
                        } else {
                            onFinishSlides()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.ink),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Tiếp tục",
                        tint = colors.background,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

// ─── Onboarding Screen #06: Quyền truy cập ────────────────────────────────────
@Composable
private fun PermissionsStepContent(
    uiState: OnBoardingUiState,
    onBack: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onMicrophoneToggle: (Boolean) -> Unit,
    onQuietHoursToggle: (Boolean) -> Unit,
    onContinue: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top bar with back button
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colors.ink,
                )
            }
        }

        Column(
            modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Để dịu ở bên\nbạn nhẹ nhàng",
                style = AppTheme.typography.displayMedium,
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bạn có thể đổi bất cứ lúc nào trong phần cài đặt.",
                style = AppTheme.typography.bodyMedium,
                color = colors.inkSecondary,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PermissionToggleCard(
                    iconRes = BaseR.drawable.ic_bell,
                    iconBackground = AppPalette.Peach,
                    title = "Thông báo dịu",
                    subtitle = "Chỉ báo tin từ vòng thân, rung nhẹ một lần",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = onNotificationsToggle,
                )

                PermissionToggleCard(
                    iconRes = BaseR.drawable.ic_mic,
                    iconBackground = AppPalette.Peach,
                    title = "Micro",
                    subtitle = "Để gửi lời nhắn thoại và gọi",
                    checked = uiState.microphoneEnabled,
                    onCheckedChange = onMicrophoneToggle,
                )

                PermissionToggleCard(
                    iconRes = BaseR.drawable.ic_moon,
                    iconBackground = AppPalette.QuietPurple.copy(alpha = 0.25f),
                    title = "Giờ yên lặng tự động",
                    subtitle = "22:30 – 6:00 mỗi ngày",
                    checked = uiState.quietHoursEnabled,
                    onCheckedChange = onQuietHoursToggle,
                )
            }
        }

        Button(
            onClick = onContinue,
            enabled = !uiState.isCompleting,
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.ink),
        ) {
            if (uiState.isCompleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.background,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Tiếp tục",
                    style = AppTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = colors.background,
                )
            }
        }
    }
}

@Composable
private fun PermissionToggleCard(
    @DrawableRes iconRes: Int,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface.copy(alpha = 0.85f))
            .border(1.dp, colors.line, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.ink,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
            Text(
                text = subtitle,
                style = AppTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
            SwitchDefaults.colors(
                checkedThumbColor = colors.background,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.background,
                uncheckedTrackColor = colors.line,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFFCF4EC)
@Composable
private fun OnBoardingScreenPreview() {
    AppTheme {
        DiuAmbientBackground {
            OnBoardingContent(uiState = OnBoardingUiState.Initial)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCF4EC)
@Composable
private fun OnBoardingPermissionsPreview() {
    AppTheme {
        DiuAmbientBackground {
            OnBoardingContent(
                uiState =
                OnBoardingUiState(
                    showPermissionsStep = true,
                    notificationsEnabled = true,
                    microphoneEnabled = true,
                    quietHoursEnabled = true,
                ),
            )
        }
    }
}
