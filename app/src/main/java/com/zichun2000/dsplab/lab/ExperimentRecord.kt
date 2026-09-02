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
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(record: ExperimentRecord): Boolean {
        return try {
            val current = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
            current.put(JSONObject().apply {
                put("labId", record.labId)
                put("observation", record.observation)
                put("timestamp", record.timestamp)
                put("parameters", JSONObject(record.parameters))
            })
            prefs.edit().putString(KEY, current.toString()).commit()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Save an assessment independently from the activity-record JSON.
     * The assessment result is committed first and verified immediately.
     * The event log is best-effort and can never invalidate the assessment result.
     */
    fun saveAssessment(type: String, score: Int, maxScore: Int): Boolean {
        val safeType = type.trim().uppercase()
        val scoreKey = "assessment_${safeType}_score"
        val maxKey = "assessment_${safeType}_max"
        val timeKey = "assessment_${safeType}_timestamp"
        val now = System.currentTimeMillis()

        val committed = prefs.edit()
            .putInt(scoreKey, score)
            .putInt(maxKey, maxScore)
            .putLong(timeKey, now)
            .commit()

        if (!committed) return false

        // Verify the actual persisted values before reporting success.
        val verified = prefs.contains(scoreKey) &&
            prefs.getInt(scoreKey, Int.MIN_VALUE) == score &&
            prefs.getInt(maxKey, Int.MIN_VALUE) == maxScore

        if (verified) {
            // Do not let an unrelated/corrupt event log prevent assessment storage.
            save(ExperimentRecord(safeType, emptyMap(), "score=$score/$maxScore", now))
        }
        return verified
    }

    fun getAssessmentScore(type: String): Int? {
        val key = "assessment_${type.trim().uppercase()}_score"
        return if (prefs.contains(key)) prefs.getInt(key, -1).takeIf { it >= 0 } else null
    }

    fun getAssessmentMax(type: String): Int? {
        val key = "assessment_${type.trim().uppercase()}_max"
        return if (prefs.contains(key)) prefs.getInt(key, 0).takeIf { it > 0 } else null
    }

    fun loadAll(): List<ExperimentRecord> {
        return try {
            val array = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val item = array.getJSONObject(i)
                    val p = item.optJSONObject("parameters") ?: JSONObject()
                    val parameters = p.keys().asSequence().associateWith { key -> p.optString(key) }
                    ExperimentRecord(
                        item.getString("labId"),
                        parameters,
                        item.optString("observation"),
                        item.optLong("timestamp", System.currentTimeMillis())
                    )
                }.getOrNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear() = prefs.edit().remove(KEY).apply()

    companion object {
        private const val PREFS_NAME = "dsp_lab_records"
        private const val KEY = "records"
    }
}
