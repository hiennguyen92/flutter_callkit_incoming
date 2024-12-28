package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

private const val CALLKIT_PREFERENCES_FILE_NAME = "flutter_callkit_incoming"
private var prefs: SharedPreferences? = null
private var editor: SharedPreferences.Editor? = null

private fun initInstance(context: Context) {
    prefs = context.getSharedPreferences(CALLKIT_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    editor = prefs?.edit()
}

fun addCall(context: Context?, data: Data, isAccepted: Boolean = false) {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData = JSONArray(json)
    var found = false

    for (i in 0 until arrayData.length()) {
        val item = arrayData.getJSONObject(i)
        if (item.getString("id") == data.id) {
            item.put("isAccepted", isAccepted)
            found = true
            break
        }
    }

    if (!found) {
        val newData = JSONObject()
        newData.put("id", data.id)
        newData.put("isAccepted", isAccepted)
        arrayData.put(newData)
    }

    putString(context, "ACTIVE_CALLS", arrayData.toString())
}

fun removeCall(context: Context?, data: Data) {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    Log.d("JSON", json!!)
    val arrayData = JSONArray(json)
    val filteredArray = JSONArray()

    for (i in 0 until arrayData.length()) {
        val item = arrayData.getJSONObject(i)
        if (item.getString("id") != data.id) {
            filteredArray.put(item)
        }
    }

    putString(context, "ACTIVE_CALLS", filteredArray.toString())
}

fun removeAllCalls(context: Context?) {
    putString(context, "ACTIVE_CALLS", "[]")
    remove(context, "ACTIVE_CALLS")
}

fun getDataActiveCalls(context: Context?): ArrayList<Data> {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData = JSONArray(json)
    val result = ArrayList<Data>()

    for (i in 0 until arrayData.length()) {
        val item = arrayData.getJSONObject(i)
        val data = Data(
            mapOf(
                "id" to item.getString("id"),
                "isAccepted" to item.optBoolean("isAccepted", false)
            )
        )
        result.add(data)
    }

    return result
}

fun getDataActiveCallsForFlutter(context: Context?): ArrayList<Map<String, Any?>> {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData = JSONArray(json)
    val result = ArrayList<Map<String, Any?>>()

    for (i in 0 until arrayData.length()) {
        val item = arrayData.getJSONObject(i)
        val map = mapOf(
            "id" to item.getString("id"),
            "isAccepted" to item.optBoolean("isAccepted", false)
        )
        result.add(map)
    }

    return result
}

fun putString(context: Context?, key: String, value: String?) {
    if (context == null) return
    initInstance(context)
    editor?.putString(key, value)
    editor?.commit()
}

fun getString(context: Context?, key: String, defaultValue: String = ""): String? {
    if (context == null) return null
    initInstance(context)
    return prefs?.getString(key, defaultValue)
}

fun remove(context: Context?, key: String) {
    if (context == null) return
    initInstance(context)
    editor?.remove(key)
    editor?.commit()
}
