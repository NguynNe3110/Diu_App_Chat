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
    data class Chat(val conversationId: String = "conv_linh_diu") : NavigationRoute

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
    data object AboutLibraries : NavigationRoute
}
