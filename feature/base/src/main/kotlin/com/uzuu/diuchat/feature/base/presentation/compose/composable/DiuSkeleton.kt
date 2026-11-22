package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

private const val DEFAULT_SHIMMER_DURATION_MS = 1250
private const val DEFAULT_HIGHLIGHT_WIDTH_RATIO = 0.45f
private const val MIN_HIGHLIGHT_WIDTH_RATIO = 0.1f
private const val SHIMMER_DIAGONAL_SLOPE = 0.35f
private const val SHIMMER_BASE_ALPHA = 0.65f
private const val SHIMMER_HIGHLIGHT_ALPHA = 0.95f

private val PersonNameWidthFractions = listOf(0.46f, 0.38f, 0.54f, 0.42f, 0.50f, 0.36f)
private val PersonSubtitleWidthFractions = listOf(0.76f, 0.64f, 0.82f, 0.58f, 0.72f, 0.68f)

/**
 * Tọa độ điểm đầu và điểm cuối của dải màu shimmer linear gradient trên một vùng vẽ.
 */
@Immutable
data class ShimmerGradientBounds(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
)

/**
 * Tính tọa độ dải sáng shimmer trượt chéo từ trái-trên sang phải-dưới theo [progress] trong `[0f, 1f]`.
 */
fun calculateShimmerGradientBounds(
    widthPx: Float,
    heightPx: Float,
    progress: Float,
    highlightWidthRatio: Float = DEFAULT_HIGHLIGHT_WIDTH_RATIO,
): ShimmerGradientBounds {
    if (widthPx <= 0f || heightPx <= 0f) {
        return ShimmerGradientBounds(0f, 0f, 0f, 0f)
    }
    val clampedProgress = progress.coerceIn(0f, 1f)
    val bandWidth = widthPx * highlightWidthRatio.coerceAtLeast(MIN_HIGHLIGHT_WIDTH_RATIO)
    val totalTravel = widthPx + (bandWidth * 2f)
    val startX = -bandWidth + (totalTravel * clampedProgress)
    val endX = startX + bandWidth
    val endY = heightPx * SHIMMER_DIAGONAL_SLOPE
    return ShimmerGradientBounds(
        startX = startX,
        startY = 0f,
        endX = endX,
        endY = endY,
    )
}

/**
 * CompositionLocal chia sẻ chung một [State] tiến độ shimmer cho toàn bộ skeleton con
 * bên trong [DiuShimmerContainer] để mọi dòng lướt sáng đồng pha như `Shimmer.fromColors` bên Flutter.
 */
val LocalDiuShimmerProgress = compositionLocalOf<State<Float>?> { null }

/**
 * Trả về [State] tiến độ shimmer `[0f..1f]`.
 * Nếu đang nằm trong [DiuShimmerContainer], tái sử dụng animation dùng chung; ngược lại tạo transition cục bộ.
 */
@Composable
fun rememberDiuShimmerProgress(durationMillis: Int = DEFAULT_SHIMMER_DURATION_MS): State<Float> {
    val shared = LocalDiuShimmerProgress.current
    if (shared != null) {
        return shared
    }
    val transition = rememberInfiniteTransition(label = "DiuShimmerLocal")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "DiuShimmerProgress",
    )
}

/**
 * Bọc một cụm/danh sách skeleton để toàn bộ placeholder bên trong dùng chung một [rememberInfiniteTransition],
 * đảm bảo ánh sáng shimmer quét đồng bộ và tránh sinh nhiều animation độc lập.
 */
@Composable
fun DiuShimmerContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    durationMillis: Int = DEFAULT_SHIMMER_DURATION_MS,
    content: @Composable () -> Unit,
) {
    val progressState: State<Float>? =
        if (enabled) {
            val transition = rememberInfiniteTransition(label = "DiuShimmerContainer")
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "DiuShimmerContainerProgress",
            )
        } else {
            null
        }
    CompositionLocalProvider(LocalDiuShimmerProgress provides progressState) {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * Áp hiệu ứng shimmer lên bất kỳ Composable nào.
 *
 * Đọc `progressState.value` hoàn toàn trong pha Draw (`drawBehind`) để bỏ qua Recomposition và Layout
 * ở mỗi frame animation 60/120fps.
 */
fun Modifier.diuShimmerEffect(
    progressState: State<Float>,
    baseColor: Color,
    highlightColor: Color,
    shape: Shape = RoundedCornerShape(10.dp),
    enabled: Boolean = true,
): Modifier = this
    .clip(shape)
    .drawBehind {
        if (!enabled) {
            drawRect(color = baseColor)
        } else {
            val bounds =
                calculateShimmerGradientBounds(
                    widthPx = size.width,
                    heightPx = size.height,
                    progress = progressState.value,
                )
            val brush =
                Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(bounds.startX, bounds.startY),
                    end = Offset(bounds.endX, bounds.endY),
                )
            drawRect(brush = brush)
        }
    }

/**
 * Khối placeholder chữ nhật bo góc có shimmer theo token màu của [AppTheme].
 */
@Composable
fun DiuSkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val progressState = rememberDiuShimmerProgress()
    Box(
        modifier =
        modifier.diuShimmerEffect(
            progressState = progressState,
            baseColor = colors.line.copy(alpha = SHIMMER_BASE_ALPHA),
            highlightColor = colors.surfaceElevated.copy(alpha = SHIMMER_HIGHLIGHT_ALPHA),
            shape = shape,
            enabled = enabled,
        ),
    )
}

/**
 * Placeholder hình tròn (phù hợp với [DiuOrbAvatar]).
 */
@Composable
fun DiuSkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    enabled: Boolean = true,
) {
    DiuSkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape,
        enabled = enabled,
    )
}

/**
 * Placeholder dạng dòng chữ bo tròn hai đầu (pill).
 */
@Composable
fun DiuSkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    enabled: Boolean = true,
) {
    DiuSkeletonBox(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(height / 2),
        enabled = enabled,
    )
}

/**
 * Skeleton khớp bố cục của [DiuPersonRow] (avatar tròn + tên + timestamp + phụ đề).
 */
@Composable
fun DiuPersonRowSkeleton(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = true,
    showTimestamp: Boolean = true,
    nameWidthFraction: Float = 0.45f,
    subtitleWidthFraction: Float = 0.72f,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiuSkeletonCircle(size = 44.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DiuSkeletonLine(
                    modifier = Modifier.fillMaxWidth(nameWidthFraction),
                    height = 14.dp,
                )
                if (showTimestamp) {
                    DiuSkeletonLine(
                        modifier = Modifier.width(34.dp),
                        height = 11.dp,
                    )
                }
            }
            if (showSubtitle) {
                DiuSkeletonLine(
                    modifier = Modifier.fillMaxWidth(subtitleWidthFraction),
                    height = 12.dp,
                )
            }
        }
    }
}

/**
 * Danh sách [DiuPersonRowSkeleton] đồng pha shimmer với độ rộng dòng biến thiên tự nhiên.
 */
@Composable
fun DiuPersonListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    showSubtitle: Boolean = true,
    showTimestamp: Boolean = true,
) {
    DiuShimmerContainer(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(itemCount.coerceAtLeast(1)) { index ->
                DiuPersonRowSkeleton(
                    showSubtitle = showSubtitle,
                    showTimestamp = showTimestamp,
                    nameWidthFraction = PersonNameWidthFractions[index % PersonNameWidthFractions.size],
                    subtitleWidthFraction = PersonSubtitleWidthFractions[index % PersonSubtitleWidthFractions.size],
                )
            }
        }
    }
}

/**
 * Skeleton dạng thẻ [DiuCard] (avatar nhỏ + tiêu đề + nhiều dòng nội dung),
 * dùng cho kỷ niệm, thư niêm phong, nhật ký hoặc mục chi tiết.
 */
@Composable
fun DiuCardSkeleton(
    modifier: Modifier = Modifier,
    lineCount: Int = 2,
) {
    DiuCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DiuSkeletonCircle(size = 34.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DiuSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.42f),
                    height = 13.dp,
                )
                DiuSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.26f),
                    height = 10.dp,
                )
            }
        }
        if (lineCount > 0) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(lineCount) { index ->
                    val widthFraction = if (index == lineCount - 1) 0.62f else 0.94f
                    DiuSkeletonLine(
                        modifier = Modifier.fillMaxWidth(widthFraction),
                        height = 12.dp,
                    )
                }
            }
        }
    }
}

/**
 * Skeleton dạng luồng tin nhắn chat xen kẽ trái/phải đồng pha shimmer.
 */
@Composable
fun DiuChatMessagesSkeleton(
    modifier: Modifier = Modifier,
    bubbleCount: Int = 5,
) {
    val widthFractions = listOf(0.68f, 0.56f, 0.74f, 0.48f, 0.62f)
    DiuShimmerContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(bubbleCount.coerceAtLeast(1)) { index ->
                val isMine = index % 2 == 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                ) {
                    DiuSkeletonBox(
                        modifier =
                        Modifier
                            .fillMaxWidth(widthFractions[index % widthFractions.size])
                            .height(if (index % 3 == 0) 58.dp else 44.dp),
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            }
        }
    }
}

@Preview(name = "Skeleton — Light", showBackground = true)
@Preview(name = "Skeleton — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuSkeletonPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuShimmerContainer {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DiuPersonListSkeleton(itemCount = 3)
                    DiuCardSkeleton(modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}
