package com.uzuu.diuchat.feature.album.domain.model

import com.uzuu.diuchat.feature.album.domain.enum.ImageSize

internal data class Image(
    val url: String,
    val size: ImageSize,
)
