package com.margy.musicbox.playback

import android.content.Context
import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Читает реальный спектр того, что играет, и отдаёт 28 полос для волны из точек.
 * Через android.media.audiofx.Visualizer — это и есть бас/низы/вокал/верха.
 */
object AudioReactive {

    private val _levels = MutableStateFlow(FloatArray(0))
    val levels: StateFlow<FloatArray> = _levels

    private var visualizer: Visualizer? = null
    private var attachedSession = -1
    private var smoothed = FloatArray(BANDS)

    @Volatile
    var permissionGranted: Boolean = false

    fun attach(sessionId: Int) {
        if (!permissionGranted) return
        if (sessionId == 0 || sessionId == Visualizer.ERROR) return
        if (visualizer != null && attachedSession == sessionId) return

        detach()
        runCatching {
            val v = Visualizer(sessionId)
            val size = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
            v.captureSize = size
            v.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        vis: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) = Unit

                    override fun onFftDataCapture(
                        vis: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft == null) return
                        _levels.value = bands(fft)
                    }
                },
                Visualizer.getMaxCaptureRate() / 2,
                false,
                true
            )
            v.enabled = true
            visualizer = v
            attachedSession = sessionId
        }
    }

    fun detach() {
        runCatching { visualizer?.enabled = false }
        runCatching { visualizer?.release() }
        visualizer = null
        attachedSession = -1
        _levels.value = FloatArray(0)
    }

    /** FFT сгруппирован в BANDS полос: слева бас, справа верха. */
    private fun bands(fft: ByteArray): FloatArray {
        val bins = fft.size / 2
        val out = FloatArray(BANDS)

        for (i in 0 until BANDS) {
            // логарифмическая группировка: низы получают больше разрешения
            val start = (bins * (i.toFloat() / BANDS).let { it * it }).toInt().coerceAtLeast(1)
            val end = (bins * (((i + 1f) / BANDS).let { it * it })).toInt().coerceAtLeast(start + 1)

            var sum = 0f
            for (k in start until end.coerceAtMost(bins)) {
                val re = fft[2 * k].toFloat()
                val im = fft[2 * k + 1].toFloat()
                sum += sqrt(re * re + im * im)
            }
            val mag = sum / (end - start).coerceAtLeast(1)
            // сжатие логом, чтоб тихие верха тоже дёргались
            val norm = (ln(1f + mag) / ln(1f + 110f)).coerceIn(0f, 1f)
            out[i] = norm
        }

        // плавность: убираем дрожание между кадрами
        for (i in 0 until BANDS) {
            smoothed[i] = smoothed[i] * 0.55f + out[i] * 0.45f
            out[i] = smoothed[i]
        }
        return out
    }

    const val BANDS = 28
}
