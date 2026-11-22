package com.uzuu.diuchat.app.presentation

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.uzuu.diuchat.app.presentation.util.rememberIsOnline
import com.uzuu.diuchat.feature.auth.presentation.screen.AuthScreen
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryViewModel
import com.uzuu.diuchat.feature.calls.presentation.screen.CallScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallViewModel
import com.uzuu.diuchat.feature.chat.presentation.screen.ChatScreen
import com.uzuu.diuchat.feature.chat.presentation.screen.NewGroupScreen
import com.uzuu.diuchat.feature.contacts.presentation.screen.ContactsScreen
import com.uzuu.diuchat.feature.home.presentation.HomeScreen
import com.uzuu.diuchat.feature.journal.presentation.favourites.FavouritesScreen
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
    val isOnline by rememberIsOnline()

    if (BuildConfig.DEBUG) {
        addOnDestinationChangedListener(navController)
    }

    val destination = backStackEntry?.destination
    val hideBottomBar =
        destination?.hasRoute<NavigationRoute.OnBoarding>() == true ||
            destination?.hasRoute<NavigationRoute.Auth>() == true ||
            destination?.hasRoute<NavigationRoute.Chat>() == true ||
            destination?.hasRoute<NavigationRoute.Call>() == true ||
            destination?.hasRoute<NavigationRoute.PhotoViewer>() == true ||
            destination?.hasRoute<NavigationRoute.LocationPicker>() == true ||
            destination?.hasRoute<NavigationRoute.LiveLocation>() == true ||
            destination?.hasRoute<NavigationRoute.NewGroup>() == true ||
            destination?.hasRoute<NavigationRoute.Search>() == true ||
            destination?.hasRoute<NavigationRoute.Letters>() == true ||
            destination?.hasRoute<NavigationRoute.Journal>() == true ||
            destination?.hasRoute<NavigationRoute.Media>() == true ||
            destination?.hasRoute<NavigationRoute.Storage>() == true ||
            destination?.hasRoute<NavigationRoute.Favourites>() == true ||
            destination?.hasRoute<NavigationRoute.AboutLibraries>() == true

    DiuAmbientBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
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
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (hideBottomBar) innerPadding.calculateBottomPadding() else 0.dp,
                    ),
            ) {
                DiuOfflineBanner(visible = !isOnline)

                NavHost(
                    navController = navController,
                    // ponytail: luôn mở onboarding; lưu cờ hoàn tất khi cần chỉ hiện lần đầu.
                    startDestination = NavigationRoute.OnBoarding,
                    modifier = Modifier.weight(1f),
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
                            onSearchClick = {
                                navController.navigate(NavigationRoute.Search())
                            },
                        )
                    }
                    composable<NavigationRoute.Contacts> {
                        ContactsScreen(
                            onNavigateToChat = { conversationId ->
                                navController.navigate(NavigationRoute.Chat(conversationId))
                            },
                            onStartCall = { peerId ->
                                val (peerName, peerMoodHex) = resolveContactCallMetadata(peerId)
                                navController.navigate(
                                    NavigationRoute.Call(
                                        peerId = peerId,
                                        peerName = peerName,
                                        peerMoodHex = peerMoodHex,
                                        isVideo = false,
                                    ),
                                )
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
                            onStartVoiceCall = { peerId, peerName, peerMoodHex ->
                                navController.navigate(
                                    NavigationRoute.Call(
                                        peerId = peerId,
                                        peerName = peerName,
                                        peerMoodHex = peerMoodHex,
                                        isVideo = false,
                                    ),
                                )
                            },
                            onStartVideoCall = { peerId, peerName, peerMoodHex ->
                                navController.navigate(
                                    NavigationRoute.Call(
                                        peerId = peerId,
                                        peerName = peerName,
                                        peerMoodHex = peerMoodHex,
                                        isVideo = true,
                                    ),
                                )
                            },
                            onOpenPhotoViewer = { caption, moodName, senderName, timestamp ->
                                navController.navigate(
                                    NavigationRoute.PhotoViewer(
                                        caption = caption,
                                        moodName = moodName,
                                        senderName = senderName,
                                        timestamp = timestamp,
                                    ),
                                )
                            },
                            onAttachLocationClick = {
                                navController.navigate(NavigationRoute.LocationPicker(args.conversationId))
                            },
                            onSendDelayedLetterClick = {
                                navController.navigate(NavigationRoute.Letters)
                            },
                            onSearchInChatClick = {
                                navController.navigate(NavigationRoute.Search(conversationId = args.conversationId))
                            },
                            onOpenSharedMediaClick = {
                                navController.navigate(NavigationRoute.Media(args.conversationId))
                            },
                            onOpenPendingLettersClick = {
                                navController.navigate(NavigationRoute.Letters)
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
                    composable<NavigationRoute.Settings> {
                        SettingsScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToStorage = {
                                navController.navigate(NavigationRoute.Storage)
                            },
                            onNavigateToJournal = {
                                navController.navigate(NavigationRoute.Journal)
                            },
                            onNavigateToLetters = {
                                navController.navigate(NavigationRoute.Letters)
                            },
                            onNavigateToContacts = {
                                navController.navigate(NavigationRoute.Contacts)
                            },
                            onNavigateToFavourites = {
                                navController.navigate(NavigationRoute.Favourites)
                            },
                            onNavigateToOnBoarding = {
                                navController.navigate(NavigationRoute.OnBoarding)
                            },
                            onReplayOnboarding = {
                                navController.navigate(NavigationRoute.OnBoarding)
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
                    composable<NavigationRoute.Call> { backStackEntry ->
                        val args = backStackEntry.toRoute<NavigationRoute.Call>()
                        val viewModel: CallViewModel = koinViewModel()
                        CallScreen(
                            viewModel = viewModel,
                            peerId = args.peerId,
                            peerName = args.peerName,
                            peerMoodHex = args.peerMoodHex,
                            isVideo = args.isVideo,
                            isIncoming = args.isIncoming,
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
                            onStartCall = { peerId, peerName, peerMoodHex, isVideo ->
                                navController.navigate(
                                    NavigationRoute.Call(
                                        peerId = peerId,
                                        peerName = peerName,
                                        peerMoodHex = peerMoodHex,
                                        isVideo = isVideo,
                                    ),
                                )
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
                    composable<NavigationRoute.Favourites> {
                        FavouritesScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                        )
                    }
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
}

@Composable
private fun DiuOfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppPalette.Danger.copy(alpha = 0.14f),
        ) {
            Text(
                text = "Đang ngoại tuyến · Tin nhắn sẽ được gửi khi có mạng",
                color = AppTheme.colors.ink,
                style = AppTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun resolveContactCallMetadata(peerId: String): Pair<String, String> = when (peerId) {
    "c1", "minh", "conv_linh_diu" -> "Minh" to "#F7A3A9"
    "c2", "me" -> "Mẹ" to "#F4CA84"
    "c3", "vy", "thao" -> "Bảo Vy" to "#A2D7AC"
    "c4", "ngoai" -> "Bà Ngoại" to "#C5B3EA"
    "c5", "ba" -> "Ba" to "#95C4E7"
    else -> peerId.replaceFirstChar { it.uppercase() } to "#F7A3A9"
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
