package com.uzuu.diuchat.feature.chat.domain.model

data class GroupMember(
    val userId: String,
    val displayName: String,
    val role: GroupRole = GroupRole.MEMBER,
    val mood: MoodType = MoodType.BINHYEN,
    val isFromMe: Boolean = false,
)
