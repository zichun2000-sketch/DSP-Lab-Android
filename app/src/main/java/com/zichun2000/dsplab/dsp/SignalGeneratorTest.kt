package com.zichun2000.dsplab.dsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalGeneratorTest {
    @Test
    fun impulseHasUnitValueAtZero() {
        val x = generateDiscreteSignal(SignalType.IMPULSE, 8, SignalParameters())
        assertEquals(1.0, x[0], 1e-9)
        assertTrue(x.drop(1).all { it == 0.0 })
    }

    @Test
    fun stepIsConstant() {
        val x = generateDiscreteSignal(SignalType.STEP, 8, SignalParameters(amplitude = 2.0))
        assertTrue(x.all { it == 2.0 })
    }

    @Test
    fun outputHasRequestedSampleCount() {
        val x = generateDiscreteSignal(SignalType.SINE, 24, SignalParameters())
        assertEquals(24, x.size)
    }
}
