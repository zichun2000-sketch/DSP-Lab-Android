package com.zichun2000.dsplab.dsp

import kotlin.math.abs
import kotlin.math.min

/** Sampling utilities for Lab 02. */
data class SampledSignal(
    val time: List<Double>,
    val values: List<Double>,
    val aliasedFrequencyHz: Double
)

fun sampleSineSignal(
    frequencyHz: Double,
    samplingFrequencyHz: Double,
    sampleCount: Int,
    amplitude: Double = 1.0,
    phaseRadians: Double = 0.0
): SampledSignal {
    require(frequencyHz >= 0.0)
    require(samplingFrequencyHz > 0.0)
    require(sampleCount > 0)

    val values = (0 until sampleCount).map { n ->
        amplitude * kotlin.math.sin(
            2.0 * Math.PI * frequencyHz * n / samplingFrequencyHz + phaseRadians
        )
    }

    // Fold the original tone into the first Nyquist zone to expose the frequency
    // that would be observed after ideal sampling.
    val folded = frequencyHz % samplingFrequencyHz
    val alias = min(folded, abs(samplingFrequencyHz - folded))
    return SampledSignal(
        time = (0 until sampleCount).map { it / samplingFrequencyHz },
        values = values,
        aliasedFrequencyHz = alias
    )
}

fun satisfiesNyquist(signalFrequencyHz: Double, samplingFrequencyHz: Double): Boolean =
    samplingFrequencyHz >= 2.0 * signalFrequencyHz
