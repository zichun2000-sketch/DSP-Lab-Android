package com.zichun2000.dsplab.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Simple O(N^2) DFT implementation intended for teaching and small N. */
data class DftBin(val frequency: Double, val magnitude: Double, val real: Double, val imag: Double)

fun dft(samples: List<Double>, sampleRate: Double): List<DftBin> {
    if (samples.isEmpty() || sampleRate <= 0.0) return emptyList()
    val n = samples.size
    val half = n / 2
    return (0..half).map { k ->
        var real = 0.0
        var imag = 0.0
        for (index in samples.indices) {
            val angle = 2.0 * PI * k * index / n
            real += samples[index] * cos(angle)
            imag -= samples[index] * sin(angle)
        }
        val scale = if (k == 0 || (n % 2 == 0 && k == half)) 1.0 / n else 2.0 / n
        DftBin(k * sampleRate / n, sqrt(real * real + imag * imag) * scale, real, imag)
    }
}

fun sineSamples(frequency: Double, sampleRate: Double, count: Int): List<Double> =
    (0 until count).map { n -> sin(2.0 * PI * frequency * n / sampleRate) }
