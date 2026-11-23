package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack

@Composable
fun LiveKitVideoView(
    videoTrack: VideoTrack,
    onInitRenderer: (TextureViewRenderer) -> Unit,
    modifier: Modifier = Modifier,
    isMirror: Boolean = false,
) {
    val context = LocalContext.current
    val renderer = remember {
        TextureViewRenderer(context).also { view ->
            onInitRenderer(view)
            view.setMirror(isMirror)
        }
    }

    DisposableEffect(renderer, isMirror) {
        renderer.setMirror(isMirror)
        onDispose {}
    }

    DisposableEffect(videoTrack, renderer) {
        videoTrack.addRenderer(renderer)
        onDispose {
            runCatching { videoTrack.removeRenderer(renderer) }
        }
    }

    DisposableEffect(renderer) {
        onDispose {
            runCatching { renderer.release() }
        }
    }

    AndroidView(
        factory = { renderer },
        modifier = modifier,
    )
}
