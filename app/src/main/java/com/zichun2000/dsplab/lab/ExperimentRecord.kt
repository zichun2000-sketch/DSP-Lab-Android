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
        prefs.edit().putString(KEY, current.toString()).apply()
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
