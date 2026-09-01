package com.zichun2000.dsplab.lab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ExperimentRecord(
    val labId: String,
    val parameters: Map<String, String>,
    val observation: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ExperimentRecordStore(context: Context) {
    private val prefs = context.getSharedPreferences("dsp_lab_records", Context.MODE_PRIVATE)

    fun save(record: ExperimentRecord) {
        val current = JSONArray(prefs.getString(KEY, "[]"))
        current.put(JSONObject().apply {
            put("labId", record.labId)
            put("observation", record.observation)
            put("timestamp", record.timestamp)
            put("parameters", JSONObject(record.parameters))
        })
        prefs.edit().putString(KEY, current.toString()).commit()
    }

    // Assessment results also get a dedicated preference entry. This makes the
    // research dashboard independent of JSON-array parsing/order and guarantees
    // that the latest Post-test survives navigation and recomposition.
    fun saveAssessment(type: String, score: Int, maxScore: Int) {
        prefs.edit()
            .putInt("assessment_${type}_score", score)
            .putInt("assessment_${type}_max", maxScore)
            .putLong("assessment_${type}_timestamp", System.currentTimeMillis())
            .commit()
        save(ExperimentRecord(type, emptyMap(), "score=$score/$maxScore"))
    }

    fun getAssessmentScore(type: String): Int? {
        val key = "assessment_${type}_score"
        return if (prefs.contains(key)) prefs.getInt(key, -1).takeIf { it >= 0 } else null
    }

    fun getAssessmentMax(type: String): Int? {
        val key = "assessment_${type}_max"
        return if (prefs.contains(key)) prefs.getInt(key, 0).takeIf { it > 0 } else null
    }

    fun loadAll(): List<ExperimentRecord> {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val p = item.getJSONObject("parameters")
            val parameters = p.keys().asSequence().associateWith { key -> p.getString(key) }
            ExperimentRecord(item.getString("labId"), parameters, item.getString("observation"), item.getLong("timestamp"))
        }
    }

    fun clear() = prefs.edit().remove(KEY).apply()

    companion object { private const val KEY = "records" }
}
