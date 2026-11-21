package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuPaginationDots(
    count: Int,
    selectedIndex: Int,
    pageLabel: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(count > 0 && selectedIndex in 0 until count) { "Invalid count or selectedIndex" }
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == selectedIndex
            val targetWidth = if (selected) 26.dp else 8.dp
            val animatedWidth by animateDpAsState(targetWidth, label = "dotWidth")

            Box(
                modifier =
                Modifier
                    .width(animatedWidth)
                    .heightIn(min = 44.dp)
                    .semantics { contentDescription = pageLabel(index) }
                    .selectable(selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            if (selected) AppTheme.colors.ink else AppTheme.colors.line,
                            CircleShape,
                        ),
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuPaginationDotsPreview() {
    AppTheme {
        var selected by remember { mutableIntStateOf(0) }
        Surface(color = AppTheme.colors.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiuPaginationDots(3, selected, { "Trang ${it + 1}" }, { selected = it })
                DiuPaginationDots(3, 1, { "Trang ${it + 1}" }, {})
            }
        }
    }
}
