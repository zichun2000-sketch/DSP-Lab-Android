package com.zichun2000.dsplab.lab

data class AssessmentItem(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

object LearningAssessment {
    val prePostItems = listOf(
        AssessmentItem("sampling", "What condition prevents aliasing for a band-limited signal?", listOf("Fs > 2Fmax", "Fs < Fmax", "Fs = Fmax / 2", "Any sampling rate"), 0),
        AssessmentItem("convolution", "For finite sequences of lengths L and M, what is the linear convolution length?", listOf("L + M", "L + M - 1", "L - M", "LM"), 1),
        AssessmentItem("dft", "For N-point DFT sampled at Fs, what is the frequency-bin spacing?", listOf("Fs", "N/Fs", "Fs/N", "2Fs"), 2),
        AssessmentItem("filter", "A low-pass FIR filter primarily preserves which frequency region?", listOf("Low frequencies", "Only DC", "High frequencies", "All frequencies equally"), 0)
    )

    fun score(answers: Map<String, Int>): Int =
        prePostItems.count { answers[it.id] == it.correctIndex }
}
