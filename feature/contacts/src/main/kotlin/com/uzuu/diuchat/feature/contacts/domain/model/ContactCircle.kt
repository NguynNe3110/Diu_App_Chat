package com.uzuu.diuchat.feature.contacts.domain.model

enum class ContactCircle(
    val id: String,
    val label: String,
) {
    ALL("all", "Tất cả"),
    LOVE("love", "Người thương"),
    FAMILY("family", "Gia đình"),
    FRIENDS("friends", "Bạn thân"),
}
