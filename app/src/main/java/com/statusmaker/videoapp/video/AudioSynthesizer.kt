package com.statusmaker.videoapp.video

import com.statusmaker.videoapp.data.model.MusicStyle
import kotlin.math.*
import kotlin.random.Random

/**
 * Multi-track procedural music engine — no audio files required.
 *
 * v2 — "arranged song" engine. What changed vs the old static-loop synth
 * (and why it no longer sounds robotic):
 *
 *  - REAL ARRANGEMENT: the track is built phrase by phrase — sparse intro,
 *    groove A, answer phrase B, energy that builds over time, drum fills
 *    at phrase turnarounds, crashes at section starts and a clean ending —
 *    instead of one 8-beat loop repeated identically forever.
 *  - REAL INSTRUMENTS: Karplus-Strong plucked strings (veena/mandolin
 *    character), 2-operator FM temple bells, a breathy flute with meend
 *    (pitch glides) and delayed vibrato, phase-accumulated supersaw with
 *    filter envelopes, warm stereo pads and a tanpura-style drone.
 *  - INDIAN PERCUSSION: synthesized tabla bols (na / tin / dha / ge),
 *    dholak-style strokes, shaker, claps, crash — arranged in theka /
 *    teenmaar-flavoured patterns per style, with swing and accent maps.
 *  - HUMAN FEEL: per-note timing/velocity jitter, beat-position accents,
 *    vibrato that fades in like a real player, glides between melody notes.
 *  - PRODUCED MIX: constant-power stereo placement per layer, sidechain
 *    pump on kick styles, bus compression, and a longer decorrelated
 *    stereo reverb.
 */
object AudioSynthesizer {

    const val SAMPLE_RATE = 44100
    private const val TWO_PI = 2.0 * Math.PI

    // ─── Note frequency table (equal temperament, A4 = 440) ─────────────────

    private val FREQ: Map<String, Double> = buildMap {
        val names = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")
        for (oct in 1..6) {
            for ((i, n) in names.withIndex()) {
                val midi = (oct + 1) * 12 + i
                put("$n$oct", 440.0 * 2.0.pow((midi - 69) / 12.0))
            }
        }
        put("R", 0.0)
    }

    private fun f(name: String): Double = FREQ[name] ?: 0.0

    // ─── Timbres available to melody/harmony lines ──────────────────────────

    private enum class Timbre { PLUCK, FLUTE, SAW, SQUARE, BELL, SINE, PAD }

    /** One melodic event: note name, length in beats, optional accent flag. */
    private data class Ev(val note: String, val beats: Double, val accent: Boolean = false)

    private fun seq(vararg e: Pair<String, Double>): List<Ev> = e.map { Ev(it.first, it.second) }

    // ─── Stereo render context ───────────────────────────────────────────────

    /**
     * All layers render into one of two stereo buses:
     * [musL]/[musR] (musical bed — gets sidechain-ducked by the kick) and
     * [drmL]/[drmR] (percussion — never ducked, always punches through).
     */
    private class Ctx(val total: Int, val bpm: Double) {
        val musL = DoubleArray(total); val musR = DoubleArray(total)
        val drmL = DoubleArray(total); val drmR = DoubleArray(total)
        val kicks = ArrayList<Int>()
        val spb: Double = SAMPLE_RATE * 60.0 / bpm      // samples per beat
        val rnd = Random(0x5EED)                        // deterministic feel
    }

    /** Constant-power pan gains for pan in [-1 (left) .. +1 (right)]. */
    private fun panGains(pan: Double): Pair<Double, Double> {
        val a = (pan.coerceIn(-1.0, 1.0) + 1.0) * (Math.PI / 4.0)
        return cos(a) to sin(a)
    }

    // ─── Public entry points ─────────────────────────────────────────────────

    /** Full track for export — includes intro, building energy and a clean fade ending. */
    fun generate(style: MusicStyle, durationSeconds: Int): ShortArray {
        val total = durationSeconds * SAMPLE_RATE
        if (style == MusicStyle.NONE || total <= 0) return ShortArray(max(total, 0) * 2)
        val ctx = build(style, total, loopMode = false)
        applyFadeOut(ctx, seconds = 0.9)
        return mixdown(ctx, style)
    }

    /**
     * Bar-exact groove loop for the preview player. Skips the sparse intro /
     * fade ending so the buffer loops seamlessly as a continuous groove.
     * Length is a whole number of musical phrases — no mid-bar truncation.
     */
    fun generateLoop(style: MusicStyle): ShortArray {
        if (style == MusicStyle.NONE) return ShortArray(SAMPLE_RATE * 4 * 2)
        val spec = specFor(style)
        val phraseSamples = (spec.phraseBeats * SAMPLE_RATE * 60.0 / spec.bpm).roundToInt()
        val total = phraseSamples * spec.loopPhrases
        val ctx = build(style, total, loopMode = true)
        return mixdown(ctx, style)
    }

    // ─── Style spec (tempo + phrase geometry, used by both entry points) ────

    private class StyleSpec(val bpm: Double, val phraseBeats: Double, val loopPhrases: Int)

    private fun specFor(style: MusicStyle): StyleSpec = when (style) {
        MusicStyle.FILMY        -> StyleSpec(128.0, 8.0, 4)
        MusicStyle.FOLK         -> StyleSpec(140.0, 8.0, 4)
        MusicStyle.CLASSICAL    -> StyleSpec(88.0, 16.0, 2)
        MusicStyle.DEVOTIONAL   -> StyleSpec(78.0, 16.0, 2)
        MusicStyle.INSTRUMENTAL -> StyleSpec(110.0, 8.0, 4)
        MusicStyle.NONE         -> StyleSpec(120.0, 8.0, 4)
    }

    private fun build(style: MusicStyle, total: Int, loopMode: Boolean): Ctx {
        val spec = specFor(style)
        val ctx = Ctx(total, spec.bpm)
        when (style) {
            MusicStyle.FILMY        -> buildFilmy(ctx, loopMode)
            MusicStyle.FOLK         -> buildFolk(ctx, loopMode)
            MusicStyle.CLASSICAL    -> buildClassical(ctx, loopMode)
            MusicStyle.DEVOTIONAL   -> buildDevotional(ctx, loopMode)
            MusicStyle.INSTRUMENTAL -> buildInstrumental(ctx, loopMode)
            MusicStyle.NONE         -> {}
        }
        return ctx
    }

    // ─── Final mixdown: sidechain → compress → reverb → limit → interleave ──

    private fun mixdown(ctx: Ctx, style: MusicStyle): ShortArray {
        val total = ctx.total
        applySidechainDuck(ctx.musL, ctx.kicks, total)
        applySidechainDuck(ctx.musR, ctx.kicks, total)

        val dryL = DoubleArray(total) { ctx.musL[it] + ctx.drmL[it] }
        val dryR = DoubleArray(total) { ctx.musR[it] + ctx.drmR[it] }
        applyCompressor(dryL, total)
        applyCompressor(dryR, total)

        var peak = 0.001
        for (i in 0 until total) {
            val m = max(abs(dryL[i]), abs(dryR[i]))
            if (m > peak) peak = m
        }
        for (i in 0 until total) { dryL[i] /= peak; dryR[i] /= peak }

        // Calmer styles get a longer, wetter tail; groove styles stay tighter.
        val (wet, fb) = when (style) {
            MusicStyle.DEVOTIONAL, MusicStyle.CLASSICAL -> 0.30 to 0.62
            MusicStyle.INSTRUMENTAL                     -> 0.26 to 0.55
            else                                        -> 0.20 to 0.48
        }
        val revL = applyReverb(dryL, total, channelSeed = 0, feedback = fb)
        val revR = applyReverb(dryR, total, channelSeed = 1, feedback = fb)

        val out = ShortArray(total * 2)
        for (i in 0 until total) {
            val l = tanh((dryL[i] + revL[i] * wet) * 0.85) * 0.94
            val r = tanh((dryR[i] + revR[i] * wet) * 0.85) * 0.94
            out[i * 2]     = (l * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
            out[i * 2 + 1] = (r * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return out
    }

    private fun applyFadeOut(ctx: Ctx, seconds: Double) {
        val n = (seconds * SAMPLE_RATE).toInt().coerceAtMost(ctx.total)
        val start = ctx.total - n
        for (i in 0 until n) {
            val g = 1.0 - i.toDouble() / n
            val gg = g * g
            ctx.musL[start + i] *= gg; ctx.musR[start + i] *= gg
            ctx.drmL[start + i] *= gg; ctx.drmR[start + i] *= gg
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Melody scheduling — articulation, accents, glides, humanization
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Renders one phrase of melodic events starting at [startSample].
     * Adds beat-position accents (downbeats stronger), per-note jitter, and —
     * when [glide] is on — a meend-style pitch slide from the previous note.
     */
    private fun melodyLine(
        ctx: Ctx, timbre: Timbre, phrase: List<Ev>, startSample: Int,
        vol: Double, pan: Double = 0.0, octaveShift: Int = 0,
        glide: Boolean = false, vibrato: Boolean = false
    ) {
        var beat = 0.0
        var prevFreq = 0.0
        for (ev in phrase) {
            val fq = f(ev.note) * 2.0.pow(octaveShift)
            if (fq > 20.0) {
                val jitter = (ctx.rnd.nextDouble(-1.0, 1.0) * ctx.spb * 0.006).toInt()
                val nearest = beat.roundToInt()
                val onBeat = abs(beat - nearest) < 1e-4
                val accent = when {
                    ev.accent                 -> 1.16
                    onBeat && nearest % 4 == 0 -> 1.10
                    onBeat                    -> 1.03
                    else                      -> 0.93
                }
                val vel = vol * accent * ctx.rnd.nextDouble(0.94, 1.05)
                val s = startSample + (beat * ctx.spb).toInt() + jitter
                val d = (ev.beats * ctx.spb).toInt()
                val gFrom = if (glide && prevFreq > 20.0 && abs(prevFreq - fq) > 0.5) prevFreq else 0.0
                when (timbre) {
                    // let the string ring a little past the notated length
                    Timbre.PLUCK  -> pluck(ctx, fq, s, min((d * 1.5).toInt(), d + SAMPLE_RATE), vel, pan)
                    Timbre.FLUTE  -> flute(ctx, fq, gFrom, s, d, vel, pan)
                    Timbre.SINE   -> softSine(ctx, fq, gFrom, s, d, vel, pan, vibrato)
                    Timbre.BELL   -> bell(ctx, fq, s, max(d, (SAMPLE_RATE * 1.2).toInt()), vel, pan)
                    Timbre.SAW    -> synthLead(ctx, fq, gFrom, s, d, vel, pan, square = false, vibrato = vibrato)
                    Timbre.SQUARE -> synthLead(ctx, fq, gFrom, s, d, vel, pan, square = true, vibrato = vibrato)
                    Timbre.PAD    -> pad(ctx, listOf(fq), s, d, vel)
                }
                prevFreq = fq
            } else {
                prevFreq = 0.0   // a rest breaks the glide chain
            }
            beat += ev.beats
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Instruments
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Karplus-Strong plucked string — a real physical model, so it decays and
     * shimmers like an actual veena/mandolin string instead of an organ tone.
     */
    private fun pluck(
        ctx: Ctx, freq: Double, start: Int, dur: Int, vol: Double, pan: Double,
        damping: Double = 0.9955, brightness: Double = 0.62
    ) {
        if (freq < 20.0 || dur <= 0) return
        val n = (SAMPLE_RATE / freq).roundToInt().coerceIn(2, 2400)
        val buf = DoubleArray(n)
        val r = Random(start * 31 + n)
        var lp = 0.0
        for (i in 0 until n) {
            lp += brightness * (r.nextDouble(-1.0, 1.0) - lp)
            buf[i] = lp
        }
        val (gl, gr) = panGains(pan)
        val fade = min(dur / 6, (0.025 * SAMPLE_RATE).toInt()).coerceAtLeast(16)
        var idx = 0
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val out = buf[idx]
            buf[idx] = (out + buf[(idx + 1) % n]) * 0.5 * damping
            idx = (idx + 1) % n
            if (p < 0) continue
            val g = if (i > dur - fade) (dur - i).toDouble() / fade else 1.0
            val v = out * vol * g * 0.9
            ctx.musL[p] += v * gl; ctx.musR[p] += v * gr
        }
    }

    /**
     * Flute — fundamental + soft harmonics + filtered breath noise. Vibrato
     * fades in after ~150 ms (like a real player) and [glideFrom] gives the
     * meend slide into the note.
     */
    private fun flute(
        ctx: Ctx, freq: Double, glideFrom: Double, start: Int, dur: Int,
        vol: Double, pan: Double
    ) {
        if (freq < 20.0 || dur <= 0) return
        val (gl, gr) = panGains(pan)
        val r = Random(start * 17 + 5)
        var phase = 0.0
        var lpN = 0.0
        val atk = min((0.045 * SAMPLE_RATE).toInt(), dur / 4).coerceAtLeast(32)
        val rel = (dur * 0.20).toInt().coerceAtLeast(64)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val t = i.toDouble() / SAMPLE_RATE
            var fq = freq
            if (glideFrom > 20.0) fq = freq + (glideFrom - freq) * exp(-t / 0.06)
            val vibDepth = 0.0075 * ((t - 0.15) / 0.30).coerceIn(0.0, 1.0)
            fq *= 1.0 + vibDepth * sin(TWO_PI * 5.4 * (t + start * 1e-7))
            phase += TWO_PI * fq / SAMPLE_RATE
            if (p < 0) continue
            val tone = sin(phase) + 0.30 * sin(2 * phase) + 0.10 * sin(3 * phase)
            lpN += 0.16 * (r.nextDouble(-1.0, 1.0) - lpN)
            val env = when {
                i < atk        -> i.toDouble() / atk
                i > dur - rel  -> (dur - i).toDouble() / rel
                else           -> 0.92 + 0.08 * sin(TWO_PI * 0.9 * t)   // gentle breath swell
            }.coerceIn(0.0, 1.0)
            val v = (tone * 0.62 + lpN * 0.055) * env * vol
            ctx.musL[p] += v * gl; ctx.musR[p] += v * gr
        }
    }

    /** Soft sine lead (counter-melodies) — 2 harmonics, optional vibrato + glide. */
    private fun softSine(
        ctx: Ctx, freq: Double, glideFrom: Double, start: Int, dur: Int,
        vol: Double, pan: Double, vibrato: Boolean
    ) {
        if (freq < 20.0 || dur <= 0) return
        val (gl, gr) = panGains(pan)
        var phase = 0.0
        val atk = min((0.02 * SAMPLE_RATE).toInt(), dur / 4).coerceAtLeast(16)
        val rel = (dur * 0.25).toInt().coerceAtLeast(32)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val t = i.toDouble() / SAMPLE_RATE
            var fq = freq
            if (glideFrom > 20.0) fq = freq + (glideFrom - freq) * exp(-t / 0.05)
            if (vibrato) {
                val vd = 0.006 * ((t - 0.12) / 0.25).coerceIn(0.0, 1.0)
                fq *= 1.0 + vd * sin(TWO_PI * 5.6 * t)
            }
            phase += TWO_PI * fq / SAMPLE_RATE
            if (p < 0) continue
            val env = when {
                i < atk       -> i.toDouble() / atk
                i > dur - rel -> (dur - i).toDouble() / rel
                else          -> 1.0
            }
            val v = (sin(phase) + 0.22 * sin(2 * phase)) * env * vol * 0.8
            ctx.musL[p] += v * gl; ctx.musR[p] += v * gr
        }
    }

    /**
     * Temple bell — 2-operator FM with an inharmonic modulator ratio plus a
     * low "hum" partial; rings and shimmers like real metal.
     */
    private fun bell(ctx: Ctx, freq: Double, start: Int, dur: Int, vol: Double, pan: Double) {
        if (freq < 20.0 || dur <= 0) return
        val (gl, gr) = panGains(pan)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            if (p < 0) continue
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-2.3 * t)
            if (env < 0.001) break
            val fmIndex = 3.2 * exp(-3.5 * t)
            val mod = sin(TWO_PI * freq * 3.5307 * t) * fmIndex
            val ring = sin(TWO_PI * freq * t + mod)
            val strike = sin(TWO_PI * freq * 2.756 * t) * exp(-11.0 * t) * 0.45
            val hum = sin(TWO_PI * freq * 0.5 * t) * exp(-1.6 * t) * 0.30
            val v = (ring + strike + hum) * env * vol * 0.75
            ctx.musL[p] += v * gl; ctx.musR[p] += v * gr
        }
    }

    /** PolyBLEP band-limited sawtooth step. [t] = phase in [0,1), [dt] = freq/SR. */
    private fun blep(t: Double, dt: Double): Double = when {
        t < dt        -> { val x = t / dt; x + x - x * x - 1.0 }
        t > 1.0 - dt  -> { val x = (t - 1.0) / dt; x * x + x + x + 1.0 }
        else          -> 0.0
    }

    private fun sawAt(phase: Double, dt: Double): Double =
        2.0 * phase - 1.0 - blep(phase, dt)

    private fun squareAt(phase: Double, dt: Double): Double {
        val p2 = (phase + 0.5) % 1.0
        return (if (phase < 0.5) 1.0 else -1.0) + blep(phase, dt) - blep(p2, dt)
    }

    /**
     * Detuned-unison synth lead ("supersaw"). Five free-running voices with
     * independent random phases (kills the phasey robot-chorus of the old
     * engine), per-note filter envelope, glide and delayed vibrato.
     */
    private fun synthLead(
        ctx: Ctx, freq: Double, glideFrom: Double, start: Int, dur: Int,
        vol: Double, pan: Double, square: Boolean, vibrato: Boolean,
        detune: Double = 0.0045,
        fStart: Double = 8500.0, fSus: Double = 5200.0, fDecay: Double = 5.0
    ) {
        if (freq < 20.0 || dur <= 0) return
        val (gl, gr) = panGains(pan)
        val voices = 5
        val phases = DoubleArray(voices)
        val offs = DoubleArray(voices)
        val gains = DoubleArray(voices)
        val r = Random(start * 13 + 7)
        for (v in 0 until voices) {
            phases[v] = r.nextDouble()
            val c = (v - (voices - 1) / 2.0) / ((voices - 1) / 2.0)   // -1..1
            offs[v] = 1.0 + detune * c
            gains[v] = if (v == voices / 2) 1.0 else 0.62
        }
        val gainNorm = gains.sum()
        var filt = 0.0
        val atk = min((0.012 * SAMPLE_RATE).toInt(), dur / 6).coerceAtLeast(16)
        val dec = (dur * 0.10).toInt().coerceAtLeast(16)
        val rel = (dur * 0.20).toInt().coerceAtLeast(32)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val t = i.toDouble() / SAMPLE_RATE
            var fq = freq
            if (glideFrom > 20.0) fq = freq + (glideFrom - freq) * exp(-t / 0.05)
            if (vibrato) {
                val vd = 0.006 * ((t - 0.15) / 0.30).coerceIn(0.0, 1.0)
                fq *= 1.0 + vd * sin(TWO_PI * 5.2 * t)
            }
            var wave = 0.0
            for (v in 0 until voices) {
                val dt = (fq * offs[v] / SAMPLE_RATE).coerceIn(1e-6, 0.45)
                phases[v] += dt
                if (phases[v] >= 1.0) phases[v] -= 1.0
                wave += gains[v] * if (square) squareAt(phases[v], dt) else sawAt(phases[v], dt)
            }
            wave /= gainNorm
            if (p < 0) continue
            val cutoff = fSus + (fStart - fSus) * exp(-fDecay * t)
            val alpha = (1.0 - exp(-TWO_PI * cutoff / SAMPLE_RATE)).coerceIn(0.0, 1.0)
            filt += alpha * (wave - filt)
            val env = when {
                i < atk        -> i.toDouble() / atk
                i < atk + dec  -> 1.0 - 0.25 * (i - atk).toDouble() / dec
                i > dur - rel  -> 0.75 * (dur - i).toDouble() / rel
                else           -> 0.75
            }.coerceIn(0.0, 1.0)
            val v2 = filt * env * vol
            ctx.musL[p] += v2 * gl; ctx.musR[p] += v2 * gr
        }
    }

    /**
     * Warm stereo pad — for each chord tone, two darkly-filtered detuned saws
     * rendered hard left / hard right (instant natural width), slow attack.
     */
    private fun pad(ctx: Ctx, freqs: List<Double>, start: Int, dur: Int, vol: Double) {
        for (freq in freqs) {
            padVoice(ctx, freq * 0.9965, start, dur, vol, left = true)
            padVoice(ctx, freq * 1.0035, start, dur, vol, left = false)
        }
    }

    private fun padVoice(ctx: Ctx, freq: Double, start: Int, dur: Int, vol: Double, left: Boolean) {
        if (freq < 20.0 || dur <= 0) return
        var phase = Random(start + if (left) 1 else 2).nextDouble()
        var filt = 0.0
        val atk = (dur * 0.30).coerceAtLeast(SAMPLE_RATE * 0.05).toInt()
        val rel = (dur * 0.30).toInt().coerceAtLeast(64)
        val alpha = (1.0 - exp(-TWO_PI * 1100.0 / SAMPLE_RATE))
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val dt = (freq / SAMPLE_RATE)
            phase += dt
            if (phase >= 1.0) phase -= 1.0
            if (p < 0) continue
            filt += alpha * (sawAt(phase, dt) - filt)
            val env = when {
                i < atk       -> i.toDouble() / atk
                i > dur - rel -> (dur - i).toDouble() / rel
                else          -> 1.0
            }
            val v = filt * env * vol * 0.55
            if (left) ctx.musL[p] += v else ctx.musR[p] += v
        }
    }

    /** Pluck-filtered synth bass + sub-sine layer. Mono, dead center. */
    private fun bassNote(ctx: Ctx, freq: Double, start: Int, dur: Int, vol: Double) {
        if (freq < 20.0 || dur <= 0) return
        var phase = 0.31
        var subPhase = 0.0
        var filt = 0.0
        val atk = (0.004 * SAMPLE_RATE).toInt().coerceAtLeast(8)
        val rel = (dur * 0.15).toInt().coerceAtLeast(32)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            val t = i.toDouble() / SAMPLE_RATE
            val dt = freq / SAMPLE_RATE
            phase += dt; if (phase >= 1.0) phase -= 1.0
            subPhase += TWO_PI * freq * 0.5 / SAMPLE_RATE
            if (p < 0) continue
            val cutoff = 750.0 + 2400.0 * exp(-10.0 * t)
            val alpha = (1.0 - exp(-TWO_PI * cutoff / SAMPLE_RATE)).coerceIn(0.0, 1.0)
            filt += alpha * (sawAt(phase, dt) - filt)
            val env = when {
                i < atk       -> i.toDouble() / atk
                i > dur - rel -> (dur - i).toDouble() / rel
                else          -> 1.0
            }
            val v = (filt * 0.75 + sin(subPhase) * 0.35) * env * vol
            ctx.musL[p] += v * 0.5; ctx.musR[p] += v * 0.5
        }
    }

    /** Schedule a bass pattern (loops once through the event list). */
    private fun bassLine(ctx: Ctx, phrase: List<Ev>, startSample: Int, vol: Double) {
        var beat = 0.0
        for (ev in phrase) {
            val fq = f(ev.note)
            if (fq > 20.0) {
                val s = startSample + (beat * ctx.spb).toInt()
                val d = (ev.beats * ctx.spb * 0.95).toInt()
                val vel = vol * ctx.rnd.nextDouble(0.95, 1.05)
                bassNote(ctx, fq, s, d, vel)
            }
            beat += ev.beats
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Percussion
    // ═══════════════════════════════════════════════════════════════════════

    /** Render [dur] samples of percussion via [gen] into the drum bus at [pan]. */
    private inline fun perc(ctx: Ctx, start: Int, dur: Int, pan: Double, gen: (Int, Double) -> Double) {
        val (gl, gr) = panGains(pan)
        for (i in 0 until dur) {
            val p = start + i
            if (p >= ctx.total) break
            if (p < 0) continue
            val v = gen(i, i.toDouble() / SAMPLE_RATE)
            ctx.drmL[p] += v * gl; ctx.drmR[p] += v * gr
        }
    }

    private fun kick(ctx: Ctx, start: Int, vol: Double = 0.9) {
        ctx.kicks.add(start)
        perc(ctx, start, (SAMPLE_RATE * 0.28).toInt(), 0.0) { i, t ->
            val fq = 72.0 + 190.0 * exp(-38.0 * t)
            val click = if (i < 200) sin(TWO_PI * 3400.0 * t) * 0.3 * exp(-90.0 * t) else 0.0
            tanh((sin(TWO_PI * fq * t) * 0.95 + click) * 1.2) * exp(-7.5 * t) * vol
        }
    }

    private fun snare(ctx: Ctx, start: Int, vol: Double = 0.55) {
        perc(ctx, start, (SAMPLE_RATE * 0.15).toInt(), 0.0) { i, t ->
            val noise = pseudoNoise(start + i, 0x5A5A)
            val tone = sin(TWO_PI * 215.0 * t) * exp(-28.0 * t)
            val body = sin(TWO_PI * 142.0 * t) * exp(-42.0 * t) * 0.4
            (noise * 0.62 + tone * 0.30 + body) * exp(-21.0 * t) * vol
        }
    }

    /** 808-style metallic hi-hat (6 inharmonic partials), panned slightly right. */
    private fun hihat(ctx: Ctx, start: Int, open: Boolean, vol: Double) {
        val decay = if (open) 13.0 else 70.0
        val dur = (SAMPLE_RATE * if (open) 0.25 else 0.055).toInt()
        val ratios = doubleArrayOf(2.0, 3.0, 4.16, 5.43, 6.79, 8.21)
        perc(ctx, start, dur, 0.35) { i, t ->
            var metal = 0.0
            for (r2 in ratios) metal += sign(sin(TWO_PI * 40.0 * r2 * t))
            metal /= ratios.size
            (metal * 0.72 + pseudoNoise(start + i, 0xBEEF) * 0.30) * exp(-decay * t) * vol
        }
    }

    /** Soft shaker — bright filtered noise with a push-pull envelope, panned left. */
    private fun shaker(ctx: Ctx, start: Int, vol: Double) {
        val dur = (SAMPLE_RATE * 0.07).toInt()
        var hp = 0.0
        perc(ctx, start, dur, -0.4) { i, t ->
            val n = pseudoNoise(start + i, 0x7A3F)
            hp += 0.55 * (n - hp)                      // crude brightener
            val env = if (t < 0.012) t / 0.012 else exp(-45.0 * (t - 0.012))
            (n - hp * 0.7) * env * vol
        }
    }

    private fun clap(ctx: Ctx, start: Int, vol: Double = 0.5) {
        val gap = (SAMPLE_RATE * 0.010).toInt()
        repeat(3) { b ->
            perc(ctx, start + b * gap, (SAMPLE_RATE * 0.012).toInt(), 0.15) { i, t ->
                pseudoNoise(start + b * gap + i, 0xC1A9) * exp(-40.0 * t) * vol * 0.55
            }
        }
        perc(ctx, start + 3 * gap, (SAMPLE_RATE * 0.09).toInt(), 0.15) { i, t ->
            pseudoNoise(start + i, 0xC1AA) * exp(-17.0 * t) * vol * 0.4
        }
    }

    /** Crash cymbal — dense inharmonic partials + noise wash, ~1.5 s ring. */
    private fun crash(ctx: Ctx, start: Int, vol: Double = 0.4) {
        val ratios = doubleArrayOf(1.0, 1.59, 2.14, 2.76, 3.29, 4.07, 5.02, 6.11)
        perc(ctx, start, (SAMPLE_RATE * 1.5).toInt(), 0.25) { i, t ->
            var metal = 0.0
            for (r2 in ratios) metal += sin(TWO_PI * 337.0 * r2 * t + r2)
            metal /= ratios.size
            val noise = pseudoNoise(start + i, 0xCA5B) * 0.6
            (metal * 0.5 + noise) * exp(-2.6 * t) * vol
        }
    }

    /** Pitched tom for fills. */
    private fun tom(ctx: Ctx, start: Int, pitch: Double, vol: Double = 0.55, pan: Double = 0.0) {
        perc(ctx, start, (SAMPLE_RATE * 0.22).toInt(), pan) { _, t ->
            val fq = pitch * (1.0 + 0.5 * exp(-30.0 * t))
            sin(TWO_PI * fq * t) * exp(-11.0 * t) * vol
        }
    }

    // ─── Tabla / dholak bols ─────────────────────────────────────────────────

    /** "Na/Ta" — bright ringing rim stroke on the dayan. */
    private fun tablaNa(ctx: Ctx, start: Int, vol: Double) {
        perc(ctx, start, (SAMPLE_RATE * 0.18).toInt(), 0.3) { i, t ->
            val ring = sin(TWO_PI * 573.0 * t) + 0.5 * sin(TWO_PI * 1146.0 * t + 0.7)
            val snap = pseudoNoise(start + i, 0x7AB1) * exp(-160.0 * t) * 0.7
            (ring * exp(-14.0 * t) * 0.6 + snap) * vol
        }
    }

    /** "Tin" — damped high stroke. */
    private fun tablaTin(ctx: Ctx, start: Int, vol: Double) {
        perc(ctx, start, (SAMPLE_RATE * 0.07).toInt(), 0.3) { i, t ->
            (sin(TWO_PI * 660.0 * t) * 0.7 + pseudoNoise(start + i, 0x33D1) * 0.3) *
                exp(-55.0 * t) * vol
        }
    }

    /** "Ge/Ghe" — bayan bass with the characteristic downward pitch bend. */
    private fun tablaGe(ctx: Ctx, start: Int, vol: Double) {
        perc(ctx, start, (SAMPLE_RATE * 0.16).toInt(), -0.2) { _, t ->
            val fq = 82.0 + 95.0 * exp(-22.0 * t)
            tanh(sin(TWO_PI * fq * t) * 1.4) * exp(-13.0 * t) * vol
        }
    }

    /** "Dha" = Na + Ge struck together — the strong beat of the theka. */
    private fun tablaDha(ctx: Ctx, start: Int, vol: Double) {
        tablaNa(ctx, start, vol * 0.85)
        tablaGe(ctx, start, vol)
    }

    /** Dholak open slap — woodier and punchier than tabla, drives folk grooves. */
    private fun dholak(ctx: Ctx, start: Int, low: Boolean, vol: Double) {
        if (low) {
            perc(ctx, start, (SAMPLE_RATE * 0.14).toInt(), -0.15) { _, t ->
                val fq = 110.0 + 130.0 * exp(-30.0 * t)
                tanh(sin(TWO_PI * fq * t) * 1.6) * exp(-16.0 * t) * vol
            }
        } else {
            perc(ctx, start, (SAMPLE_RATE * 0.09).toInt(), 0.2) { i, t ->
                val tone = sin(TWO_PI * 420.0 * t) * 0.6 + sin(TWO_PI * 630.0 * t) * 0.3
                (tone + pseudoNoise(start + i, 0xD0AC) * 0.35) * exp(-38.0 * t) * vol
            }
        }
    }

    // ─── Pattern scheduling ──────────────────────────────────────────────────

    /** A drum hit type used in beat-pattern tables. */
    private enum class Hit { KICK, SNARE, CLAP, HAT, OHAT, SHAKER, NA, TIN, GE, DHA, DHOL_LO, DHOL_HI, CRASH }

    /**
     * Schedule (beat, hit, velocity) triples over one phrase starting at
     * [startSample], with optional swing on off-beat 16ths.
     */
    private fun drumPattern(
        ctx: Ctx, startSample: Int, hits: List<Triple<Double, Hit, Double>>,
        swing: Double = 0.0
    ) {
        for ((beat, hit, vel) in hits) {
            var b = beat
            // swing: delay the "e" and "a" 16th positions
            val frac = b - floor(b)
            if (swing > 0 && (abs(frac - 0.25) < 1e-4 || abs(frac - 0.75) < 1e-4)) b += swing * 0.12
            val s = startSample + (b * ctx.spb).toInt() +
                    (ctx.rnd.nextDouble(-1.0, 1.0) * ctx.spb * 0.004).toInt()
            val v = vel * ctx.rnd.nextDouble(0.93, 1.05)
            when (hit) {
                Hit.KICK    -> kick(ctx, s, v)
                Hit.SNARE   -> snare(ctx, s, v)
                Hit.CLAP    -> clap(ctx, s, v)
                Hit.HAT     -> hihat(ctx, s, open = false, vol = v)
                Hit.OHAT    -> hihat(ctx, s, open = true, vol = v)
                Hit.SHAKER  -> shaker(ctx, s, v)
                Hit.NA      -> tablaNa(ctx, s, v)
                Hit.TIN     -> tablaTin(ctx, s, v)
                Hit.GE      -> tablaGe(ctx, s, v)
                Hit.DHA     -> tablaDha(ctx, s, v)
                Hit.DHOL_LO -> dholak(ctx, s, low = true, vol = v)
                Hit.DHOL_HI -> dholak(ctx, s, low = false, vol = v)
                Hit.CRASH   -> crash(ctx, s, v)
            }
        }
    }

    /** 16th-note hat/shaker carpet with a strong-weak accent map. */
    private fun hatCarpet(
        ctx: Ctx, startSample: Int, beats: Double, hit: Hit,
        stepBeats: Double = 0.5, baseVel: Double = 0.30, swing: Double = 0.0
    ) {
        val hits = ArrayList<Triple<Double, Hit, Double>>()
        var b = 0.0
        while (b < beats - 1e-6) {
            val nearest = b.roundToInt()
            val vel = when {
                abs(b - nearest) < 1e-4 && nearest % 2 == 0 -> baseVel * 1.25
                abs(b - nearest) < 1e-4                     -> baseVel * 1.05
                else                                        -> baseVel * 0.72
            }
            hits.add(Triple(b, hit, vel))
            b += stepBeats
        }
        drumPattern(ctx, startSample, hits, swing)
    }

    /** Snare/tom turnaround fill over the last [beats] of a phrase. */
    private fun drumFill(ctx: Ctx, startSample: Int, beats: Double, energetic: Boolean) {
        val steps = (beats * 4).toInt()                    // 16th notes
        val toms = doubleArrayOf(196.0, 165.0, 131.0)
        for (i in 0 until steps) {
            val s = startSample + (i * 0.25 * ctx.spb).toInt()
            val vel = 0.32 + 0.35 * i / steps              // crescendo into the downbeat
            when {
                !energetic && i % 2 == 1 -> {}             // sparser fill for calm styles
                i % 4 == 3 -> tom(ctx, s, toms[(i / 4) % 3], vel + 0.1, pan = 0.3 - 0.3 * (i.toDouble() / steps))
                else       -> snare(ctx, s, vel * 0.8)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Arrangement framework
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Walks the buffer phrase by phrase. Section plan:
     * phrase 0 = sparse intro (export only), then A/B alternating; energy
     * tiers up from phrase 4 so long videos keep building instead of looping.
     */
    private inline fun forEachPhrase(
        ctx: Ctx, phraseBeats: Double, loopMode: Boolean,
        body: (start: Int, section: Char, energy: Double, fill: Boolean) -> Unit
    ) {
        val phraseSamples = (phraseBeats * ctx.spb).roundToInt()
        var idx = 0
        var start = 0
        while (start < ctx.total) {
            val section = when {
                !loopMode && idx == 0 -> 'I'
                idx % 2 == 1          -> 'B'
                else                  -> 'A'
            }
            val energy = when {
                section == 'I' -> 0.8
                idx >= 4       -> 1.15
                else           -> 1.0
            }
            val fill = section == 'B' || section == 'I'   // turnaround into next A
            body(start, section, energy, fill)
            start += phraseSamples
            idx++
        }
    }

    private fun MutableList<Triple<Double, Hit, Double>>.hit(beat: Double, h: Hit, v: Double) {
        add(Triple(beat, h, v))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FILMY — Tollywood mass groove, E minor, 128 BPM, supersaw + teenmaar kit
    // ═══════════════════════════════════════════════════════════════════════
    private fun buildFilmy(ctx: Ctx, loopMode: Boolean) {
        val melodyA = seq(
            "E5" to 0.5, "G5" to 0.25, "A5" to 0.25, "B5" to 0.75, "A5" to 0.25,
            "G5" to 0.5, "A5" to 0.5, "B5" to 0.5, "D6" to 0.5,
            "B5" to 0.75, "A5" to 0.25, "G5" to 0.5, "E5" to 0.5,
            "G5" to 1.0, "R" to 1.0
        )
        val melodyB = seq(
            "D6" to 0.5, "B5" to 0.25, "A5" to 0.25, "B5" to 0.5, "G5" to 0.5,
            "A5" to 0.5, "G5" to 0.25, "E5" to 0.25, "G5" to 0.5, "E5" to 0.5,
            "D5" to 0.5, "E5" to 0.5, "G5" to 0.5, "A5" to 0.5,
            "B5" to 1.5, "R" to 0.5
        )
        val bassA = seq(
            "E2" to 0.5, "E3" to 0.5, "E2" to 0.5, "E3" to 0.5,
            "G2" to 0.5, "G3" to 0.5, "A2" to 0.5, "A3" to 0.5,
            "E2" to 0.5, "E3" to 0.5, "E2" to 0.5, "E3" to 0.5,
            "D3" to 0.5, "D3" to 0.5, "B2" to 0.5, "B2" to 0.5
        )
        val stabChords = listOf(
            0.0 to listOf("E4", "G4", "B4"), 1.75 to listOf("E4", "G4", "B4"),
            3.5 to listOf("D4", "A4", "D5"), 4.0 to listOf("E4", "G4", "B4"),
            5.75 to listOf("C4", "G4", "C5"), 7.0 to listOf("D4", "A4", "D5")
        )

        forEachPhrase(ctx, 8.0, loopMode) { start, section, energy, fill ->
            val melody = if (section == 'B') melodyB else melodyA
            melodyLine(ctx, Timbre.SAW, melody, start, 0.34 * energy, pan = 0.0,
                glide = true, vibrato = true)
            if (energy > 1.05) {   // octave doubler joins as the song builds
                melodyLine(ctx, Timbre.PLUCK, melody, start, 0.16, pan = -0.35, octaveShift = -1)
            }
            if (section != 'I') bassLine(ctx, bassA, start, 0.42)

            // brass-ish square stabs on the A sections
            if (section == 'A') {
                for ((beat, notes) in stabChords) {
                    val s = start + (beat * ctx.spb).toInt()
                    val d = (0.22 * ctx.spb).toInt()
                    for (n in notes) synthLead(ctx, f(n), 0.0, s, d, 0.10, 0.3, square = true,
                        vibrato = false, fStart = 6000.0, fSus = 3000.0, fDecay = 14.0)
                }
            }

            val hits = ArrayList<Triple<Double, Hit, Double>>()
            if (section == 'I') {
                // warm pads fill out the sparse intro under the lead
                pad(ctx, listOf(f("E4"), f("G4"), f("B4")), start, (3.8 * ctx.spb).toInt(), 0.14)
                pad(ctx, listOf(f("D4"), f("Gb4"), f("A4")),
                    start + (4.0 * ctx.spb).toInt(), (3.8 * ctx.spb).toInt(), 0.14)
                for (half in 0..1) {
                    val o = half * 4.0
                    hits.hit(o + 0.75, Hit.DHOL_HI, 0.35); hits.hit(o + 1.5, Hit.DHOL_HI, 0.3)
                    hits.hit(o + 1.0, Hit.CLAP, 0.4); hits.hit(o + 3.0, Hit.CLAP, 0.4)
                }
            } else {
                for (half in 0..1) {
                    val o = half * 4.0
                    for (b in 0..3) hits.hit(o + b.toDouble(), Hit.KICK, 0.9)
                    hits.hit(o + 0.75, Hit.DHOL_HI, 0.42); hits.hit(o + 1.5, Hit.DHOL_HI, 0.36)
                    hits.hit(o + 2.75, Hit.DHOL_HI, 0.42); hits.hit(o + 3.5, Hit.DHOL_LO, 0.5)
                    hits.hit(o + 1.0, Hit.SNARE, 0.5); hits.hit(o + 1.0, Hit.CLAP, 0.42)
                    hits.hit(o + 3.0, Hit.SNARE, 0.55); hits.hit(o + 3.0, Hit.CLAP, 0.45)
                    hits.hit(o + 3.5, Hit.OHAT, 0.26)
                }
                hatCarpet(ctx, start, 8.0, Hit.HAT,
                    stepBeats = if (energy > 1.05) 0.25 else 0.5, baseVel = 0.24, swing = 0.25)
                hatCarpet(ctx, start, 8.0, Hit.SHAKER, stepBeats = 0.5, baseVel = 0.30, swing = 0.25)
            }
            if (section == 'A' && !loopMode) crash(ctx, start, 0.35)
            drumPattern(ctx, start, hits, swing = 0.25)
            if (fill) drumFill(ctx, start + (7.0 * ctx.spb).toInt(), 1.0, energetic = true)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FOLK — Telugu janapada, G major pentatonic, 140 BPM, plucked strings +
    // dappu/dholak drive
    // ═══════════════════════════════════════════════════════════════════════
    private fun buildFolk(ctx: Ctx, loopMode: Boolean) {
        val melodyA = seq(
            "G4" to 0.25, "A4" to 0.25, "B4" to 0.25, "D5" to 0.25, "B4" to 0.5, "A4" to 0.5,
            "G4" to 0.25, "A4" to 0.25, "G4" to 0.25, "E4" to 0.25, "G4" to 1.0,
            "A4" to 0.25, "B4" to 0.25, "D5" to 0.25, "E5" to 0.25, "D5" to 0.5, "B4" to 0.5,
            "A4" to 0.25, "G4" to 0.25, "A4" to 0.25, "B4" to 0.25, "A4" to 1.0
        )
        val melodyB = seq(
            "D5" to 0.25, "E5" to 0.25, "G5" to 0.5, "E5" to 0.25, "D5" to 0.25, "B4" to 0.5,
            "D5" to 0.25, "E5" to 0.25, "D5" to 0.25, "B4" to 0.25, "D5" to 1.0,
            "B4" to 0.25, "D5" to 0.25, "B4" to 0.25, "A4" to 0.25, "G4" to 0.5, "A4" to 0.5,
            "B4" to 0.25, "A4" to 0.25, "G4" to 0.25, "E4" to 0.25, "G4" to 1.0
        )
        val bass = seq(
            "G2" to 0.5, "G3" to 0.5, "G2" to 0.5, "G3" to 0.5,
            "C3" to 0.5, "C3" to 0.5, "D3" to 0.5, "D3" to 0.5,
            "G2" to 0.5, "G3" to 0.5, "E3" to 0.5, "E3" to 0.5,
            "C3" to 0.5, "D3" to 0.5, "G2" to 1.0
        )

        forEachPhrase(ctx, 8.0, loopMode) { start, section, energy, fill ->
            val melody = if (section == 'B') melodyB else melodyA
            melodyLine(ctx, Timbre.PLUCK, melody, start, 0.52 * energy, pan = 0.1)
            if (energy > 1.05) {   // flute doubles the tune an octave up when it builds
                melodyLine(ctx, Timbre.FLUTE, melody, start, 0.14, pan = -0.3,
                    octaveShift = 1, glide = true)
            }
            if (section != 'I') bassLine(ctx, bass, start, 0.40)

            val hits = ArrayList<Triple<Double, Hit, Double>>()
            for (half in 0..1) {
                val o = half * 4.0
                if (section == 'I') {
                    hits.hit(o + 0.0, Hit.DHOL_LO, 0.4); hits.hit(o + 1.5, Hit.DHOL_HI, 0.32)
                    hits.hit(o + 2.0, Hit.DHOL_LO, 0.36); hits.hit(o + 3.0, Hit.CLAP, 0.4)
                } else {
                    // dappu / chindu-flavoured drive
                    hits.hit(o + 0.0, Hit.KICK, 0.85); hits.hit(o + 0.0, Hit.DHOL_LO, 0.5)
                    hits.hit(o + 0.75, Hit.DHOL_HI, 0.4); hits.hit(o + 1.0, Hit.SNARE, 0.45)
                    hits.hit(o + 1.5, Hit.DHOL_HI, 0.45); hits.hit(o + 2.0, Hit.KICK, 0.8)
                    hits.hit(o + 2.0, Hit.DHOL_LO, 0.45); hits.hit(o + 2.75, Hit.DHOL_HI, 0.4)
                    hits.hit(o + 3.0, Hit.SNARE, 0.5); hits.hit(o + 3.0, Hit.CLAP, 0.45)
                    hits.hit(o + 3.5, Hit.DHOL_HI, 0.42)
                }
            }
            if (section != 'I') {
                hatCarpet(ctx, start, 8.0, Hit.SHAKER, stepBeats = 0.25, baseVel = 0.26, swing = 0.35)
                if (section == 'A' && !loopMode) crash(ctx, start, 0.3)
            }
            drumPattern(ctx, start, hits, swing = 0.35)
            if (fill) drumFill(ctx, start + (7.0 * ctx.spb).toInt(), 1.0, energetic = true)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CLASSICAL — Shankarabharanam-flavoured, C major, 88 BPM.
    // Flute with meend + veena (pluck) response over tanpura and soft keherwa.
    // ═══════════════════════════════════════════════════════════════════════
    private fun buildClassical(ctx: Ctx, loopMode: Boolean) {
        val melodyA = seq(
            "E5" to 1.0, "D5" to 0.5, "C5" to 0.5, "B4" to 0.5, "C5" to 0.5, "D5" to 0.5, "E5" to 0.5,
            "E5" to 0.5, "F5" to 0.5, "G5" to 1.0, "A5" to 0.5, "G5" to 0.5, "F5" to 0.5, "E5" to 0.5,
            "D5" to 0.5, "E5" to 0.5, "F5" to 0.5, "D5" to 0.5, "E5" to 1.5, "R" to 0.5,
            "C5" to 0.5, "D5" to 0.5, "E5" to 0.5, "C5" to 0.5, "D5" to 1.0, "C5" to 1.0
        )
        val melodyB = seq(
            "G5" to 1.0, "A5" to 0.5, "G5" to 0.5, "F5" to 0.5, "G5" to 0.5, "A5" to 0.5, "B5" to 0.5,
            "C6" to 1.0, "B5" to 0.5, "A5" to 0.5, "G5" to 1.0, "E5" to 1.0,
            "F5" to 0.5, "G5" to 0.5, "A5" to 0.5, "F5" to 0.5, "G5" to 1.5, "R" to 0.5,
            "E5" to 0.5, "F5" to 0.5, "E5" to 0.5, "D5" to 0.5, "C5" to 2.0
        )
        // veena-style arpeggio response, follows I-IV-V-I movement
        val veena = seq(
            "C4" to 0.5, "E4" to 0.5, "G4" to 0.5, "C5" to 0.5,
            "C4" to 0.5, "E4" to 0.5, "G4" to 0.5, "E4" to 0.5,
            "F4" to 0.5, "A4" to 0.5, "C5" to 0.5, "A4" to 0.5,
            "G4" to 0.5, "B4" to 0.5, "D5" to 0.5, "B4" to 0.5,
            "C4" to 0.5, "E4" to 0.5, "G4" to 0.5, "C5" to 0.5,
            "F4" to 0.5, "A4" to 0.5, "G4" to 0.5, "B4" to 0.5,
            "C5" to 0.5, "G4" to 0.5, "E4" to 0.5, "G4" to 0.5,
            "C4" to 1.0, "G3" to 1.0
        )

        forEachPhrase(ctx, 16.0, loopMode) { start, section, energy, _ ->
            val melody = if (section == 'B') melodyB else melodyA
            melodyLine(ctx, Timbre.FLUTE, melody, start, 0.42 * energy, pan = -0.15, glide = true)
            melodyLine(ctx, Timbre.PLUCK, veena, start,
                if (section == 'I') 0.18 else 0.28, pan = 0.4)
            tanpura(ctx, start, 16.0)

            if (section != 'I') {
                kehrwaTabla(ctx, start, beats = 16.0, vol = if (energy > 1.05) 0.4 else 0.32)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEVOTIONAL — temple atmosphere, C major, 78 BPM.
    // FM bells + flute over tanpura drone and gentle keherwa tabla.
    // ═══════════════════════════════════════════════════════════════════════
    private fun buildDevotional(ctx: Ctx, loopMode: Boolean) {
        val bellsA = seq(
            "C5" to 2.0, "E5" to 1.0, "G5" to 1.0, "E5" to 2.0, "D5" to 1.0, "C5" to 1.0,
            "E5" to 2.0, "G5" to 1.0, "A5" to 1.0, "G5" to 2.0, "E5" to 1.0, "D5" to 1.0
        )
        val fluteB = seq(
            "G5" to 1.5, "A5" to 0.5, "G5" to 1.0, "E5" to 1.0,
            "F5" to 1.0, "E5" to 0.5, "D5" to 0.5, "E5" to 2.0,
            "C5" to 1.0, "D5" to 0.5, "E5" to 0.5, "G5" to 1.5, "E5" to 0.5,
            "D5" to 1.0, "C5" to 3.0
        )

        forEachPhrase(ctx, 16.0, loopMode) { start, section, energy, _ ->
            if (section == 'B') {
                melodyLine(ctx, Timbre.FLUTE, fluteB, start, 0.40 * energy, pan = -0.2, glide = true)
                melodyLine(ctx, Timbre.BELL, bellsA, start, 0.14, pan = 0.35)   // bells answer softly
            } else {
                melodyLine(ctx, Timbre.BELL, bellsA, start, 0.42 * energy, pan = 0.25)
                // soft sine shadow keeps the bell sections from feeling empty
                melodyLine(ctx, Timbre.SINE, bellsA, start, 0.12, pan = -0.2,
                    octaveShift = -1, vibrato = true)
                if (energy > 1.05) {
                    melodyLine(ctx, Timbre.FLUTE, bellsA, start, 0.14, pan = -0.3, glide = true)
                }
            }
            // big temple bell marks the start of every phrase
            bell(ctx, f("C4"), start, (SAMPLE_RATE * 2.5).toInt(),
                if (section == 'I') 0.20 else 0.14, -0.1)
            tanpura(ctx, start, 16.0)
            if (section != 'I') kehrwaTabla(ctx, start, beats = 16.0, vol = 0.30)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INSTRUMENTAL — cinematic fusion, A minor, 110 BPM.
    // Supersaw lead + pads + pluck arps, full kit with builds and fills.
    // ═══════════════════════════════════════════════════════════════════════
    private fun buildInstrumental(ctx: Ctx, loopMode: Boolean) {
        val leadA = seq(
            "A4" to 0.5, "C5" to 0.5, "E5" to 0.75, "D5" to 0.25, "C5" to 0.5, "B4" to 0.5,
            "C5" to 0.5, "A4" to 0.5,
            "G4" to 0.5, "A4" to 0.5, "B4" to 0.75, "C5" to 0.25, "B4" to 0.5, "G4" to 0.5,
            "A4" to 1.0
        )
        val leadB = seq(
            "E5" to 0.5, "G5" to 0.5, "A5" to 0.75, "G5" to 0.25, "E5" to 0.5, "D5" to 0.5,
            "E5" to 0.5, "C5" to 0.5,
            "D5" to 0.5, "E5" to 0.5, "G5" to 0.5, "E5" to 0.5, "D5" to 0.5, "C5" to 0.5,
            "A4" to 1.0
        )
        val arp = seq(
            "A3" to 0.25, "C4" to 0.25, "E4" to 0.25, "A4" to 0.25,
            "A3" to 0.25, "C4" to 0.25, "E4" to 0.25, "C4" to 0.25,
            "F3" to 0.25, "A3" to 0.25, "C4" to 0.25, "F4" to 0.25,
            "G3" to 0.25, "B3" to 0.25, "D4" to 0.25, "G4" to 0.25,
            "A3" to 0.25, "C4" to 0.25, "E4" to 0.25, "A4" to 0.25,
            "F3" to 0.25, "A3" to 0.25, "C4" to 0.25, "F4" to 0.25,
            "C4" to 0.25, "E4" to 0.25, "G4" to 0.25, "E4" to 0.25,
            "G3" to 0.25, "B3" to 0.25, "D4" to 0.25, "B3" to 0.25
        )
        val bass = seq(
            "A2" to 0.5, "A2" to 0.5, "A2" to 0.5, "E3" to 0.5,
            "F2" to 0.5, "F2" to 0.5, "C3" to 0.5, "C3" to 0.5,
            "A2" to 0.5, "A2" to 0.5, "E3" to 0.5, "E3" to 0.5,
            "G2" to 0.5, "G2" to 0.5, "D3" to 0.5, "B2" to 0.5
        )
        val padProg = listOf(
            0.0 to listOf("A3", "C4", "E4"), 2.0 to listOf("F3", "A3", "C4"),
            4.0 to listOf("C4", "E4", "G4"), 6.0 to listOf("G3", "B3", "D4")
        )

        forEachPhrase(ctx, 8.0, loopMode) { start, section, energy, fill ->
            val lead = if (section == 'B') leadB else leadA
            melodyLine(ctx, Timbre.SAW, lead, start, 0.30 * energy, pan = 0.0,
                glide = true, vibrato = true)
            for ((beat, notes) in padProg) {
                val s = start + (beat * ctx.spb).toInt()
                pad(ctx, notes.map { f(it) }, s, (1.9 * ctx.spb).toInt(), 0.11)
            }
            if (energy > 1.05 || section == 'B') {
                melodyLine(ctx, Timbre.PLUCK, arp, start, 0.20, pan = -0.35)
            }
            if (section != 'I') bassLine(ctx, bass, start, 0.40)

            val hits = ArrayList<Triple<Double, Hit, Double>>()
            if (section == 'I') {
                hits.hit(1.0, Hit.SNARE, 0.3); hits.hit(3.0, Hit.CLAP, 0.35)
                hits.hit(5.0, Hit.SNARE, 0.3); hits.hit(7.0, Hit.CLAP, 0.35)
            } else {
                for (half in 0..1) {
                    val o = half * 4.0
                    hits.hit(o + 0.0, Hit.KICK, 0.85); hits.hit(o + 2.5, Hit.KICK, 0.7)
                    hits.hit(o + 1.0, Hit.SNARE, 0.5)
                    hits.hit(o + 3.0, Hit.SNARE, 0.55); hits.hit(o + 3.0, Hit.CLAP, 0.4)
                    hits.hit(o + 3.75, Hit.OHAT, 0.22)
                }
                hatCarpet(ctx, start, 8.0, Hit.HAT,
                    stepBeats = if (energy > 1.05) 0.25 else 0.5, baseVel = 0.22, swing = 0.15)
                if (section == 'A' && !loopMode) crash(ctx, start, 0.32)
            }
            drumPattern(ctx, start, hits, swing = 0.15)
            if (fill) drumFill(ctx, start + (7.0 * ctx.spb).toInt(), 1.0, energetic = energy > 1.05)
        }
    }

    // ─── Shared Indian idiom layers ──────────────────────────────────────────

    /** Tanpura-style drone: long-ringing low plucks cycling Sa-Pa-Sa-Sa. */
    private fun tanpura(ctx: Ctx, start: Int, beats: Double) {
        val cycle = listOf("G2", "C3", "C3", "C2")
        var b = 0.0
        var i = 0
        while (b < beats - 1e-6) {
            val s = start + (b * ctx.spb).toInt()
            pluck(ctx, f(cycle[i % 4]), s, (2.2 * ctx.spb).toInt(), 0.14,
                pan = -0.5, damping = 0.9985, brightness = 0.85)
            b += 1.0
            i++
        }
    }

    /** Keherwa theka (8-beat cycle): dha ge na tin | na ge dha na. */
    private fun kehrwaTabla(ctx: Ctx, start: Int, beats: Double, vol: Double) {
        val cycle = listOf(
            0.0 to Hit.DHA, 0.5 to Hit.GE, 1.0 to Hit.NA, 1.5 to Hit.TIN,
            2.0 to Hit.NA, 2.5 to Hit.GE, 3.0 to Hit.DHA, 3.5 to Hit.NA
        )
        val hits = ArrayList<Triple<Double, Hit, Double>>()
        var o = 0.0
        while (o < beats - 1e-6) {
            for ((b, h) in cycle) {
                val accent = if (b == 0.0) 1.2 else if (b == 3.0) 1.1 else 0.9
                hits.hit(o + b, h, vol * accent)
            }
            o += 4.0
        }
        drumPattern(ctx, start, hits, swing = 0.1)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Mix FX
    // ═══════════════════════════════════════════════════════════════════════

    /** Kick-triggered ducking of the musical bed — the "pump" that keeps the groove alive. */
    private fun applySidechainDuck(music: DoubleArray, kickOnsets: List<Int>, total: Int) {
        if (kickOnsets.isEmpty()) return
        val attack  = (SAMPLE_RATE * 0.008).toInt().coerceAtLeast(1)
        val hold    = (SAMPLE_RATE * 0.015).toInt()
        val release = (SAMPLE_RATE * 0.140).toInt()
        val depth = 0.5
        val gain = DoubleArray(total) { 1.0 }
        for (onset in kickOnsets) {
            if (onset < 0 || onset >= total) continue
            val dipEnd = onset + attack
            val holdEnd = dipEnd + hold
            val relEnd = holdEnd + release
            for (i in onset until min(relEnd, total)) {
                val g = when {
                    i < dipEnd  -> 1.0 - (1.0 - depth) * (i - onset).toDouble() / attack
                    i < holdEnd -> depth
                    else        -> depth + (1.0 - depth) * (i - holdEnd).toDouble() / release
                }
                if (g < gain[i]) gain[i] = g
            }
        }
        for (i in 0 until total) music[i] *= gain[i]
    }

    /** Feed-forward peak compressor for bus glue. */
    private fun applyCompressor(buf: DoubleArray, total: Int) {
        val threshold = 0.5
        val ratio = 3.0
        val atkC = exp(-1.0 / (SAMPLE_RATE * 0.003))
        val relC = exp(-1.0 / (SAMPLE_RATE * 0.100))
        var env = 0.0
        for (i in 0 until total) {
            val a = abs(buf[i])
            env = if (a > env) atkC * env + (1 - atkC) * a else relC * env + (1 - relC) * a
            if (env > threshold) {
                buf[i] *= (threshold + (env - threshold) / ratio) / env
            }
        }
    }

    /** Schroeder reverb — parallel combs + allpass; comb lengths differ per channel. */
    private fun applyReverb(dry: DoubleArray, total: Int, channelSeed: Int, feedback: Double): DoubleArray {
        val combMs = listOf(29.7, 37.1, 41.3, 43.7, 50.9, 56.3)
        val combLengths = combMs.map {
            ((it + channelSeed * 1.1) * SAMPLE_RATE / 1000.0).toInt().coerceAtLeast(8)
        }
        val allpassLen = ((5.0 + channelSeed * 0.4) * SAMPLE_RATE / 1000.0).toInt().coerceAtLeast(4)
        val apFb = 0.5

        val combSum = DoubleArray(total)
        for (len in combLengths) {
            val buf = DoubleArray(len)
            var damp = 0.0
            var idx = 0
            for (i in 0 until total) {
                val delayed = buf[idx]
                damp += 0.35 * (delayed - damp)      // darker tail = more natural
                buf[idx] = dry[i] + damp * feedback
                combSum[i] += delayed
                idx = (idx + 1) % len
            }
        }

        val apBuf = DoubleArray(allpassLen)
        var apIdx = 0
        val out = DoubleArray(total)
        for (i in 0 until total) {
            val bufOut = apBuf[apIdx]
            val input = combSum[i] / combLengths.size
            val vn = input - apFb * bufOut
            apBuf[apIdx] = vn
            out[i] = bufOut + apFb * vn
            apIdx = (apIdx + 1) % allpassLen
        }
        return out
    }

    /** Deterministic pseudo-noise — reproducible percussion transients. */
    private fun pseudoNoise(i: Int, seed: Int): Double {
        var x = i xor seed
        x = x xor (x shl 13)
        x = x xor (x ushr 7)
        x = x xor (x shl 17)
        return (x and 0x7FFFFFFF).toDouble() / 0x7FFFFFFF * 2.0 - 1.0
    }
}
