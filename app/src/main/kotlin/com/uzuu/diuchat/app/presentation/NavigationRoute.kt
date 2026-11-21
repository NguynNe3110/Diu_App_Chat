package com.uzuu.diuchat.app.presentation

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable
    data object OnBoarding : NavigationRoute

    @Serializable
    data object Auth : NavigationRoute

    @Serializable
    data object Home : NavigationRoute

    @Serializable
    data object Contacts : NavigationRoute

    @Serializable
    data class Chat(
        val conversationId: String = "conv_linh_diu",
    ) : NavigationRoute

    @Serializable
    data class Media(
        val conversationId: String = "minh",
    ) : NavigationRoute

    @Serializable
    data class PhotoViewer(
        val caption: String = "",
        val moodName: String = "",
        val senderName: String = "",
        val timestamp: String = "",
    ) : NavigationRoute

    @Serializable
    data object Storage : NavigationRoute

    @Serializable
    data object AlbumList : NavigationRoute

    @Serializable
    data class AlbumDetail(
        val albumName: String,
        val artistName: String,
        val albumMbId: String?,
    ) : NavigationRoute

    @Serializable
    data object Favourites : NavigationRoute

    @Serializable
    data object Settings : NavigationRoute

    @Serializable
    data object Journal : NavigationRoute

    @Serializable
    data class Search(
        val conversationId: String? = null,
        val conversationName: String? = null,
    ) : NavigationRoute

    @Serializable
    data object AboutLibraries : NavigationRoute
}
