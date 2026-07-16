package com.statusmaker.videoapp.video

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.statusmaker.videoapp.data.model.MusicStyle
import kotlinx.coroutines.*

/**
 * Streams synthesized audio via AudioTrack during preview.
 * Generates one bar-exact groove cycle (a whole number of musical phrases,
 * ~14-25 s depending on tempo), then loops it indefinitely using
 * AudioTrack.setLoopPoints() for zero-copy gapless repetition. Using the
 * phrase-aligned loop keeps fills/sections intact — the old fixed 4 s buffer
 * cut phrases off mid-bar, which sounded broken.
 */
class PreviewAudioPlayer(
    private val style: MusicStyle
) {
    companion object {
        private const val TAG = "PreviewAudioPlayer"
        private const val AUDIO_CHANNELS = 2   // stereo — see AudioSynthesizer.generate()
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
                // Generate one loopable chunk — stereo interleaved [L,R,L,R,...]
                val samples = AudioSynthesizer.generateLoop(style)
                val byteCount = samples.size * 2   // 16-bit = 2 bytes/sample

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
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
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                // Loop the entire buffer indefinitely (-1 = infinite).
                // setLoopPoints() takes FRAMES — for stereo 16-bit that is
                // half the interleaved short-array length.
                track.setLoopPoints(0, samples.size / AUDIO_CHANNELS, -1)

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
