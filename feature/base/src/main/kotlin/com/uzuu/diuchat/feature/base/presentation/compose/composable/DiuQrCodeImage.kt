package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun DiuQrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    contentDescription: String = "Mã QR cá nhân",
) {
    val colors = AppTheme.colors
    val sizePx = with(LocalDensity.current) { size.toPx().roundToInt().coerceAtLeast(1) }

    val qrBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = content,
        key2 = sizePx,
    ) {
        value =
            withContext(Dispatchers.Default) {
                QrCodeGenerator.generateBitmap(
                    content = content,
                    sizePx = sizePx,
                )?.asImageBitmap()
            }
    }

    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = qrBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.accent,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiuQrCodeImagePreview() {
    AppTheme {
        DiuQrCodeImage(
            content = "https://diu.vn/linh/7K2M9P",
            modifier = Modifier.padding(16.dp),
        )
    }
}
