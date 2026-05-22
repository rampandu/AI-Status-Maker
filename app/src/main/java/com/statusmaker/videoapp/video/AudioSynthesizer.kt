package com.statusmaker.videoapp.video

import com.statusmaker.videoapp.data.model.MusicStyle
import kotlin.math.*

/**
 * Multi-track audio synthesizer — no audio files required.
 * Generates: sawtooth/square melody + bass line + kick/snare/hihat drums.
 * Each MusicStyle has its own tempo, scale, and groove pattern.
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
        val mix = DoubleArray(total)

        when (style) {
            MusicStyle.FILMY        -> buildFilmy(mix, total)
            MusicStyle.FOLK         -> buildFolk(mix, total)
            MusicStyle.CLASSICAL    -> buildClassical(mix, total)
            MusicStyle.DEVOTIONAL   -> buildDevotional(mix, total)
            MusicStyle.INSTRUMENTAL -> buildInstrumental(mix, total)
            MusicStyle.NONE         -> {}
        }

        // Soft limiting + normalize
        val peak = mix.maxOfOrNull { abs(it) }?.coerceAtLeast(0.001) ?: 1.0
        return ShortArray(total) { i ->
            val x = mix[i] / peak
            // Soft clip
            val clipped = tanh(x * 0.85) * 0.9
            (clipped * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILMY — Bollywood/Tollywood disco groove  BPM=128, 4/4
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildFilmy(mix: DoubleArray, total: Int) {
        val bpm = 128.0
        val spb = spb(bpm)

        // Melody — 8-bar repeating hook  (E-minor pentatonic feel)
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

        // Bass line — pumping 8th-note pattern
        val bass = listOf(
            "E3" to 0.5,"E3" to 0.5, "G3" to 0.5,"G3" to 0.5,
            "A3" to 0.5,"A3" to 0.5, "G3" to 0.5,"B3" to 0.5
        )
        val bassBeats = bass.sumOf { it.second }

        // Chord stabs (octave+5th power chords)
        val chords = listOf(
            listOf("E4","B4") to 0.0,
            listOf("G4","D5") to 2.0,
            listOf("A4","E5") to 4.0,
            listOf("G4","D5") to 6.0
        )

        var pos = 0
        while (pos < total) {
            scheduleSeq(mix, pos, spb, melody, melodyBeats, 0.55, "saw", vibrato = true)
            scheduleSeq(mix, pos, spb, bass, bassBeats, 0.50, "saw")
            for ((noteList, beatOff) in chords) {
                val start = pos + (beatOff * spb).toInt()
                val dur = (0.22 * spb).toInt()
                for (n in noteList) note(mix, FREQ[n]!!,start,dur,0.25,"square")
            }
            drumLoop(mix, pos, spb, total,
                kickPat   = listOf(0.0,0.75,1.0,1.75, 2.0,2.75,3.0,3.5),
                snarePat  = listOf(1.0, 3.0),
                hihatPat  = (0 until 16).map { it * 0.5 },
                open      = listOf(1.0, 3.0, 3.5)
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FOLK — Telugu Janapada, fast pentatonic  BPM=138, 4/4
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildFolk(mix: DoubleArray, total: Int) {
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

        // Counter-melody (harmonised a 3rd below)
        val counter = listOf(
            "E5" to 0.50,"F5" to 0.50,"E5" to 0.50,"D5" to 0.50,
            "C5" to 0.50,"D5" to 0.50,"C5" to 0.50,"A4" to 0.50
        )
        val cBeats = counter.sumOf { it.second }

        var pos = 0
        while (pos < total) {
            scheduleSeq(mix, pos, spb, melody, mBeats, 0.55, "square", vibrato = false)
            scheduleSeq(mix, pos, spb, counter, cBeats, 0.28, "sine", vibrato = false)
            scheduleSeq(mix, pos, spb, bass, bBeats, 0.45, "saw")
            drumLoop(mix, pos, spb, total,
                kickPat  = listOf(0.0,0.5,2.0,2.5, 4.0,4.5,6.0,6.5),
                snarePat = listOf(1.0,3.0,5.0,7.0),
                hihatPat = (0 until 32).map { it * 0.25 },
                open     = emptyList()
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLASSICAL — Indian Shankarabharanam (major), 3-voice  BPM=92
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildClassical(mix: DoubleArray, total: Int) {
        val bpm = 92.0
        val spb = spb(bpm)

        // Upper voice
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

        // Middle voice (harmony thirds/sixths)
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

        // Bass (pizzicato feel — short notes)
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
            scheduleSeq(mix, pos, spb, upper,  uBeats, 0.52, "sine", vibrato = true)
            scheduleSeq(mix, pos, spb, middle, mBeats, 0.38, "sine", vibrato = true)
            scheduleSeq(mix, pos, spb, bass,   bBeats, 0.50, "saw")
            pos += (uBeats * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DEVOTIONAL — temple bells + tanpura drone  BPM=72
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildDevotional(mix: DoubleArray, total: Int) {
        val bpm = 72.0
        val spb = spb(bpm)

        // Bell melody — Sa Ga Ma Pa Ni Sa (Bilawal = major)
        val bells = listOf(
            "C5" to 2.0,"E5" to 1.0,"F5" to 1.0,
            "G5" to 2.0,"B5" to 1.0,"C6" to 1.0,
            "B5" to 1.0,"G5" to 1.0,"E5" to 1.0,"C5" to 1.0,
            "F5" to 1.5,"E5" to 0.5,"D5" to 1.0,
            "C5" to 4.0
        )
        val bBeats = bells.sumOf { it.second }

        // Second bell voice (lower, slower)
        val bells2 = listOf(
            "G4" to 2.0,"C5" to 2.0,
            "E5" to 2.0,"G5" to 2.0,
            "E5" to 2.0,"C5" to 2.0,
            "D5" to 2.0,"G4" to 2.0,
            "C5" to 4.0
        )
        val b2Beats = bells2.sumOf { it.second }

        // Tanpura drone — C + G constant with long sustain
        val drone = listOf("C3" to 2.0,"G3" to 2.0,"C4" to 2.0,"G4" to 2.0)
        val dBeats = drone.sumOf { it.second }

        // Tabla-like soft rhythm
        val tabla = listOf(0.0, 1.0, 1.5, 2.0, 3.0, 3.5)

        var pos = 0
        while (pos < total) {
            scheduleSeq(mix, pos, spb, bells,  bBeats,  0.60, "sine", vibrato = true,
                attackRatio=0.02, releaseRatio=0.50)
            scheduleSeq(mix, pos, spb, bells2, b2Beats, 0.35, "sine", vibrato = true,
                attackRatio=0.02, releaseRatio=0.50)
            scheduleSeq(mix, pos, spb, drone,  dBeats,  0.18, "sine")
            // Tabla taps
            for (t in tabla) {
                val start = pos + (t * spb).toInt()
                tablaHit(mix, start, total, 0.35)
            }
            pos += (bBeats * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INSTRUMENTAL — cinematic/fusion groove  BPM=108
    // ═══════════════════════════════════════════════════════════════════════════
    private fun buildInstrumental(mix: DoubleArray, total: Int) {
        val bpm = 108.0
        val spb = spb(bpm)

        // Lead melody (pentatonic A-minor)
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

        // Pad chords (Am - F - C - G in half-bar sweeps)
        val padChords = listOf(
            listOf("A4","C5","E5")  to 0.0,
            listOf("F4","A4","C5")  to 2.0,
            listOf("C4","E4","G4")  to 4.0,
            listOf("G4","B4","D5")  to 6.0
        )

        // Walking bass
        val bass = listOf(
            "A3" to 0.5,"C4" to 0.5,"E4" to 0.5,"G3" to 0.5,
            "F3" to 0.5,"A3" to 0.5,"C4" to 0.5,"E3" to 0.5,
            "C3" to 0.5,"E3" to 0.5,"G3" to 0.5,"B3" to 0.5,
            "G3" to 0.5,"B3" to 0.5,"D4" to 0.5,"F3" to 0.5
        )
        val bBeats = bass.sumOf { it.second }

        var pos = 0
        while (pos < total) {
            scheduleSeq(mix, pos, spb, lead, lBeats, 0.60, "saw", vibrato = true)
            scheduleSeq(mix, pos, spb, bass, bBeats, 0.50, "saw")
            for ((notes, beatOff) in padChords) {
                val start = pos + (beatOff * spb).toInt()
                val dur = (1.8 * spb).toInt()
                for (n in notes) note(mix, FREQ[n]!!, start, dur, 0.18, "sine")
            }
            drumLoop(mix, pos, spb, total,
                kickPat  = listOf(0.0, 2.0, 3.5),
                snarePat = listOf(1.0, 3.0),
                hihatPat = (0 until 16).map { it * 0.5 },
                open     = listOf(2.0, 6.0)
            )
            pos += (8 * spb).toInt()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Core synthesis helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /** Samples per beat at given BPM */
    private fun spb(bpm: Double) = (SAMPLE_RATE * 60.0 / bpm)

    /** Write one synthesized note into [mix] */
    private fun note(
        mix: DoubleArray, freq: Double, startSample: Int, durationSamples: Int,
        volume: Double, waveform: String,
        vibrato: Boolean = false,
        attackRatio: Double = 0.06, decayRatio: Double = 0.10,
        sustainLevel: Double = 0.72, releaseRatio: Double = 0.22
    ) {
        if (freq <= 0.0 || durationSamples <= 0) return
        val atk = (durationSamples * attackRatio).toInt().coerceAtLeast(1)
        val dec = (durationSamples * decayRatio).toInt().coerceAtLeast(1)
        val rel = (durationSamples * releaseRatio).toInt().coerceAtLeast(1)
        for (i in 0 until durationSamples) {
            val idx = startSample + i
            if (idx < 0 || idx >= mix.size) continue
            val t = i.toDouble() / SAMPLE_RATE
            val f = if (vibrato) freq * (1.0 + 0.0045 * sin(TWO_PI * 5.2 * t)) else freq
            val wave = when (waveform) {
                "saw"    -> sawWave(f, t)
                "square" -> squareWave(f, t)
                else     -> sin(TWO_PI * f * t)
            }
            val env = adsr(i, durationSamples, atk, dec, sustainLevel, rel)
            mix[idx] += wave * env * volume
        }
    }

    /** Schedule a looping sequence of (noteName, durationBeats) pairs */
    private fun scheduleSeq(
        mix: DoubleArray, startSample: Int, spb: Double,
        seq: List<Pair<String, Double>>, loopBeats: Double,
        volume: Double, waveform: String, vibrato: Boolean = false,
        attackRatio: Double = 0.06, releaseRatio: Double = 0.22
    ) {
        val loopSamples = (loopBeats * spb).toInt()
        if (loopSamples <= 0) return
        var loopStart = startSample
        while (loopStart < mix.size) {
            var beatPos = 0.0
            for ((noteName, dur) in seq) {
                val s = loopStart + (beatPos * spb).toInt()
                val d = (dur * spb).toInt()
                if (noteName != "R") {
                    note(mix, FREQ[noteName] ?: 0.0, s, d, volume, waveform, vibrato,
                        attackRatio = attackRatio, releaseRatio = releaseRatio)
                }
                beatPos += dur
            }
            loopStart += loopSamples
        }
    }

    /** Full drum loop (kick + snare + hi-hat) */
    private fun drumLoop(
        mix: DoubleArray, startSample: Int, spb: Double, total: Int,
        kickPat: List<Double>, snarePat: List<Double>,
        hihatPat: List<Double>, open: List<Double>
    ) {
        val loopBeats = 8.0
        val loopSamples = (loopBeats * spb).toInt()
        var loopStart = startSample
        while (loopStart < total) {
            for (b in kickPat)  kickHit(mix, loopStart + (b * spb).toInt(), total)
            for (b in snarePat) snareHit(mix, loopStart + (b * spb).toInt(), total)
            for (b in hihatPat) {
                val isOpen = open.any { abs(it - b) < 0.01 }
                hihatHit(mix, loopStart + (b * spb).toInt(), total, isOpen)
            }
            loopStart += loopSamples
        }
    }

    // ─── Percussion synthesis ─────────────────────────────────────────────────

    private fun kickHit(mix: DoubleArray, start: Int, total: Int) {
        val dur = (SAMPLE_RATE * 0.30).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx >= total) break
            val t = i.toDouble() / SAMPLE_RATE
            val f = 75.0 + 180.0 * exp(-35.0 * t)
            val env = exp(-7.0 * t)
            val click = if (i < 220) sin(TWO_PI * 3500.0 * t) * 0.3 * exp(-80.0 * t) else 0.0
            mix[idx] += (sin(TWO_PI * f * t) * 0.9 + click) * env * 0.80
        }
    }

    private fun snareHit(mix: DoubleArray, start: Int, total: Int) {
        val dur = (SAMPLE_RATE * 0.14).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx >= total) break
            val t = i.toDouble() / SAMPLE_RATE
            val noise = pseudoNoise(i, 0x5A5A)
            val tone = sin(TWO_PI * 220.0 * t) * exp(-30.0 * t)
            val env = exp(-22.0 * t)
            mix[idx] += (noise * 0.65 + tone * 0.35) * env * 0.55
        }
    }

    private fun hihatHit(mix: DoubleArray, start: Int, total: Int, open: Boolean) {
        val maxT = if (open) 0.18 else 0.045
        val decay = if (open) 18.0 else 90.0
        val dur = (SAMPLE_RATE * maxT).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx >= total) break
            val t = i.toDouble() / SAMPLE_RATE
            val n = pseudoNoise(i, 0xBEEF)
            // Band-pass: mix high-freq noise with a metallic tone
            val metal = sin(TWO_PI * 8000.0 * t) * 0.25
            mix[idx] += (n * 0.75 + metal) * exp(-decay * t) * 0.35
        }
    }

    private fun tablaHit(mix: DoubleArray, start: Int, total: Int, vol: Double) {
        val dur = (SAMPLE_RATE * 0.12).toInt()
        for (i in 0 until dur) {
            val idx = start + i; if (idx >= total) break
            val t = i.toDouble() / SAMPLE_RATE
            val f = 280.0 + 120.0 * exp(-40.0 * t)
            mix[idx] += sin(TWO_PI * f * t) * exp(-25.0 * t) * vol
        }
    }

    // ─── Waveform generators ──────────────────────────────────────────────────

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

    // ─── Deterministic pseudo-noise (no Random — reproducible) ───────────────

    private fun pseudoNoise(i: Int, seed: Int): Double {
        var x = i xor seed
        x = x xor (x shl 13)
        x = x xor (x ushr 7)
        x = x xor (x shl 17)
        return (x and 0x7FFFFFFF).toDouble() / 0x7FFFFFFF * 2.0 - 1.0
    }
}
