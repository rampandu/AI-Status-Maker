package com.statusmaker.videoapp.video

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import java.nio.ByteOrder
import kotlin.math.roundToInt

/**
 * Decodes a user-picked song (any format the device's MediaCodec supports —
 * MP3, AAC, OGG, WAV, FLAC…) into the exact PCM contract AudioSynthesizer's
 * procedural music already produces: 44.1kHz stereo 16-bit interleaved
 * shorts, trimmed or looped to exactly [durationSeconds] worth of frames.
 * Matching that contract exactly means the caller (VideoGenerator,
 * PreviewAudioPlayer) needs zero changes to consume it — it drops straight
 * into the existing encode/preview pipeline as a ShortArray.
 *
 * Every failure path returns null rather than throwing — decoding an
 * arbitrary user-supplied file is inherently more failure-prone than our own
 * generated audio, and a bad song must never take the whole export down
 * with it; callers fall back to silence or procedural music instead.
 */
object CustomAudioDecoder {
    private const val TAG = "CustomAudioDecoder"
    private const val TARGET_SAMPLE_RATE = AudioSynthesizer.SAMPLE_RATE // 44100
    private const val TARGET_CHANNELS = 2
    private const val MAX_DECODE_MS = 30_000L   // guards against a corrupt/huge file hanging export

    fun decode(context: Context, uri: Uri, durationSeconds: Int): ShortArray? {
        val targetFrames = durationSeconds * TARGET_SAMPLE_RATE
        return try {
            val raw = decodeRaw(context, uri) ?: return null
            fitToDuration(raw, targetFrames)
        } catch (e: Exception) {
            Log.e(TAG, "Custom audio decode failed: ${e.message}", e)
            null
        }
    }

    private class RawPcm(val samples: ShortArray, val sampleRate: Int, val channels: Int)

    private fun decodeRaw(context: Context, uri: Uri): RawPcm? {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) { trackIndex = i; format = f; break }
            }
            if (trackIndex < 0 || format == null) {
                Log.w(TAG, "No audio track found in selected file")
                return null
            }
            extractor.selectTrack(trackIndex)

            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            try {
                val bufferInfo = MediaCodec.BufferInfo()
                val pcmChunks = ArrayList<ShortArray>()
                var sawInputEOS = false
                var sawOutputEOS = false
                var outSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                var outChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                val startTime = System.currentTimeMillis()

                while (!sawOutputEOS && System.currentTimeMillis() - startTime < MAX_DECODE_MS) {
                    if (!sawInputEOS) {
                        val inIdx = decoder.dequeueInputBuffer(10_000L)
                        if (inIdx >= 0) {
                            val inBuf = decoder.getInputBuffer(inIdx)!!
                            val sampleSize = extractor.readSampleData(inBuf, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                sawInputEOS = true
                            } else {
                                decoder.queueInputBuffer(inIdx, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }

                    val outIdx = decoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                    when {
                        outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = decoder.outputFormat
                            outSampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            outChannels = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                        outIdx >= 0 -> {
                            val outBuf = decoder.getOutputBuffer(outIdx)
                            if (outBuf != null && bufferInfo.size > 0) {
                                outBuf.position(bufferInfo.offset)
                                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                                outBuf.order(ByteOrder.LITTLE_ENDIAN)
                                val chunk = ShortArray(bufferInfo.size / 2)
                                outBuf.asShortBuffer().get(chunk)
                                pcmChunks.add(chunk)
                            }
                            decoder.releaseOutputBuffer(outIdx, false)
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                sawOutputEOS = true
                            }
                        }
                    }
                }

                val total = pcmChunks.sumOf { it.size }
                if (total == 0) return null
                val merged = ShortArray(total)
                var pos = 0
                for (chunk in pcmChunks) { chunk.copyInto(merged, pos); pos += chunk.size }
                return RawPcm(merged, outSampleRate, outChannels)
            } finally {
                decoder.stop()
                decoder.release()
            }
        } finally {
            extractor.release()
        }
    }

    /** Resamples/upmixes to 44.1kHz stereo, then trims or loops to exactly [targetFrames] frames. */
    private fun fitToDuration(raw: RawPcm, targetFrames: Int): ShortArray {
        val stereo = toStereo(raw.samples, raw.channels)
        val resampled = if (raw.sampleRate == TARGET_SAMPLE_RATE) stereo
                         else resampleStereo(stereo, raw.sampleRate, TARGET_SAMPLE_RATE)

        val sourceFrames = resampled.size / TARGET_CHANNELS
        val out = ShortArray(targetFrames * TARGET_CHANNELS)
        if (sourceFrames <= 0) return out   // silence rather than crash on a degenerate decode

        for (frame in 0 until targetFrames) {
            val srcFrame = frame % sourceFrames   // loop if the song is shorter than the video
            out[frame * 2]     = resampled[srcFrame * 2]
            out[frame * 2 + 1] = resampled[srcFrame * 2 + 1]
        }
        return out
    }

    private fun toStereo(samples: ShortArray, channels: Int): ShortArray = when {
        channels == 2 -> samples
        channels == 1 -> ShortArray(samples.size * 2).also { out ->
            for (i in samples.indices) { out[i * 2] = samples[i]; out[i * 2 + 1] = samples[i] }
        }
        channels >= 3 -> {
            // Rare for music files — take the first two channels as L/R.
            val frames = samples.size / channels
            ShortArray(frames * 2).also { out ->
                for (f in 0 until frames) {
                    out[f * 2]     = samples[f * channels]
                    out[f * 2 + 1] = samples[f * channels + 1]
                }
            }
        }
        else -> ShortArray(0)
    }

    /** Linear-interpolation resampler — ample quality for background music at video bitrates. */
    private fun resampleStereo(stereo: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        val srcFrames = stereo.size / 2
        if (srcFrames <= 1) return stereo
        val dstFrames = ((srcFrames.toLong() * toRate) / fromRate).toInt()
        val out = ShortArray(dstFrames * 2)
        val ratio = fromRate.toDouble() / toRate.toDouble()
        for (i in 0 until dstFrames) {
            val srcPos = i * ratio
            val i0 = srcPos.toInt().coerceIn(0, srcFrames - 1)
            val i1 = (i0 + 1).coerceAtMost(srcFrames - 1)
            val frac = srcPos - i0
            out[i * 2]     = (stereo[i0 * 2]     + (stereo[i1 * 2]     - stereo[i0 * 2])     * frac).roundToInt().toShort()
            out[i * 2 + 1] = (stereo[i0 * 2 + 1] + (stereo[i1 * 2 + 1] - stereo[i0 * 2 + 1]) * frac).roundToInt().toShort()
        }
        return out
    }
}
