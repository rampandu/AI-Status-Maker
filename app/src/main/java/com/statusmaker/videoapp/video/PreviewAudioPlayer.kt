package com.statusmaker.videoapp.video

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.statusmaker.videoapp.data.model.MusicStyle
import kotlinx.coroutines.*

/**
 * Streams synthesized audio via AudioTrack during preview and export.
 * Generates one pattern loop (~4s), then loops it indefinitely using
 * AudioTrack.setLoopPoints() for zero-copy gapless repetition.
 */
class PreviewAudioPlayer(
    private val style: MusicStyle
) {
    companion object {
        private const val TAG = "PreviewAudioPlayer"
        private const val LOOP_DURATION_SEC = 4   // generate 4 s and loop
    }

    private var audioTrack: AudioTrack? = null
    private var prepareJob: Job? = null
    private var ready = false

    /**
     * Pre-generate samples and load into AudioTrack (static mode).
     * Call once after template is known; fires [onReady] when playback can start.
     */
    fun prepare(scope: CoroutineScope, onReady: () -> Unit) {
        prepareJob = scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = AudioSynthesizer.SAMPLE_RATE
                // Generate one loopable chunk
                val samples = AudioSynthesizer.generate(style, LOOP_DURATION_SEC)
                val byteCount = samples.size * 2   // 16-bit = 2 bytes/sample

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufSize = maxOf(minBuf, byteCount)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                // Loop the entire buffer indefinitely (-1 = infinite)
                track.setLoopPoints(0, samples.size, -1)

                audioTrack = track
                ready = true

                withContext(Dispatchers.Main) { onReady() }
            } catch (e: Exception) {
                Log.e(TAG, "prepare failed: ${e.message}")
            }
        }
    }

    fun play() {
        if (!ready) return
        try {
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "play failed: ${e.message}")
        }
    }

    fun pause() {
        try { audioTrack?.pause() } catch (_: Exception) {}
    }

    fun resume() {
        try { if (ready) audioTrack?.play() } catch (_: Exception) {}
    }

    fun release() {
        prepareJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        ready = false
    }
}
