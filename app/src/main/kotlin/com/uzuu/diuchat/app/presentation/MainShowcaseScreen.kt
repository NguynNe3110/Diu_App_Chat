package com.uzuu.diuchat.app.presentation

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uzuu.diuchat.app.BuildConfig
import com.uzuu.diuchat.app.presentation.util.NavigationDestinationLogger
import com.uzuu.diuchat.feature.album.presentation.screen.albumdetail.AlbumDetailScreen
import com.uzuu.diuchat.feature.album.presentation.screen.albumlist.AlbumListScreen
import com.uzuu.diuchat.feature.auth.presentation.screen.AuthScreen
import com.uzuu.diuchat.feature.chat.presentation.screen.ChatScreen
import com.uzuu.diuchat.feature.contacts.presentation.screen.ContactsScreen
import com.uzuu.diuchat.feature.home.presentation.HomeScreen
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.onboarding.presentation.screen.OnBoardingScreen

@Composable
fun MainShowcaseScreen(modifier: Modifier = Modifier) {
    // tạo và lưu trữ trạng thái của NavController
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    if (BuildConfig.DEBUG) {
        addOnDestinationChangedListener(navController)
    }

    DiuAmbientBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                val isAuthOrOnboarding =
                    backStackEntry?.destination?.hasRoute<NavigationRoute.OnBoarding>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.Auth>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.Chat>() == true
                if (!isAuthOrOnboarding) {
                    BottomNavigationBar(navController)
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                // ponytail: luôn mở onboarding; lưu cờ hoàn tất khi cần chỉ hiện lần đầu.
                startDestination = NavigationRoute.OnBoarding,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<NavigationRoute.OnBoarding> {
                    OnBoardingScreen(
                        onFinish = {
                            navController.navigate(NavigationRoute.Auth) {
                                popUpTo<NavigationRoute.OnBoarding> { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable<NavigationRoute.Auth> {
                    AuthScreen(
                        onAuthSuccess = {
                            navController.navigate(NavigationRoute.Home) {
                                popUpTo<NavigationRoute.Auth> { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable<NavigationRoute.Home> {
                    HomeScreen(
                        onOpenContacts = {
                            navController.navigate(NavigationRoute.AboutLibraries)
                        },
                    )
                }
                composable<NavigationRoute.Contacts> {
                    ContactsScreen()
                }
                composable<NavigationRoute.Chat> {
                    ChatScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
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
//            composable<NavigationRoute.Favourites> {
//                FavouriteScreen()
//            }
//            composable<NavigationRoute.Settings> {
//                SettingsScreen(
//                    onNavigateToAboutLibraries = {
//                        navController.navigate(NavigationRoute.AboutLibraries)
//                    },
//                )
//            }
//            composable<NavigationRoute.AboutLibraries> {
//                AboutLibrariesScreen(
//                    onBackClick = {
//                        navController.popBackStack()
//                    },
//                )
//            }
            }
        }
    }
}

// khi chuyển màn thì in ra log
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
