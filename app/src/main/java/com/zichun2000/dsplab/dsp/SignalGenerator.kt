package com.zichun2000.dsplab.dsp

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Basic discrete-time signals used by Lab 01. */
enum class SignalType(val displayName: String) {
    SINE("Sine"),
    IMPULSE("Unit Impulse"),
    STEP("Unit Step"),
    EXPONENTIAL("Exponential")
}

data class SignalParameters(
    val amplitude: Double = 1.0,
    val frequencyCycles: Double = 2.0,
    val phaseRadians: Double = 0.0,
    val decay: Double = 0.06
)

fun generateDiscreteSignal(
    type: SignalType,
    sampleCount: Int,
    parameters: SignalParameters
): List<Double> {
    return (0 until sampleCount).map { n ->
        when (type) {
            SignalType.SINE -> parameters.amplitude * sin(
                2.0 * PI * parameters.frequencyCycles * n / sampleCount + parameters.phaseRadians
            )
            SignalType.IMPULSE -> if (n == 0) parameters.amplitude else 0.0
            SignalType.STEP -> parameters.amplitude
            SignalType.EXPONENTIAL -> parameters.amplitude * exp(-parameters.decay * n)
        }
    }
}
