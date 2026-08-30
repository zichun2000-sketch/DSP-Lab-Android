package com.zichun2000.dsplab.dsp

/** Linear convolution for short teaching sequences. */
fun convolve(x: List<Double>, h: List<Double>): List<Double> {
    if (x.isEmpty() || h.isEmpty()) return emptyList()
    val y = MutableList(x.size + h.size - 1) { 0.0 }
    for (n in y.indices) {
        var sum = 0.0
        for (k in x.indices) {
            val j = n - k
            if (j in h.indices) sum += x[k] * h[j]
        }
        y[n] = sum
    }
    return y
}

data class ConvolutionStep(
    val outputIndex: Int,
    val products: List<Double>,
    val sum: Double
)

fun convolutionStep(x: List<Double>, h: List<Double>, outputIndex: Int): ConvolutionStep {
    val products = x.indices.map { k ->
        val j = outputIndex - k
        if (j in h.indices) x[k] * h[j] else 0.0
    }
    return ConvolutionStep(outputIndex, products, products.sum())
}
