package com.statusmaker.videoapp.video

import com.statusmaker.videoapp.data.model.MusicStyle
import kotlin.math.*
import kotlin.random.Random

/**
 * Multi-track audio synthesizer — no audio files required.
 *
 * Production techniques applied (this is what separates "MIDI demo" from
 * something that sounds produced/exciting):
 *  - Detuned unison oscillators on melody/bass/lead (the classic "supersaw"
 *    trick — three voices slightly out of tune instead of one thin voice)
 *  - Per-note filter envelopes (bright pluck settling into a darker
 *    sustain — real synth-bass/pluck character instead of a static tone)
 *  - Sidechain ducking: the musical layer dips on every kick hit and
 *    springs back, the "pump" that makes a mix feel alive
 *  - Humanized timing/velocity (tiny per-note jitter — nothing is
 *    robotically identical)
 *  - 808-style metallic hi-hats (6 inharmonic partials, not just filtered
 *    noise) + a real clap layer
 *  - Master bus compression for glue, then a genuine stereo reverb send
 *    (decorrelated L/R tails, dry signal stays centered)
 */
object AudioSynthesizer {

    const val SAMPLE_RATE = 44100
    private const val TWO_PI = 2.0 * Math.PI

    // ─── Note frequency table ─────────────────────────────────────────────────

    private val FREQ = mapOf(
        "C2" to 65.41, "G2" to 98.00, "A2" to 110.00, "Bb2" to 116.54,
        "C3" to 130.81, "D3" to 146.83, "E3" to 164.81, "F3" to 174.61,
        "G3" to 196.00, "A3" to 220.00, "Bb3" to 233.08, "B3" to 246.94,
        "C4" to 261.63, "D4" to 293.66, "Eb4" to 311.13, "E4" to 329.63,
        "F4" to 349.23, "G4" to 392.00, "Ab4" to 415.30, "A4" to 440.00,
        "Bb4" to 466.16, "B4" to 493.88,
        "C5" to 523.25, "D5" to 587.33, "Eb5" to 622.25, "E5" to 659.25,
        "F5" to 698.46, "G5" to 783.99, "Ab5" to 830.61, "A5" to 880.00,
        "Bb5" to 932.33, "B5" to 987.77,
        "C6" to 1046.50, "D6" to 1174.66, "E6" to 1318.51,
        "R" to 0.0
    )

    // ─── Public entry point ───────────────────────────────────────────────────

    fun generate(style: MusicStyle, durationSeconds: Int): ShortArray {
        val total = durationSeconds * SAMPLE_RATE
        if (style == MusicStyle.NONE) return ShortArray(total * 2)

        // Two separate layers: drums get summed in AFTER sidechain ducking
        // is applied to everything else — that's what makes the kick punch
        // through instead of just sitting in a static, flat-sounding pile.
        val music = DoubleArray(total)
        val drums = DoubleArray(total)
        val kickOnsets = mutableListOf<Int>()

        when (style) {
            MusicStyle.FILMY        -> buildFilmy(music, drums, kickOnsets, total)
            MusicStyle.FOLK         -> buildFolk(music, drums, kickOnsets, total)
            MusicStyle.CLASSICAL    -> buildClassical(music, drums, kickOnsets, total)
            MusicStyle.DEVOTIONAL   -> buildDevotional(music, drums, kickOnsets, total)
            MusicStyle.INSTRUMENTAL -> buildInstrumental(music, drums, kickOnsets, total)
            MusicStyle.NONE -> {}
        }

        applySidechainDuck(music, kickOnsets, total)

        val dry = DoubleArray(total) { music[it] + drums[it] }
        applyCompressor(dry, total)

        val peak = dry.maxOfOrNull { abs(it) }?.coerceAtLeast(0.001) ?: 1.0
        for (i in 0 until total) dry[i] /= peak

        // Stereo reverb — different comb-delay lengths per channel give a
        // real stereo tail; the dry signal itself stays identical in both
        // channels (centered) so there's no phase cancellation.
        val reverbL = applyReverb(dry, total, channelSeed = 0)
        val reverbR = applyReverb(dry, total, channelSeed = 1)
        val wetMix = 0.22

        val out = ShortArray(total * 2)
        for (i in 0 until total) {
            val left  = tanh((dry[i] + reverbL[i] * wetMix) * 0.88) * 0.92
            val right = tanh((dry[i] + reverbR[i] * wetMix) * 0.88) * 0.92
            out[i * 2]     = (left  * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
            out[i * 2 + 1] = (right * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return out
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILMY — Bollywood/Tollywood disco groove  BPM=128, 4/4
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildFilmy(music: DoubleArray, drums: DoubleArray, kicks: MutableList<Int>, total: Int) {
        val bpm = 128.0
        val spb = spb(bpm)

        val melody = listOf(
            "E5" to 0.25, "R" to 0.25,  "G5" to 0.25, "A5" to 0.25,
            "B5" to 0.50,             "A5" to 0.25, "G5" to 0.25,
            "E5" to 0.25, "G5" to 0.25,  "E5" to 0.25, "D5" to 0.25,
            "E5" to 1.0,
            "D5" to 0.25, "E5" to 0.25,  "G5" to 0.50,
            "A5" to 0.25, "G5" to 0.25,  "E5" to 0.50,
            "D5" to 0.25, "E5" to 0.25,  "D5" to 0.25, "B4" to 0.25,
            "E5" to 1.50,            "R" to 0.50
        )
        val melodyBeats = melody.sumOf { it.second }

        val bass = listOf(
            "E3" to 0.5,"E3" to 0.5, "G3" to 0.5,"G3" to 0.5,
            "A3" to 0.5,"A3" to 0.5, "G3" to 0.5,"B3" to 0.5
        )
        val bassBeats = bass.sumOf { it.second }

        val chords = listOf(
            listOf("E4","B4") to 0.0,
            listOf("G4","D5") to 2.0,
            listOf("A4","E5") to 4.0,
            listOf("G4","D5") to 6.0
        )

        var pos = 0
        while (pos < total) {
            scheduleSeq(music, pos, spb, melody, melodyBeats, 0.55, "saw", vibrato = true,
                filterStartHz = 9500.0, filterSustainHz = 6500.0, filterDecayRate = 4.0)
            scheduleSeq(music, pos, spb, bass, bassBeats, 0.52, "saw",
                filterStartHz = 3200.0, filterSustainHz = 900.0, filterDecayRate = 11.0, unisonDetune = 0.002)
            for ((noteList, beatOff) in chords) {
                val start = pos + (beatOff * spb).toInt()
                val dur = (0.22 * spb).toInt()
                for (n in noteList) note(music, FREQ[n]!!, start, dur, 0.25, "square")
            }
            drumLoop(drums, kicks, pos, spb, total,
                kickPat   = listOf(0.0,0.75,1.0,1.75, 2.0,2.75,3.0,3.5),
                snarePat  = listOf(1.0, 3.0),
                clapPat   = listOf(1.0, 3.0),
                hihatPat  = (0 until 16).map { it * 0.5 },
                open      = listOf(1.0, 3.0, 3.5)
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FOLK — Telugu Janapada, fast pentatonic  BPM=138, 4/4
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildFolk(music: DoubleArray, drums: DoubleArray, kicks: MutableList<Int>, total: Int) {
        val bpm = 138.0
        val spb = spb(bpm)

        val melody = listOf(
            "G5" to 0.25,"A5" to 0.25,"C6" to 0.25,"D6" to 0.25,
            "C6" to 0.50,           "A5" to 0.25,"G5" to 0.25,
            "E5" to 0.25,"G5" to 0.25,"A5" to 0.25,"C6" to 0.25,
            "G5" to 1.0,
            "A5" to 0.25,"C6" to 0.25,"D6" to 0.25,"E6" to 0.25,
            "D6" to 0.50,           "C6" to 0.50,
            "A5" to 0.25,"G5" to 0.25,"E5" to 0.25,"D5" to 0.25,
            "G5" to 1.50,          "R" to 0.50
        )
        val mBeats = melody.sumOf { it.second }

        val bass = listOf(
            "G3" to 0.5,"G3" to 0.5,"A3" to 0.5,"C4" to 0.5,
            "D4" to 0.5,"C4" to 0.5,"A3" to 0.5,"G3" to 0.5
        )
        val bBeats = bass.sumOf { it.second }

        val counter = listOf(
            "E5" to 0.50,"F5" to 0.50,"E5" to 0.50,"D5" to 0.50,
            "C5" to 0.50,"D5" to 0.50,"C5" to 0.50,"A4" to 0.50
        )
        val cBeats = counter.sumOf { it.second }

        var pos = 0
        while (pos < total) {
            scheduleSeq(music, pos, spb, melody, mBeats, 0.55, "square", vibrato = false,
                filterStartHz = 9000.0, filterSustainHz = 7000.0, filterDecayRate = 5.0)
            scheduleSeq(music, pos, spb, counter, cBeats, 0.28, "sine", vibrato = false, unisonDetune = 0.0)
            scheduleSeq(music, pos, spb, bass, bBeats, 0.48, "saw",
                filterStartHz = 3000.0, filterSustainHz = 1000.0, filterDecayRate = 10.0, unisonDetune = 0.002)
            drumLoop(drums, kicks, pos, spb, total,
                kickPat  = listOf(0.0,0.5,2.0,2.5, 4.0,4.5,6.0,6.5),
                snarePat = listOf(1.0,3.0,5.0,7.0),
                clapPat  = listOf(3.0, 7.0),
                hihatPat = (0 until 32).map { it * 0.25 },
                open     = emptyList()
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLASSICAL — Indian Shankarabharanam (major), 3-voice  BPM=92
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildClassical(music: DoubleArray, drums: DoubleArray, kicks: MutableList<Int>, total: Int) {
        val bpm = 92.0
        val spb = spb(bpm)

        val upper = listOf(
            "E5" to 1.0,"D5" to 0.5,"C5" to 0.5,
            "B4" to 0.5,"C5" to 0.5,"D5" to 0.5,"E5" to 0.5,
            "E5" to 0.5,"E5" to 0.5,"F5" to 0.5,"G5" to 0.5,
            "A5" to 1.0,"G5" to 1.0,
            "F5" to 0.5,"E5" to 0.5,"D5" to 0.5,"C5" to 0.5,
            "B4" to 1.0,"G4" to 1.0,
            "C5" to 0.5,"E5" to 0.5,"G5" to 0.5,"C6" to 0.5,
            "B5" to 0.5,"A5" to 0.5,"G5" to 0.5,"E5" to 0.5,
            "F5" to 0.5,"A5" to 0.5,"C6" to 0.5,"E6" to 0.5,
            "D6" to 1.0,"C6" to 1.0
        )
        val uBeats = upper.sumOf { it.second }

        val middle = listOf(
            "C5" to 1.0,"B4" to 0.5,"A4" to 0.5,
            "G4" to 0.5,"A4" to 0.5,"B4" to 0.5,"C5" to 0.5,
            "C5" to 0.5,"C5" to 0.5,"D5" to 0.5,"E5" to 0.5,
            "F5" to 1.0,"E5" to 1.0,
            "D5" to 0.5,"C5" to 0.5,"B4" to 0.5,"A4" to 0.5,
            "G4" to 1.0,"E4" to 1.0,
            "E4" to 0.5,"G4" to 0.5,"C5" to 0.5,"E5" to 0.5,
            "G5" to 0.5,"F5" to 0.5,"E5" to 0.5,"C5" to 0.5,
            "A4" to 0.5,"C5" to 0.5,"E5" to 0.5,"A5" to 0.5,
            "G5" to 1.0,"E5" to 1.0
        )
        val mBeats = middle.sumOf { it.second }

        val bass = listOf(
            "C3" to 0.5,"R" to 0.5,"G3" to 0.5,"R" to 0.5,
            "F3" to 0.5,"R" to 0.5,"E3" to 0.5,"R" to 0.5,
            "A3" to 0.5,"R" to 0.5,"G3" to 0.5,"R" to 0.5,
            "C3" to 0.5,"R" to 0.5,"G2" to 0.5,"R" to 0.5,
            "F3" to 0.5,"R" to 0.5,"A3" to 0.5,"R" to 0.5,
            "E3" to 0.5,"R" to 0.5,"G3" to 0.5,"R" to 0.5,
            "C3" to 1.0,"G2" to 1.0,
            "C3" to 0.5,"R" to 0.5,"C3" to 0.5,"R" to 0.5
        )
        val bBeats = bass.sumOf { it.second }

        var pos = 0
        while (pos < total) {
            scheduleSeq(music, pos, spb, upper,  uBeats, 0.52, "sine", vibrato = true, unisonDetune = 0.0015)
            scheduleSeq(music, pos, spb, middle, mBeats, 0.36, "sine", vibrato = true, unisonDetune = 0.0015)
            scheduleSeq(music, pos, spb, bass,   bBeats, 0.50, "saw",
                filterStartHz = 3000.0, filterSustainHz = 1100.0, filterDecayRate = 9.0, unisonDetune = 0.002)
            pos += (uBeats * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DEVOTIONAL — temple bells + tanpura drone  BPM=72
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildDevotional(music: DoubleArray, drums: DoubleArray, kicks: MutableList<Int>, total: Int) {
        val bpm = 72.0
        val spb = spb(bpm)

        val bells = listOf(
            "C5" to 2.0,"E5" to 1.0,"F5" to 1.0,
            "G5" to 2.0,"B5" to 1.0,"C6" to 1.0,
            "B5" to 1.0,"G5" to 1.0,"E5" to 1.0,"C5" to 1.0,
            "F5" to 1.5,"E5" to 0.5,"D5" to 1.0,
            "C5" to 4.0
        )
        val bBeats = bells.sumOf { it.second }

        val bells2 = listOf(
            "G4" to 2.0,"C5" to 2.0,
            "E5" to 2.0,"G5" to 2.0,
            "E5" to 2.0,"C5" to 2.0,
            "D5" to 2.0,"G4" to 2.0,
            "C5" to 4.0
        )
        val b2Beats = bells2.sumOf { it.second }

        val drone = listOf("C3" to 2.0,"G3" to 2.0,"C4" to 2.0,"G4" to 2.0)
        val dBeats = drone.sumOf { it.second }

        val tabla = listOf(0.0, 1.0, 1.5, 2.0, 3.0, 3.5)

        var pos = 0
        while (pos < total) {
            scheduleSeq(music, pos, spb, bells,  bBeats,  0.60, "sine", vibrato = true,
                attackRatio = 0.02, releaseRatio = 0.50, unisonDetune = 0.0012)
            scheduleSeq(music, pos, spb, bells2, b2Beats, 0.35, "sine", vibrato = true,
                attackRatio = 0.02, releaseRatio = 0.50, unisonDetune = 0.0012)
            scheduleSeq(music, pos, spb, drone,  dBeats,  0.18, "sine", unisonDetune = 0.0)
            for (t in tabla) {
                val start = pos + (t * spb).toInt()
                tablaHit(drums, start, total, 0.35)
            }
            pos += (bBeats * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INSTRUMENTAL — cinematic/fusion groove  BPM=108
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildInstrumental(music: DoubleArray, drums: DoubleArray, kicks: MutableList<Int>, total: Int) {
        val bpm = 108.0
        val spb = spb(bpm)

        val lead = listOf(
            "A5" to 0.5,"G5" to 0.25,"E5" to 0.25, "A5" to 0.5,"C6" to 0.5,
            "D6" to 0.5,"C6" to 0.25,"A5" to 0.25, "G5" to 1.0,
            "E5" to 0.5,"G5" to 0.5,            "A5" to 0.5,"C6" to 0.5,
            "B5" to 0.5,"A5" to 0.5,            "G5" to 1.0,
            "E5" to 0.25,"G5" to 0.25,"A5" to 0.25,"C6" to 0.25, "D6" to 0.5,"E6" to 0.5,
            "D6" to 0.5,"C6" to 0.25,"A5" to 0.25, "G5" to 0.5,"E5" to 0.5,
            "A5" to 1.5,                     "G5" to 0.5,
            "E5" to 1.0,                     "A5" to 1.0
        )
        val lBeats = lead.sumOf { it.second }

        val padChords = listOf(
            listOf("A4","C5","E5")  to 0.0,
            listOf("F4","A4","C5")  to 2.0,
            listOf("C4","E4","G4")  to 4.0,
            listOf("G4","B4","D5")  to 6.0
        )

        val bass = listOf(
            "A3" to 0.5,"C4" to 0.5,"E4" to 0.5,"G3" to 0.5,
            "F3" to 0.5,"A3" to 0.5,"C4" to 0.5,"E3" to 0.5,
            "C3" to 0.5,"E3" to 0.5,"G3" to 0.5,"B3" to 0.5,
            "G3" to 0.5,"B3" to 0.5,"D4" to 0.5,"F3" to 0.5
        )
        val bBeats = bass.sumOf { it.second }

        var pos = 0
        while (pos < total) {
            scheduleSeq(music, pos, spb, lead, lBeats, 0.58, "saw", vibrato = true,
                filterStartHz = 9500.0, filterSustainHz = 6800.0, filterDecayRate = 4.0)
            scheduleSeq(music, pos, spb, bass, bBeats, 0.50, "saw",
                filterStartHz = 3200.0, filterSustainHz = 950.0, filterDecayRate = 10.0, unisonDetune = 0.002)
            for ((notes, beatOff) in padChords) {
                val start = pos + (beatOff * spb).toInt()
                val dur = (1.8 * spb).toInt()
                for (n in notes) note(music, FREQ[n]!!, start, dur, 0.16, "sine", unisonDetune = 0.0015)
            }
            drumLoop(drums, kicks, pos, spb, total,
                kickPat  = listOf(0.0, 2.0, 3.5),
                snarePat = listOf(1.0, 3.0),
                clapPat  = listOf(3.0),
                hihatPat = (0 until 16).map { it * 0.5 },
                open     = listOf(2.0, 6.0)
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Core synthesis helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private fun spb(bpm: Double) = (SAMPLE_RATE * 60.0 / bpm)

    /**
     * Write one synthesized note into [music]. Adds unison detune (richness)
     * and a filter envelope (movement/character) on top of the base ADSR —
     * the two biggest levers for making a single oscillator sound "produced"
     * instead of flat.
     */
    private fun note(
        music: DoubleArray, freq: Double, startSample: Int, durationSamples: Int,
        volume: Double, waveform: String,
        vibrato: Boolean = false,
        attackRatio: Double = 0.06, decayRatio: Double = 0.10,
        sustainLevel: Double = 0.72, releaseRatio: Double = 0.22,
        unisonDetune: Double = 0.0035,
        filterStartHz: Double = 9000.0,
        filterSustainHz: Double = 9000.0,
        filterDecayRate: Double = 5.0
    ) {
        if (freq <= 0.0 || durationSamples <= 0) return
        val atk = (durationSamples * attackRatio).toInt().coerceAtLeast(1)
        val dec = (durationSamples * decayRatio).toInt().coerceAtLeast(1)
        val rel = (durationSamples * releaseRatio).toInt().coerceAtLeast(1)

        var filterState = 0.0
        for (i in 0 until durationSamples) {
            val idx = startSample + i
            if (idx < 0 || idx >= music.size) continue
            val t = i.toDouble() / SAMPLE_RATE
            val f = if (vibrato) freq * (1.0 + 0.0045 * sin(TWO_PI * 5.2 * t)) else freq

            val wave = if (unisonDetune > 0.0) {
                val w1 = waveformAt(waveform, f * (1.0 - unisonDetune), t)
                val w2 = waveformAt(waveform, f, t)
                val w3 = waveformAt(waveform, f * (1.0 + unisonDetune), t)
                (w1 * 0.5 + w2 * 1.0 + w3 * 0.5) / 2.0
            } else {
                waveformAt(waveform, f, t)
            }

            // One-pole lowpass with a time-varying cutoff: bright at onset,
            // settling toward the sustain cutoff. With matching start/sustain
            // values this is just a gentle fixed filter that doesn't audibly
            // change the existing tonal character — it only becomes a pluck
            // effect where the two values are deliberately set apart (bass).
            val cutoff = filterSustainHz + (filterStartHz - filterSustainHz) * exp(-filterDecayRate * t)
            val alpha = (1.0 - exp(-TWO_PI * cutoff / SAMPLE_RATE)).coerceIn(0.0, 1.0)
            filterState += alpha * (wave - filterState)

            val env = adsr(i, durationSamples, atk, dec, sustainLevel, rel)
            music[idx] += filterState * env * volume
        }
    }

    /** Schedule a looping sequence of (noteName, durationBeats) pairs, with humanization. */
    private fun scheduleSeq(
        music: DoubleArray, startSample: Int, spb: Double,
        seq: List<Pair<String, Double>>, loopBeats: Double,
        volume: Double, waveform: String, vibrato: Boolean = false,
        attackRatio: Double = 0.06, releaseRatio: Double = 0.22,
        unisonDetune: Double = 0.0035,
        filterStartHz: Double = 9000.0,
        filterSustainHz: Double = 9000.0,
        filterDecayRate: Double = 5.0
    ) {
        val loopSamples = (loopBeats * spb).toInt()
        if (loopSamples <= 0) return
        var loopStart = startSample
        while (loopStart < music.size) {
            var beatPos = 0.0
            for ((noteName, dur) in seq) {
                // Humanization: tiny timing/velocity jitter so repeated loops
                // never sound robotically identical to each other.
                val jitterSamples = (Random.nextDouble(-1.0, 1.0) * spb * 0.006).toInt()
                val velocityJitter = Random.nextDouble(0.92, 1.06)

                val s = loopStart + (beatPos * spb).toInt() + jitterSamples
                val d = (dur * spb).toInt()
                if (noteName != "R") {
                    note(music, FREQ[noteName] ?: 0.0, s, d, volume * velocityJitter, waveform, vibrato,
                        attackRatio = attackRatio, releaseRatio = releaseRatio,
                        unisonDetune = unisonDetune,
                        filterStartHz = filterStartHz, filterSustainHz = filterSustainHz,
                        filterDecayRate = filterDecayRate)
                }
                beatPos += dur
            }
            loopStart += loopSamples
        }
    }

    /** Full drum loop (kick + snare + clap + hi-hat). Records kick sample positions for sidechain. */
    private fun drumLoop(
        drums: DoubleArray, kicks: MutableList<Int>, startSample: Int, spb: Double, total: Int,
        kickPat: List<Double>, snarePat: List<Double>, clapPat: List<Double>,
        hihatPat: List<Double>, open: List<Double>
    ) {
        val loopBeats = 8.0
        val loopSamples = (loopBeats * spb).toInt()
        var loopStart = startSample
        while (loopStart < total) {
            for (b in kickPat) {
                val s = loopStart + (b * spb).toInt()
                kickHit(drums, s, total)
                kicks.add(s)
            }
            for (b in snarePat) snareHit(drums, loopStart + (b * spb).toInt(), total)
            for (b in clapPat)  clapHit(drums, loopStart + (b * spb).toInt(), total)
            for (b in hihatPat) {
                val isOpen = open.any { abs(it - b) < 0.01 }
                hihatHit(drums, loopStart + (b * spb).toInt(), total, isOpen)
            }
            loopStart += loopSamples
        }
    }

    // ─── Sidechain ducking ────────────────────────────────────────────────────

    /**
     * Dips the musical layer on every kick hit and springs it back — the
     * "pump" that's the single biggest difference between a static-sounding
     * loop and something that feels alive and rhythmic.
     */
    private fun applySidechainDuck(music: DoubleArray, kickOnsets: List<Int>, total: Int) {
        if (kickOnsets.isEmpty()) return
        val attackSamples  = (SAMPLE_RATE * 0.008).toInt().coerceAtLeast(1)
        val holdSamples    = (SAMPLE_RATE * 0.015).toInt()
        val releaseSamples = (SAMPLE_RATE * 0.140).toInt()
        val duckDepth = 0.45

        val gain = DoubleArray(total) { 1.0 }
        for (onset in kickOnsets) {
            if (onset < 0 || onset >= total) continue
            val dipEnd = onset + attackSamples
            val holdEnd = dipEnd + holdSamples
            val releaseEnd = holdEnd + releaseSamples
            for (i in onset until min(releaseEnd, total)) {
                val g = when {
                    i < dipEnd  -> 1.0 - (1.0 - duckDepth) * (i - onset).toDouble() / attackSamples
                    i < holdEnd -> duckDepth
                    else -> duckDepth + (1.0 - duckDepth) * (i - holdEnd).toDouble() / releaseSamples
                }
                if (g < gain[i]) gain[i] = g
            }
        }
        for (i in 0 until total) music[i] *= gain[i]
    }

    // ─── Master bus compressor ────────────────────────────────────────────────

    /** Simple feed-forward peak compressor — glues the mix before the final limiter/reverb. */
    private fun applyCompressor(buf: DoubleArray, total: Int) {
        val threshold = 0.5
        val ratio = 3.0
        val attackCoeff  = exp(-1.0 / (SAMPLE_RATE * 0.003))
        val releaseCoeff = exp(-1.0 / (SAMPLE_RATE * 0.100))
        var envelope = 0.0
        for (i in 0 until total) {
            val inputAbs = abs(buf[i])
            envelope = if (inputAbs > envelope) attackCoeff * envelope + (1 - attackCoeff) * inputAbs
                       else releaseCoeff * envelope + (1 - releaseCoeff) * inputAbs
            if (envelope > threshold) {
                val excess = envelope - threshold
                val reducedExcess = excess / ratio
                val targetGain = (threshold + reducedExcess) / envelope
                buf[i] *= targetGain
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Stereo reverb (Schroeder-style: parallel comb filters + allpass diffusion)
    // ═══════════════════════════════════════════════════════════════════════════
    private fun applyReverb(dry: DoubleArray, total: Int, channelSeed: Int): DoubleArray {
        val combMs = listOf(29.7, 37.1, 41.3, 43.7)
        val combLengths = combMs.map {
            ((it + channelSeed * 0.8) * SAMPLE_RATE / 1000.0).toInt().coerceAtLeast(8)
        }
        val feedback = 0.34
        val allpassMs = 5.0 + channelSeed * 0.3
        val allpassLen = (allpassMs * SAMPLE_RATE / 1000.0).toInt().coerceAtLeast(4)
        val allpassFeedback = 0.5

        val combSum = DoubleArray(total)
        for (len in combLengths) {
            val buf = DoubleArray(len)
            var idx = 0
            for (i in 0 until total) {
                val delayed = buf[idx]
                val input = dry[i] + delayed * feedback
                buf[idx] = input
                combSum[i] += delayed
                idx = (idx + 1) % len
            }
        }

        val allpassBuf = DoubleArray(allpassLen)
        var apIdx = 0
        val diffused = DoubleArray(total)
        for (i in 0 until total) {
            val bufOut = allpassBuf[apIdx]
            val input = combSum[i] * 0.25
            val vn = input - allpassFeedback * bufOut
            allpassBuf[apIdx] = vn
            diffused[i] = bufOut + allpassFeedback * vn
            apIdx = (apIdx + 1) % allpassLen
        }
        return diffused
    }

    // ─── Percussion synthesis ─────────────────────────────────────────────────

    private fun kickHit(drums: DoubleArray, start: Int, total: Int) {
        val dur = (SAMPLE_RATE * 0.30).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx < 0 || idx >= total) continue
            val t = i.toDouble() / SAMPLE_RATE
            val f = 75.0 + 180.0 * exp(-35.0 * t)
            val env = exp(-7.0 * t)
            val click = if (i < 220) sin(TWO_PI * 3500.0 * t) * 0.3 * exp(-80.0 * t) else 0.0
            val raw = sin(TWO_PI * f * t) * 0.9 + click
            drums[idx] += tanh(raw * 1.15) * env * 0.85
        }
    }

    private fun snareHit(drums: DoubleArray, start: Int, total: Int) {
        val dur = (SAMPLE_RATE * 0.14).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx < 0 || idx >= total) continue
            val t = i.toDouble() / SAMPLE_RATE
            val noise = pseudoNoise(i, 0x5A5A)
            val tone = sin(TWO_PI * 220.0 * t) * exp(-30.0 * t)
            val body = sin(TWO_PI * 140.0 * t) * exp(-45.0 * t) * 0.4
            val env = exp(-22.0 * t)
            drums[idx] += (noise * 0.62 + tone * 0.30 + body) * env * 0.55
        }
    }

    /**
     * 808-style metallic hi-hat: 6 inharmonic square-wave partials summed,
     * not just filtered noise — this is what makes it read as a real
     * cymbal/hi-hat timbre instead of a hiss with an envelope on it.
     */
    private fun hihatHit(drums: DoubleArray, start: Int, total: Int, open: Boolean) {
        val maxT = if (open) 0.22 else 0.05
        val decay = if (open) 14.0 else 75.0
        val dur = (SAMPLE_RATE * maxT).toInt()
        val baseFreq = 40.0
        val ratios = doubleArrayOf(2.0, 3.0, 4.16, 5.43, 6.79, 8.21)
        for (i in 0 until dur) {
            val idx = start + i; if (idx < 0 || idx >= total) continue
            val t = i.toDouble() / SAMPLE_RATE
            var metal = 0.0
            for (r in ratios) metal += sign(sin(TWO_PI * baseFreq * r * t))
            metal /= ratios.size
            val noise = pseudoNoise(i, 0xBEEF) * 0.3
            val env = exp(-decay * t)
            drums[idx] += (metal * 0.75 + noise) * env * 0.28
        }
    }

    /** Layered noise bursts + tail — classic clap synthesis, used to thicken backbeats. */
    private fun clapHit(drums: DoubleArray, start: Int, total: Int) {
        val burstGap = (SAMPLE_RATE * 0.010).toInt()
        val burstDur = (SAMPLE_RATE * 0.012).toInt()
        repeat(3) { burstIdx ->
            val burstStart = start + burstIdx * burstGap
            for (i in 0 until burstDur) {
                val idx = burstStart + i; if (idx < 0 || idx >= total) continue
                val t = i.toDouble() / SAMPLE_RATE
                val n = pseudoNoise(idx, 0xC1A9)
                drums[idx] += n * exp(-40.0 * t) * 0.28
            }
        }
        val tailStart = start + 3 * burstGap
        val tailDur = (SAMPLE_RATE * 0.08).toInt()
        for (i in 0 until tailDur) {
            val idx = tailStart + i; if (idx < 0 || idx >= total) continue
            val t = i.toDouble() / SAMPLE_RATE
            val n = pseudoNoise(idx, 0xC1AA)
            drums[idx] += n * exp(-18.0 * t) * 0.20
        }
    }

    private fun tablaHit(drums: DoubleArray, start: Int, total: Int, vol: Double) {
        val dur = (SAMPLE_RATE * 0.12).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx < 0 || idx >= total) continue
            val t = i.toDouble() / SAMPLE_RATE
            val f = 280.0 + 120.0 * exp(-40.0 * t)
            drums[idx] += sin(TWO_PI * f * t) * exp(-25.0 * t) * vol
        }
    }

    // ─── Waveform generators ──────────────────────────────────────────────────

    private fun waveformAt(waveform: String, freq: Double, t: Double): Double = when (waveform) {
        "saw"    -> sawWave(freq, t)
        "square" -> squareWave(freq, t)
        else     -> sin(TWO_PI * freq * t)
    }

    private fun sawWave(freq: Double, t: Double): Double {
        if (freq <= 0) return 0.0
        var s = 0.0
        for (n in 1..7) s += sin(TWO_PI * freq * n * t) / n
        return s * (2.0 / Math.PI)
    }

    private fun squareWave(freq: Double, t: Double): Double {
        if (freq <= 0) return 0.0
        var s = 0.0
        var n = 1; while (n <= 9) { s += sin(TWO_PI * freq * n * t) / n; n += 2 }
        return s * (4.0 / Math.PI)
    }

    // ─── ADSR envelope ────────────────────────────────────────────────────────

    private fun adsr(
        i: Int, total: Int,
        atk: Int, dec: Int, sus: Double, rel: Int
    ): Double {
        val sustain = (total - atk - dec - rel).coerceAtLeast(0)
        return when {
            i < atk -> i.toDouble() / atk
            i < atk + dec -> 1.0 - (1.0 - sus) * (i - atk).toDouble() / dec
            i < atk + dec + sustain -> sus
            else -> sus * (1.0 - (i - atk - dec - sustain).toDouble() / rel.coerceAtLeast(1))
        }.coerceIn(0.0, 1.0)
    }

    // ─── Deterministic pseudo-noise (for percussion — reproducible per call) ──

    private fun pseudoNoise(i: Int, seed: Int): Double {
        var x = i xor seed
        x = x xor (x shl 13)
        x = x xor (x ushr 7)
        x = x xor (x shl 17)
        return (x and 0x7FFFFFFF).toDouble() / 0x7FFFFFFF * 2.0 - 1.0
    }
}
