package com.uzuu.diuchat.feature.location.domain.model

private const val MINUTES_15 = 15
private const val MINUTES_60 = 60
private const val MINUTES_480 = 480

enum class LiveDuration(val minutes: Int, val label: String) {
    FIFTEEN_MINUTES(MINUTES_15, "15 phút"),
    ONE_HOUR(MINUTES_60, "1 giờ"),
    EIGHT_HOURS(MINUTES_480, "8 giờ"),
}
