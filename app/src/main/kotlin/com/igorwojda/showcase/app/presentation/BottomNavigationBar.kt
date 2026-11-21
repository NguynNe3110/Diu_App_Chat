package com.uzuu.diuchat.app.presentation

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme

/**
 * BottomNavigationBar — Thanh điều hướng đảo nổi (Floating Island) của Dịu.
 *
 * Gồm 2 khối:
 * 1. Khối viên thuốc (Pill Bar): Chứa 4 tab điều hướng chính (Chỉ icon, không chữ).
 * 2. Khối nút tròn (+): Nút thao tác nhanh để tạo nhóm mới hoặc tạo cuộc trò chuyện mới.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
) {
    val navigationItems = remember { getBottomNavigationItems() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedNavigationIndex = getSelectedNavigationIndex(currentRoute, navigationItems)

    DiuBottomNavigationBarContent(
        selectedIndex = selectedNavigationIndex,
        items = navigationItems,
        onItemClick = { index, item ->
            if (selectedNavigationIndex != index) {
                navController.navigate(item.route) {
                    popUpTo(0)
                    restoreState = true
                }
            }
        },
        onAddClick = onAddClick,
        modifier = modifier,
    )
}

/**
 * UI Component thuần túy để render thanh điều hướng (tách biệt để dễ test và Preview).
 */
@Composable
fun DiuBottomNavigationBarContent(
    selectedIndex: Int,
    onItemClick: (Int, DiuNavigationItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<DiuNavigationItem> = remember { getBottomNavigationItems() },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Pill Bar
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(32.dp),
            color = AppTheme.colors.surface,
            border = BorderStroke(1.dp, AppTheme.colors.line),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    DiuTabIconItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemClick(index, item) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Nút tròn
        Surface(
            onClick = onAddClick,
            modifier = Modifier
                .size(64.dp),
            shape = CircleShape,
            color = AppTheme.colors.surface,
            border = BorderStroke(1.dp, AppTheme.colors.line),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = AppIcons.Plus),
                    contentDescription = "Tạo mới",
                    tint = AppTheme.colors.ink,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun DiuTabIconItem(
    item: DiuNavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animation chuyển màu êm dịu khi chuyển tab
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AppPalette.Peach.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "tab_bg_color",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) AppTheme.colors.ink else AppTheme.colors.inkTertiary,
        animationSpec = tween(durationMillis = 250),
        label = "tab_icon_color",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(26.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun getBottomNavigationItems() =
    listOf(
        DiuNavigationItem(
            iconRes = AppIcons.Chat,
            contentDescription = "Trò chuyện",
            route = NavigationRoute.AlbumList, //todo refactor route
        ),
        DiuNavigationItem(
            iconRes = AppIcons.Phone,
            contentDescription = "Cuộc gọi",
            route = NavigationRoute.Favourites, //todo refactor route
        ),
        DiuNavigationItem(
            iconRes = AppIcons.People,
            contentDescription = "Danh bạ",
            route = NavigationRoute.AboutLibraries, //todo refactor route
        ),
        DiuNavigationItem(
            iconRes = AppIcons.QrCode,
            contentDescription = "QR của tôi",
            route = NavigationRoute.Settings, //todo refactor route
        ),
    )

private fun getSelectedNavigationIndex(
    currentRoute: String?,
    navigationItems: List<DiuNavigationItem>,
): Int =
    navigationItems
        .indexOfFirst { item ->
            when (currentRoute) {
                null -> false
                NavigationRoute.AlbumDetail::class.qualifiedName -> item.route is NavigationRoute.AlbumList
                else -> item.route::class.qualifiedName == currentRoute
            }
        }.takeIf { it >= 0 } ?: 0

data class DiuNavigationItem(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val route: NavigationRoute,
)

// ═══════════════════════════════════════════════
// Previews
// ═══════════════════════════════════════════════

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFCF4EC)
@Composable
private fun BottomNavigationBarLightPreview() {
    AppTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            DiuBottomNavigationBarContent(
                selectedIndex = 2,
                onItemClick = { _, _ -> },
                onAddClick = {},
            )
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF211511, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BottomNavigationBarDarkPreview() {
    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            DiuBottomNavigationBarContent(
                selectedIndex = 0,
                onItemClick = { _, _ -> },
                onAddClick = {},
            )
        }
    }
}
