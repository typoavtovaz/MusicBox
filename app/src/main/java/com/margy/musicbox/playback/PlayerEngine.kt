package com.margy.musicbox.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaybackUi(
    val track: Track? = null,
    val playing: Boolean = false,
    val loading: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)

/**
 * Один ExoPlayer на всё приложение. Живёт в процессе, состояние — в StateFlow.
 */
object PlayerEngine {

    private var player: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ticker: Job? = null

    private val _state = MutableStateFlow(PlaybackUi())
    val state: StateFlow<PlaybackUi> = _state

    fun init(context: Context) {
        if (player != null) return

        val p = ExoPlayer.Builder(context.applicationContext).build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(playing = isPlaying) }
                if (isPlaying) {
                    AudioReactive.attach(p.audioSessionId)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        loading = playbackState == Player.STATE_BUFFERING,
                        durationMs = p.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(loading = false, error = "поток не играет: ${error.errorCodeName}")
                }
            }
        })
        player = p

        ticker = scope.launch {
            while (true) {
                delay(350)
                val exo = player ?: continue
                if (exo.isPlaying) {
                    _state.update {
                        it.copy(positionMs = exo.currentPosition, durationMs = exo.duration.coerceAtLeast(0L))
                    }
                }
            }
        }
    }

    suspend fun play(track: Track) {
        val exo = player ?: return
        _state.update { it.copy(track = track, loading = true, error = null, positionMs = 0L, durationMs = track.durationMs) }

        val url = try {
            ProviderRegistry.streamUrl(track)
        } catch (e: Exception) {
            null
        }

        if (url.isNullOrBlank()) {
            _state.update {
                it.copy(
                    loading = false,
                    error = "не нашёл аудиопоток (${track.source.title})"
                )
            }
            return
        }

        withContext(Dispatchers.Main) {
            exo.setMediaItem(MediaItem.fromUri(url))
            exo.prepare()
            exo.play()
        }
    }

    fun toggle() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun pause() {
        player?.pause()
    }

    fun seekTo(ms: Long) {
        player?.seekTo(ms)
    }
}
