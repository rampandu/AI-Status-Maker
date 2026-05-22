package com.statusmaker.videoapp.video

import android.content.Context
import android.graphics.*
import android.media.*
import android.media.MediaCodecInfo.CodecProfileLevel
import android.net.Uri
import android.util.Log
import com.statusmaker.videoapp.data.model.*
import com.statusmaker.videoapp.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

sealed class VideoGenState {
    data class Progress(val percent: Int, val message: String) : VideoGenState()
    data class Success(val outputPath: String) : VideoGenState()
    data class Error(val message: String) : VideoGenState()
}

class VideoGenerator(private val context: Context) {

    companion object {
        private const val TAG = "VideoGenerator"
        const val VIDEO_WIDTH  = 720
        const val VIDEO_HEIGHT = 1280
        private const val FPS  = 30
        private const val VIDEO_BIT_RATE     = 3_000_000
        private const val I_FRAME_INTERVAL   = 1
        private const val AUDIO_SAMPLE_RATE  = AudioSynthesizer.SAMPLE_RATE
        private const val AUDIO_CHANNELS     = 1
        private const val AUDIO_BIT_RATE     = 128_000
        private const val AUDIO_FRAME_SAMPLES = 1024   // AAC-LC frame size
    }

    fun generateVideo(
        template: Template,
        userInput: UserInput,
        addWatermark: Boolean = true
    ): Flow<VideoGenState> = flow {

        try {
            emit(VideoGenState.Progress(5, "Preparing…"))

            val outputFile = FileUtils.getOutputVideoFile(context)
            val totalVideoFrames = template.durationSeconds * FPS

            // Load user photo once
            val userPhotoBitmap: Bitmap? = userInput.personPhotoUri?.let { uriStr ->
                try {
                    context.contentResolver.openInputStream(Uri.parse(uriStr))
                        ?.use { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) { null }
            }

            emit(VideoGenState.Progress(8, "Synthesizing music…"))

            // Pre-generate all audio samples for the video duration
            val musicSamples: ShortArray = AudioSynthesizer.generate(
                style           = userInput.musicStyle,
                durationSeconds = template.durationSeconds
            )
            val totalAudioSamples = musicSamples.size

            emit(VideoGenState.Progress(12, "Setting up encoders…"))

            // ── Video encoder ────────────────────────────────────────────
            val videoEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            videoEncoder.configure(
                MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC, VIDEO_WIDTH, VIDEO_HEIGHT
                ).also {
                    it.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                    it.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE)
                    it.setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
                    it.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
                }, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE
            )

            // ── Audio encoder ────────────────────────────────────────────
            val audioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            audioEncoder.configure(
                MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, AUDIO_CHANNELS
                ).also {
                    it.setInteger(MediaFormat.KEY_AAC_PROFILE,
                        CodecProfileLevel.AACObjectLC)
                    it.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                    it.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,
                        AUDIO_FRAME_SAMPLES * 2 * AUDIO_CHANNELS)
                }, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE
            )

            val muxer = MediaMuxer(
                outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )

            var videoTrackIndex  = -1
            var audioTrackIndex  = -1
            var muxerStarted     = false
            val videoInfo        = MediaCodec.BufferInfo()
            val audioInfo        = MediaCodec.BufferInfo()

            var videoFrameIndex    = 0
            var videoEos           = false
            var audioEos           = false
            var audioSamplesQueued = 0
            var audioInputDone     = false

            videoEncoder.start()
            audioEncoder.start()

            emit(VideoGenState.Progress(15, "Encoding…"))

            // ── Main encode loop ─────────────────────────────────────────
            while (!videoEos || !audioEos) {

                // ── Feed video frame ─────────────────────────────────────
                if (!videoEos && videoFrameIndex <= totalVideoFrames) {
                    val idx = videoEncoder.dequeueInputBuffer(10_000L)
                    if (idx >= 0) {
                        if (videoFrameIndex < totalVideoFrames) {
                            val ratio = videoFrameIndex.toFloat() / totalVideoFrames
                            val bmp = FrameRenderer.renderFrame(
                                context      = context,
                                template     = template,
                                userInput    = userInput,
                                userPhoto    = userPhotoBitmap,
                                frameIndex   = videoFrameIndex,
                                totalFrames  = totalVideoFrames,
                                progressRatio = ratio,
                                addWatermark = addWatermark,
                                width        = VIDEO_WIDTH,
                                height       = VIDEO_HEIGHT
                            )
                            val yuv = bitmapToYUV420(bmp)
                            videoEncoder.getInputBuffer(idx)!!.also {
                                it.clear(); it.put(yuv)
                            }
                            bmp.recycle()
                            videoEncoder.queueInputBuffer(
                                idx, 0, yuv.size,
                                videoFrameIndex * 1_000_000L / FPS, 0
                            )

                            val pct = 15 + (videoFrameIndex * 65 / totalVideoFrames)
                            if (videoFrameIndex % 15 == 0) {
                                emit(VideoGenState.Progress(pct,
                                    "Frame $videoFrameIndex / $totalVideoFrames"))
                            }
                        } else {
                            videoEncoder.getInputBuffer(idx)!!.clear()
                            videoEncoder.queueInputBuffer(
                                idx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        }
                        videoFrameIndex++
                    }
                }

                // ── Feed audio PCM chunk ──────────────────────────────────
                if (!audioInputDone) {
                    val idx = audioEncoder.dequeueInputBuffer(0L)
                    if (idx >= 0) {
                        if (audioSamplesQueued < totalAudioSamples) {
                            val count = minOf(AUDIO_FRAME_SAMPLES,
                                totalAudioSamples - audioSamplesQueued)
                            val buf = audioEncoder.getInputBuffer(idx)!!
                            buf.clear()
                            // Write shorts as bytes (little-endian PCM)
                            for (i in 0 until count) {
                                val s = musicSamples[audioSamplesQueued + i]
                                buf.put((s.toInt() and 0xFF).toByte())
                                buf.put(((s.toInt() shr 8) and 0xFF).toByte())
                            }
                            val pts = audioSamplesQueued * 1_000_000L / AUDIO_SAMPLE_RATE
                            audioEncoder.queueInputBuffer(idx, 0, count * 2, pts, 0)
                            audioSamplesQueued += count
                        } else {
                            audioEncoder.getInputBuffer(idx)!!.clear()
                            audioEncoder.queueInputBuffer(
                                idx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            audioInputDone = true
                        }
                    }
                }

                // ── Drain video output ────────────────────────────────────
                if (!videoEos) {
                    drainLoop@ while (true) {
                        val outIdx = videoEncoder.dequeueOutputBuffer(videoInfo, 0L)
                        when {
                            outIdx == MediaCodec.INFO_TRY_AGAIN_LATER -> break@drainLoop
                            outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                                if (audioTrackIndex >= 0 && !muxerStarted) {
                                    muxer.start(); muxerStarted = true
                                }
                            }
                            outIdx >= 0 -> {
                                val buf = videoEncoder.getOutputBuffer(outIdx)!!
                                val isConfig = videoInfo.flags and
                                        MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                                if (!isConfig && muxerStarted && videoTrackIndex >= 0) {
                                    muxer.writeSampleData(videoTrackIndex, buf, videoInfo)
                                }
                                videoEncoder.releaseOutputBuffer(outIdx, false)
                                if (videoInfo.flags and
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                    videoEos = true; break@drainLoop
                                }
                            }
                            else -> break@drainLoop
                        }
                    }
                }

                // ── Drain audio output ────────────────────────────────────
                if (!audioEos) {
                    drainLoop@ while (true) {
                        val outIdx = audioEncoder.dequeueOutputBuffer(audioInfo, 0L)
                        when {
                            outIdx == MediaCodec.INFO_TRY_AGAIN_LATER -> break@drainLoop
                            outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                audioTrackIndex = muxer.addTrack(audioEncoder.outputFormat)
                                if (videoTrackIndex >= 0 && !muxerStarted) {
                                    muxer.start(); muxerStarted = true
                                }
                            }
                            outIdx >= 0 -> {
                                val buf = audioEncoder.getOutputBuffer(outIdx)!!
                                val isConfig = audioInfo.flags and
                                        MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                                if (!isConfig && muxerStarted && audioTrackIndex >= 0) {
                                    muxer.writeSampleData(audioTrackIndex, buf, audioInfo)
                                }
                                audioEncoder.releaseOutputBuffer(outIdx, false)
                                if (audioInfo.flags and
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                    audioEos = true; break@drainLoop
                                }
                            }
                            else -> break@drainLoop
                        }
                    }
                }
            }

            emit(VideoGenState.Progress(90, "Finalizing…"))

            videoEncoder.stop();  videoEncoder.release()
            audioEncoder.stop();  audioEncoder.release()
            if (muxerStarted) muxer.stop()
            muxer.release()
            userPhotoBitmap?.recycle()

            emit(VideoGenState.Progress(100, "Done!"))
            emit(VideoGenState.Success(outputFile.absolutePath))

        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            emit(VideoGenState.Error(e.message ?: "Unknown error"))
        }

    }.flowOn(Dispatchers.IO)

    // ─── Bitmap → YUV420 ──────────────────────────────────────────────────────

    private fun bitmapToYUV420(bitmap: Bitmap): ByteArray {
        val w = bitmap.width;  val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val ySize  = w * h
        val uvSize = ySize / 4
        val yuv    = ByteArray(ySize + uvSize * 2)
        var yIdx = 0;  var uIdx = ySize;  var vIdx = ySize + uvSize

        for (j in 0 until h) {
            for (i in 0 until w) {
                val p = pixels[j * w + i]
                val r = (p shr 16) and 0xff
                val g = (p shr  8) and 0xff
                val b =  p         and 0xff
                yuv[yIdx++] = (((66*r+129*g+25*b+128) shr 8)+16).coerceIn(0,255).toByte()
                if (j%2==0 && i%2==0) {
                    yuv[uIdx++] = (((-38*r-74*g+112*b+128) shr 8)+128).coerceIn(0,255).toByte()
                    yuv[vIdx++] = (((112*r-94*g-18*b+128) shr 8)+128).coerceIn(0,255).toByte()
                }
            }
        }
        return yuv
    }
}
