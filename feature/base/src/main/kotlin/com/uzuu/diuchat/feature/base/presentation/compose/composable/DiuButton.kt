package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuTypography

@Composable
fun DiuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: DiuButtonVariant = DiuButtonVariant.Primary,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors

    val (containerColor, contentColor) =
        when (variant) {
            DiuButtonVariant.Primary -> colors.ink to colors.background
            DiuButtonVariant.Danger -> colors.danger to colors.onAccent
            DiuButtonVariant.Soft -> colors.surface to colors.ink
            DiuButtonVariant.Outline -> Color.Transparent to colors.ink
        }

    val border =
        if (variant == DiuButtonVariant.Outline) {
            BorderStroke(1.dp, if (enabled) colors.line else colors.line.copy(alpha = 0.35f))
        } else {
            null
        }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        enabled = enabled,
        shape = shape,
        border = border,
        colors =
        ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (variant == DiuButtonVariant.Outline) Color.Transparent else containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.35f),
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    ) {
        ProvideTextStyle(
            DiuTypography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
        ) {
            content()
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuButtonPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Tiếp tục")
                }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = DiuButtonVariant.Outline) {
                    Text("Viền ngoài")
                }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = DiuButtonVariant.Danger) {
                    Text("Xóa cuộc trò chuyện")
                }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                    Text("Vô hiệu hóa")
                }
            }
        }
    }
}
