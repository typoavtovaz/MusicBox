package com.margy.musicbox.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.margy.musicbox.core.model.Source
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
 * Каждый трек готовится с заголовками сервиса — иначе Яндекс отдаёт 403.
 */
object PlayerEngine {

    private var player: ExoPlayer? = null
    private var dataSource: DefaultHttpDataSource.Factory? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ticker: Job? = null

    private val _state = MutableStateFlow(PlaybackUi())
    val state: StateFlow<PlaybackUi> = _state

    fun init(context: Context) {
        if (player != null) return

        val ds = DefaultHttpDataSource.Factory()
            .setUserAgent(
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/124.0.0.0 Mobile Safari/537.36"
            )
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(25_000)
            .setDefaultRequestProperties(
                mapOf(
                    "X-Yandex-Music-Client" to "YandexMusicAndroid/24023621",
                    "Accept-Language" to "ru"
                )
            )
        dataSource = ds

        val p = ExoPlayer.Builder(context.applicationContext).build()
        p.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(playing = isPlaying) }
                if (isPlaying) AudioReactive.attach(p.audioSessionId)
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
                    it.copy(
                        loading = false,
                        playing = false,
                        error = "поток не играет: ${error.errorCodeName}"
                    )
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
                        it.copy(
                            positionMs = exo.currentPosition,
                            durationMs = exo.duration.coerceAtLeast(0L)
                        )
                    }
                }
            }
        }
    }

    suspend fun play(track: Track) {
        val exo = player ?: return
        _state.update {
            it.copy(
                track = track,
                loading = true,
                error = null,
                positionMs = 0L,
                durationMs = track.durationMs,
                playing = false
            )
        }

        val url = try {
            ProviderRegistry.streamUrl(track)
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.message ?: "нет стрима") }
            return
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

        val source = ProgressiveMediaSource.Factory(dataSource!!)
            .createMediaSource(MediaItem.fromUri(url))

        withContext(Dispatchers.Main) {
            exo.setMediaSource(source)
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

    fun stop() {
        player?.stop()
        AudioReactive.detach()
    }

    fun seekTo(ms: Long) {
        player?.seekTo(ms)
    }
}
