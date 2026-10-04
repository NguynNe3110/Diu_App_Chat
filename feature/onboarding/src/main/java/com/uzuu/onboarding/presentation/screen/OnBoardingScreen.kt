package com.uzuu.onboarding.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.onboarding.presentation.composable.OnBoardingIllustration
import com.uzuu.onboarding.presentation.composable.OnBoardingIndicator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─── Nội dung 3 slide ─────────────────────────────────────────────────────────
private data class OnBoardingPage(val title: String, val subtitle: String)

private val pages = listOf(
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
    OnBoardingContent(onFinish = onFinish, onPageChanged = viewModel::onPageChanged)
}

@Composable
private fun OnBoardingContent(
    onFinish: () -> Unit,
    onPageChanged: (Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val colors = AppTheme.colors

    // Đồng bộ: vuốt trang → ViewModel
    LaunchedEffect(pagerState, onPageChanged) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {

            // ── Top Bar: Logo + Bỏ qua ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Logo "dịu" dùng font chung, biến thể italic
                Text(
                    text = "dịu",
                    style = AppTheme.typography.displaySmall.copy(
                        fontSize = 26.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = colors.ink,
                )

                // Nút Bỏ qua
                TextButton(onClick = onFinish) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    // Illustration area
                    OnBoardingIllustration(
                        page = page,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(top = 10.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Text nội dung ────────────────────────────────────────
                    AnimatedContent(
                        targetState = page,
                        transitionSpec = {
                            (slideInHorizontally(tween(400)) { it / 4 } + fadeIn(tween(400))) togetherWith
                                (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(200)))
                        },
                        label = "onb_text",
                    ) { targetPage ->
                        val p = pages[targetPage]
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
            }

            // ── Footer: Dots + Nút Next tròn ────────────────────────────────
            Row(
                modifier = Modifier
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
                            onFinish()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.ink),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
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

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFFCF4EC)
@Composable
private fun OnBoardingScreenPreview() {
    AppTheme {
        DiuAmbientBackground {
            OnBoardingContent(onFinish = {})
        }
    }
}
