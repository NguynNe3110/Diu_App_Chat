package com.uzuu.diuchat.feature.chat.domain.model

enum class MessageStatus(
    val value: String,
) {
    PENDING("Pending"),
    UPLOADING("Uploading"),
    SENT("Sent"),
    DELIVERED("Delivered"),
    READ("Read"),
    FAILED("Failed"),
    ;

    companion object {
        fun fromValue(value: String?): MessageStatus = entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
    }
}
