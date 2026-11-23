package com.uzuu.diuchat.app.presentation

import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.uzuu.diuchat.app.call.CallNotificationManager
import com.uzuu.diuchat.app.call.IncomingCallLaunchRequest
import com.uzuu.diuchat.app.presentation.util.NavigationDestinationLogger
import com.uzuu.diuchat.app.presentation.util.rememberIsOnline
import com.uzuu.diuchat.feature.auth.presentation.screen.AuthScreen
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonListSkeleton
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryViewModel
import com.uzuu.diuchat.feature.calls.presentation.screen.CallScreen
import com.uzuu.diuchat.feature.calls.presentation.screen.CallViewModel
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.usecase.DeleteMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.StarMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ToggleReactionUseCase
import com.uzuu.diuchat.feature.chat.presentation.screen.ChatScreen
import com.uzuu.diuchat.feature.chat.presentation.screen.NewGroupScreen
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetContactsUseCase
import com.uzuu.diuchat.feature.contacts.presentation.screen.ContactsScreen
import com.uzuu.diuchat.feature.home.presentation.HomeScreen
import com.uzuu.diuchat.feature.journal.presentation.favourites.FavouritesScreen
import com.uzuu.diuchat.feature.journal.presentation.journal.JournalScreen
import com.uzuu.diuchat.feature.journal.presentation.search.SearchScreen
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.usecase.GetLettersUseCase
import com.uzuu.diuchat.feature.letters.presentation.screen.LettersScreen
import com.uzuu.diuchat.feature.location.presentation.LiveLocationScreen
import com.uzuu.diuchat.feature.location.presentation.LocationPickerScreen
import com.uzuu.diuchat.feature.media.presentation.storage.StorageScreen
import com.uzuu.diuchat.feature.media.presentation.vault.MediaVaultScreen
import com.uzuu.diuchat.feature.media.presentation.viewer.PhotoViewerScreen
import com.uzuu.diuchat.feature.onboarding.presentation.screen.OnBoardingScreen
import com.uzuu.diuchat.feature.settings.presentation.screen.SettingsScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainShowcaseScreen(
    modifier: Modifier = Modifier,
    sessionViewModel: AppSessionViewModel = koinViewModel(),
    pendingIncomingCall: IncomingCallLaunchRequest? = null,
    onConsumePendingIncomingCall: () -> Unit = {},
    onCallLockScreenVisibilityChange: (Boolean) -> Unit = {},
) {
    // tạo và lưu trữ trạng thái của NavController
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isOnline by rememberIsOnline()
    val startupDestination by sessionViewModel.startupDestination.collectAsState()
    val callRepository: CallRepository = koinInject()
    val currentCallSession by callRepository.currentCallSession.collectAsState()
    val context = LocalContext.current
    val currentOnConsumePendingIncomingCall by rememberUpdatedState(onConsumePendingIncomingCall)
    val currentOnCallLockScreenVisibilityChange by rememberUpdatedState(onCallLockScreenVisibilityChange)

    LaunchedEffect(currentCallSession?.status) {
        val status = currentCallSession?.status
        val isLiveCall = status == CallStatus.RINGING ||
            status == CallStatus.INVITING ||
            status == CallStatus.CONNECTING ||
            status == CallStatus.ACTIVE
        currentOnCallLockScreenVisibilityChange(isLiveCall)
        if (status != null && status != CallStatus.RINGING) {
            CallNotificationManager.cancelIncomingCallNotification(context)
        }
    }

    LaunchedEffect(startupDestination, pendingIncomingCall) {
        val request = pendingIncomingCall ?: return@LaunchedEffect
        if (startupDestination !is StartupDestination.Home) return@LaunchedEffect

        CallNotificationManager.cancelIncomingCallNotification(context)
        val incomingSession = request.payload.toCallSession()
        val current = callRepository.currentCallSession.value
        val isDifferentLiveCall = current != null &&
            current.status != CallStatus.IDLE &&
            current.status != CallStatus.ENDED &&
            current.callId != incomingSession.callId

        if (!isDifferentLiveCall) {
            callRepository.onIncomingCallReceived(incomingSession)
            if (request.autoAccept) {
                callRepository.acceptCall(incomingSession.callId)
            }
            val isAlreadyOnCall = navController.currentDestination?.hasRoute<NavigationRoute.Call>() == true
            if (!isAlreadyOnCall) {
                navController.navigate(
                    NavigationRoute.Call(
                        callId = incomingSession.callId,
                        conversationId = incomingSession.conversationId,
                        peerId = incomingSession.callerId,
                        peerName = incomingSession.callerName,
                        peerMoodHex = incomingSession.peerMoodHex,
                        isVideo = incomingSession.type == CallType.VIDEO,
                        isIncoming = true,
                    ),
                ) {
                    launchSingleTop = true
                }
            }
        }
        currentOnConsumePendingIncomingCall()
    }

    LaunchedEffect(startupDestination) {
        if (startupDestination is StartupDestination.Home) {
            callRepository.observeIncomingCalls().collect { incomingSession ->
                val current = callRepository.currentCallSession.value
                val isBusy = current != null &&
                    current.status != CallStatus.IDLE &&
                    current.status != CallStatus.ENDED
                if (!isBusy) {
                    CallNotificationManager.cancelIncomingCallNotification(context)
                    callRepository.onIncomingCallReceived(incomingSession)
                    val isAlreadyOnCall = navController.currentDestination?.hasRoute<NavigationRoute.Call>() == true
                    if (!isAlreadyOnCall) {
                        navController.navigate(
                            NavigationRoute.Call(
                                callId = incomingSession.callId,
                                conversationId = incomingSession.conversationId,
                                peerId = incomingSession.callerId,
                                peerName = incomingSession.callerName,
                                peerMoodHex = incomingSession.peerMoodHex,
                                isVideo = incomingSession.type == CallType.VIDEO,
                                isIncoming = true,
                            ),
                        ) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

    if (BuildConfig.DEBUG) {
        addOnDestinationChangedListener(navController)
    }

    val destination = backStackEntry?.destination
    val isStartupLoading = startupDestination is StartupDestination.Loading
    val hideBottomBar =
        isStartupLoading ||
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
                    )
                    .consumeWindowInsets(innerPadding),
            ) {
                DiuOfflineBanner(visible = !isOnline)

                val resolvedStart = startupDestination
                if (resolvedStart is StartupDestination.Loading) {
                    DiuPersonListSkeleton(
                        itemCount = 6,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 24.dp),
                    )
                } else {
                    val startRoute: NavigationRoute = when (resolvedStart) {
                        StartupDestination.OnBoarding -> NavigationRoute.OnBoarding
                        is StartupDestination.Auth -> NavigationRoute.Auth
                        StartupDestination.Home -> NavigationRoute.Home
                        StartupDestination.Loading -> NavigationRoute.OnBoarding
                    }
                    val requireUsernameSetup = (resolvedStart as? StartupDestination.Auth)?.requireUsernameSetup == true

                    NavHost(
                        navController = navController,
                        startDestination = startRoute,
                        modifier = Modifier.weight(1f),
                    ) {
                        composable<NavigationRoute.OnBoarding> {
                            OnBoardingScreen(
                                onFinish = {
                                    sessionViewModel.markOnboardingCompleted()
                                    navController.navigate(NavigationRoute.Auth) {
                                        popUpTo<NavigationRoute.OnBoarding> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        composable<NavigationRoute.Auth> {
                            AuthScreen(
                                requireUsernameSetup = requireUsernameSetup,
                                onAuthSuccess = {
                                    sessionViewModel.onAuthenticated()
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
                                onStartCall = { peerId, peerName, peerMoodHex ->
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
                            val pendingReplyMessageId by backStackEntry.savedStateHandle
                                .getStateFlow<String?>("reply_message_id", null)
                                .collectAsState()
                            ChatScreen(
                                conversationId = args.conversationId,
                                pendingReplyMessageId = pendingReplyMessageId,
                                onConsumePendingReply = {
                                    backStackEntry.savedStateHandle.remove<String>("reply_message_id")
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onStartVoiceCall = { calleeId, conversationId, peerName, peerMoodHex ->
                                    navController.navigate(
                                        NavigationRoute.Call(
                                            conversationId = conversationId,
                                            peerId = calleeId,
                                            peerName = peerName,
                                            peerMoodHex = peerMoodHex,
                                            isVideo = false,
                                        ),
                                    )
                                },
                                onStartVideoCall = { calleeId, conversationId, peerName, peerMoodHex ->
                                    navController.navigate(
                                        NavigationRoute.Call(
                                            conversationId = conversationId,
                                            peerId = calleeId,
                                            peerName = peerName,
                                            peerMoodHex = peerMoodHex,
                                            isVideo = true,
                                        ),
                                    )
                                },
                                onOpenPhotoViewer = { imageUrl, messageId, convoId, caption, moodName, senderName, timestamp ->
                                    navController.navigate(
                                        NavigationRoute.PhotoViewer(
                                            imageUrl = imageUrl,
                                            messageId = messageId,
                                            conversationId = convoId,
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
                        composable<NavigationRoute.Media> { backStackEntry ->
                            val args = backStackEntry.toRoute<NavigationRoute.Media>()
                            MediaVaultScreen(
                                conversationId = args.conversationId,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onOpenPhoto = { photo ->
                                    navController.navigate(
                                        NavigationRoute.PhotoViewer(
                                            imageUrl = photo.url,
                                            messageId = photo.id,
                                            conversationId = photo.conversationId,
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
                            val deleteMessageUseCase: DeleteMessageUseCase = koinInject()
                            val starMessageUseCase: StarMessageUseCase = koinInject()
                            val toggleReactionUseCase: ToggleReactionUseCase = koinInject()
                            val coroutineScope = rememberCoroutineScope()
                            val viewerContext = LocalContext.current

                            PhotoViewerScreen(
                                imageUrl = args.imageUrl,
                                caption = args.caption,
                                moodName = args.moodName,
                                senderName = args.senderName,
                                timestamp = args.timestamp,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onShareClick = {
                                    val shareText = listOf(args.caption, args.imageUrl)
                                        .filter { it.isNotBlank() }
                                        .joinToString("\n")
                                    if (shareText.isNotBlank()) {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        viewerContext.startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh"))
                                    }
                                },
                                onReactionClick = { moodName ->
                                    if (args.messageId.isNotBlank()) {
                                        val mood = when (moodName) {
                                            "Thương" -> MoodType.THUONG
                                            "Vui" -> MoodType.VUI
                                            "Nhớ" -> MoodType.NHO
                                            "Trầm" -> MoodType.TRAM
                                            else -> MoodType.BINHYEN
                                        }
                                        coroutineScope.launch {
                                            toggleReactionUseCase(args.messageId, mood)
                                        }
                                    }
                                },
                                onReplyClick = {
                                    val prevEntry = navController.previousBackStackEntry
                                    if (prevEntry?.destination?.hasRoute<NavigationRoute.Chat>() == true) {
                                        if (args.messageId.isNotBlank()) {
                                            prevEntry.savedStateHandle["reply_message_id"] = args.messageId
                                        }
                                        navController.popBackStack()
                                    } else if (args.conversationId.isNotBlank()) {
                                        navController.navigate(NavigationRoute.Chat(args.conversationId)) {
                                            popUpTo<NavigationRoute.PhotoViewer> { inclusive = true }
                                        }
                                        if (args.messageId.isNotBlank()) {
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "reply_message_id",
                                                args.messageId,
                                            )
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                },
                                onSaveMemoryClick = {
                                    if (args.messageId.isNotBlank()) {
                                        coroutineScope.launch {
                                            starMessageUseCase(args.messageId)
                                        }
                                    }
                                },
                                onDeleteClick = {
                                    if (args.messageId.isNotBlank()) {
                                        coroutineScope.launch {
                                            deleteMessageUseCase(args.messageId)
                                            navController.popBackStack()
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                },
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
                            val getLettersUseCase: GetLettersUseCase = koinInject()
                            val getContactsUseCase: GetContactsUseCase = koinInject()
                            val letters by getLettersUseCase().collectAsState(initial = emptyList())
                            val contacts by getContactsUseCase().collectAsState(initial = emptyList())
                            val pendingLettersCount =
                                letters.count {
                                    it.status == LetterStatus.SEALED_PENDING || it.status == LetterStatus.READY_TO_OPEN
                                }
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
                                onLoggedOut = {
                                    sessionViewModel.onLoggedOut()
                                    navController.navigate(NavigationRoute.Auth) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                pendingLettersCount = pendingLettersCount,
                                closeCircleCount = contacts.size,
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
                                callId = args.callId,
                                conversationId = args.conversationId,
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
