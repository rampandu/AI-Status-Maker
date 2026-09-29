package com.statusmaker.videoapp.video

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.statusmaker.videoapp.data.model.MusicStyle
import kotlinx.coroutines.*

/**
 * Streams background audio for the live preview / audition screens.
 *
 * Two modes:
 *  - Procedural styles: generates one bar-exact groove loop via
 *    AudioSynthesizer and streams it through AudioTrack (unchanged from
 *    before — phrase-aligned so fills/sections stay intact).
 *  - MusicStyle.CUSTOM: plays the user's own picked song directly through
 *    MediaPlayer, looping. No PCM decode needed here — that only happens
 *    once, at export time, in CustomAudioDecoder — so previewing a custom
 *    song is as cheap as previewing any other audio file on the device.
 */
class PreviewAudioPlayer(
    private val style: MusicStyle,
    private val context: Context? = null,
    private val customAudioUri: Uri? = null
) {
    companion object {
        private const val TAG = "PreviewAudioPlayer"
        private const val AUDIO_CHANNELS = 2   // stereo — see AudioSynthesizer.generate()
    }

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var prepareJob: Job? = null
    private var ready = false

    /** Call once after template is known; fires [onReady] when playback can start. */
    fun prepare(scope: CoroutineScope, onReady: () -> Unit) {
        val ctx = context
        val uri = customAudioUri
        if (style == MusicStyle.CUSTOM && ctx != null && uri != null) {
            prepareCustom(ctx, uri, onReady)
        } else {
            prepareProcedural(scope, onReady)
        }
    }

    private fun prepareCustom(context: Context, uri: Uri, onReady: () -> Unit) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                isLooping = true
                setOnPreparedListener {
                    ready = true
                    onReady()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Custom audio playback error: what=$what extra=$extra")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Custom audio prepare failed: ${e.message}")
        }
    }

    private fun prepareProcedural(scope: CoroutineScope, onReady: () -> Unit) {
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
            mediaPlayer?.start() ?: audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "play failed: ${e.message}")
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun resume() {
        if (!ready) return
        try {
            mediaPlayer?.start() ?: audioTrack?.play()
        } catch (_: Exception) {}
    }

    fun release() {
        prepareJob?.cancel()
        try {
            mediaPlayer?.stop(); mediaPlayer?.release()
        } catch (_: Exception) {}
        try {
            audioTrack?.stop(); audioTrack?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        audioTrack = null
        ready = false
    }
}
