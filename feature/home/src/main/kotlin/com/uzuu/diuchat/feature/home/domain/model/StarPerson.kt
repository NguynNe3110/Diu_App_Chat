package com.uzuu.diuchat.feature.home.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StarPerson(
    val id: String,
    val name: String,
    val initials: String,
    val moodColor: Color,
    val size: Dp = 48.dp,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val isQuiet: Boolean = false,
)
