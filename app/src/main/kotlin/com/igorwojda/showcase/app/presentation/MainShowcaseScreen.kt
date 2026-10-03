package com.uzuu.diuchat.app.presentation

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uzuu.diuchat.app.BuildConfig
import com.uzuu.diuchat.app.presentation.util.NavigationDestinationLogger
import com.uzuu.diuchat.feature.album.presentation.screen.albumdetail.AlbumDetailScreen
import com.uzuu.diuchat.feature.album.presentation.screen.albumlist.AlbumListScreen
import com.uzuu.diuchat.feature.favourite.presentation.screen.favourite.FavouriteScreen
import com.uzuu.diuchat.feature.settings.presentation.screen.aboutlibraries.AboutLibrariesScreen
import com.uzuu.diuchat.feature.settings.presentation.screen.settings.SettingsScreen

@Composable
fun MainShowcaseScreen(modifier: Modifier = Modifier) {
    // tạo và lưu trữ trạng thái của NavController
    val navController = rememberNavController()

    if (BuildConfig.DEBUG) {
        addOnDestinationChangedListener(navController)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController) }, // thanh bottom nav
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.AlbumList,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<NavigationRoute.AlbumList> {
                AlbumListScreen(
                    // artistName: String, albumName: String, mbId: String?
                    onNavigateToAlbumDetail = { artistName, albumName, albumMbId ->
                        navController.navigate(
                            NavigationRoute.AlbumDetail(artistName, albumName, albumMbId),
                        )
                    },
                )
            }
            composable<NavigationRoute.AlbumDetail> { backStackEntry ->
                // Retrieve typed args
                val args = backStackEntry.toRoute<NavigationRoute.AlbumDetail>()

                AlbumDetailScreen(
                    albumName = args.albumName,
                    artistName = args.artistName,
                    albumMbId = args.albumMbId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
            composable<NavigationRoute.Favourites> {
                FavouriteScreen()
            }
            composable<NavigationRoute.Settings> {
                SettingsScreen(
                    onNavigateToAboutLibraries = {
                        navController.navigate(NavigationRoute.AboutLibraries)
                    },
                )
            }
            composable<NavigationRoute.AboutLibraries> {
                AboutLibrariesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

//khi chuyển màn thì in ra log
private fun addOnDestinationChangedListener(navController: NavController) {
    navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?,
            ) {
                NavigationDestinationLogger.logDestinationChange(destination, arguments)
            }
        },
    )
}
