package com.uzuu.diuchat.feature.chat.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val mood: MoodType,
    val lastMessage: String,
    val lastMessageTimeEpochMs: Long,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val avatarUrl: String? = null,
    val pinnedMessageId: String? = null,
    val pinnedMessageSnippet: String? = null,
    val daysTogether: Int? = null,
    val anniversaryDate: String? = null,
    val customNickname: String? = null,
    val isMuted: Boolean = false,
    val members: List<GroupMember> = emptyList(),
) {
    val directPeerId: String?
        get() = if (isGroup) null else members.firstOrNull { !it.isFromMe }?.userId
}
