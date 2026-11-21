package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        contentColor = colors.ink,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuCardPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuCard(modifier = Modifier.padding(16.dp)) {
                Text("Khoảng yên", style = AppTheme.typography.titleMedium)
                Text("Một chút dịu dàng cho hôm nay.")
            }
        }
    }
}
