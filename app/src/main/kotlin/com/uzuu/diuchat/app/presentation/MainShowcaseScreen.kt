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
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryViewModel
import com.uzuu.diuchat.feature.calls.presentation.screen.CallScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallViewModel
import com.uzuu.diuchat.feature.chat.presentation.screen.ChatScreen
import com.uzuu.diuchat.feature.chat.presentation.screen.NewGroupScreen
import com.uzuu.diuchat.feature.contacts.presentation.screen.ContactsScreen
import com.uzuu.diuchat.feature.home.presentation.HomeScreen
import com.uzuu.diuchat.feature.journal.presentation.journal.JournalScreen
import com.uzuu.diuchat.feature.journal.presentation.search.SearchScreen
import com.uzuu.diuchat.feature.letters.presentation.screen.LettersScreen
import com.uzuu.diuchat.feature.location.presentation.LiveLocationScreen
import com.uzuu.diuchat.feature.location.presentation.LocationPickerScreen
import com.uzuu.diuchat.feature.media.presentation.storage.StorageScreen
import org.koin.androidx.compose.koinViewModel
import com.uzuu.diuchat.feature.media.presentation.vault.MediaVaultScreen
import com.uzuu.diuchat.feature.media.presentation.viewer.PhotoViewerScreen
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.onboarding.presentation.screen.OnBoardingScreen
import com.uzuu.diuchat.feature.settings.presentation.screen.SettingsScreen

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
                val hideBottomBar =
                    backStackEntry?.destination?.hasRoute<NavigationRoute.OnBoarding>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.Auth>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.Chat>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.Call>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.PhotoViewer>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.LocationPicker>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.LiveLocation>() == true ||
                        backStackEntry?.destination?.hasRoute<NavigationRoute.NewGroup>() == true
                if (!hideBottomBar) {
                    BottomNavigationBar(
                        navController = navController,
                        onAddClick = {
                            navController.navigate(NavigationRoute.NewGroup)
                        },
                    )
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
                        onOpenChat = { conversationId ->
                            navController.navigate(NavigationRoute.Chat(conversationId))
                        },
                        onOpenContacts = {
                            navController.navigate(NavigationRoute.Contacts)
                        },
                        onOpenCheckin = {
                            navController.navigate(NavigationRoute.Journal)
                        },
                        onOpenQuietHours = {
                            navController.navigate(NavigationRoute.Settings)
                        },
                        onInvite = {
                            navController.navigate(NavigationRoute.Contacts)
                        },
                    )
                }
                composable<NavigationRoute.Contacts> {
                    ContactsScreen(
                        onNavigateToChat = { conversationId ->
                            navController.navigate(NavigationRoute.Chat(conversationId))
                        },
                        onStartCall = {
                            navController.navigate(NavigationRoute.Call)
                        },
                    )
                }
                composable<NavigationRoute.Chat> { backStackEntry ->
                    val args = backStackEntry.toRoute<NavigationRoute.Chat>()
                    ChatScreen(
                        conversationId = args.conversationId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
                composable<NavigationRoute.Media> {
                    MediaVaultScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onOpenPhoto = { photo ->
                            navController.navigate(
                                NavigationRoute.PhotoViewer(
                                    caption = photo.caption,
                                    moodName = photo.moodName,
                                    senderName = photo.senderName,
                                    timestamp = photo.timestamp,
                                ),
                            )
                        },
                        onOpenStorage = {
                            navController.navigate(NavigationRoute.Storage)
                        },
                    )
                }
                composable<NavigationRoute.PhotoViewer> { backStackEntry ->
                    val args = backStackEntry.toRoute<NavigationRoute.PhotoViewer>()
                    PhotoViewerScreen(
                        caption = args.caption,
                        moodName = args.moodName,
                        senderName = args.senderName,
                        timestamp = args.timestamp,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onShareClick = {},
                    )
                }
                composable<NavigationRoute.Storage> {
                    StorageScreen(
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
                composable<NavigationRoute.Settings> {
                    SettingsScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
                composable<NavigationRoute.Letters> {
                    LettersScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
                composable<NavigationRoute.Journal> {
                    JournalScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onOpenSearch = {
                            navController.navigate(NavigationRoute.Search())
                        },
                    )
                }
                composable<NavigationRoute.Search> { backStackEntry ->
                    val args = backStackEntry.toRoute<NavigationRoute.Search>()
                    SearchScreen(
                        conversationId = args.conversationId,
                        conversationName = args.conversationName,
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
                composable<NavigationRoute.Call> {
                    val viewModel: CallViewModel = koinViewModel()
                    CallScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                    )
                }
                composable<NavigationRoute.CallHistory> {
                    val viewModel: CallHistoryViewModel = koinViewModel()
                    CallHistoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onStartCall = {
                            navController.navigate(NavigationRoute.Call)
                        },
                    )
                }
                composable<NavigationRoute.NewGroup> {
                    NewGroupScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onCreateGroupSuccess = { conversationId ->
                            navController.navigate(NavigationRoute.Chat(conversationId)) {
                                popUpTo<NavigationRoute.NewGroup> { inclusive = true }
                            }
                        },
                    )
                }
                composable<NavigationRoute.LocationPicker> { backStackEntry ->
                    val args = backStackEntry.toRoute<NavigationRoute.LocationPicker>()
                    LocationPickerScreen(
                        conversationId = args.conversationId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSendLocation = {
                            navController.popBackStack()
                        },
                        onStartLiveSession = { session ->
                            navController.navigate(NavigationRoute.LiveLocation(session.sessionId)) {
                                popUpTo<NavigationRoute.LocationPicker> { inclusive = true }
                            }
                        },
                    )
                }
                composable<NavigationRoute.LiveLocation> { backStackEntry ->
                    val args = backStackEntry.toRoute<NavigationRoute.LiveLocation>()
                    LiveLocationScreen(
                        sessionId = args.sessionId,
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
