package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import org.json.JSONArray


private const val CALLKIT_PREFERENCES_FILE_NAME = "flutter_callkit_incoming"
private var prefs: SharedPreferences? = null
private var editor: SharedPreferences.Editor? = null

private fun initInstance(context: Context) {
    prefs = context.getSharedPreferences(CALLKIT_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    editor = prefs?.edit()
}

fun addCall(context: Context?, value: Map<String, *>) {
    var mapData = getMap(context, "DATA_CALLS")?.toMutableMap()
    if (mapData != null) {
        var activeCalls = mapData["ACTIVE_CALLS"] as JSONArray?
        if(activeCalls != null) {
            activeCalls.put(value)
        }else {
            activeCalls = JSONArray()
            activeCalls.put(value)
            mapData["ACTIVE_CALLS"] = activeCalls
        }
        putMap(context, "DATA_CALLS", mapData)
    }else {
        val activeCalls = JSONArray()
        activeCalls.put(value)
        mapData = mutableMapOf()
        mapData["ACTIVE_CALLS"] = activeCalls
        putMap(context, "DATA_CALLS", mapData)
    }
}

fun removeCall(context: Context?, value: Map<String, *>) {
    var mapData = getMap(context, "DATA_CALLS")?.toMutableMap()
    if (mapData != null) {
        var activeCalls = mapData["ACTIVE_CALLS"] as JSONArray?
        if(activeCalls != null) {

            for (i in 0 until activeCalls.length()) {
                val item = activeCalls.getJSONObject(i)
                if(item["id"] == value["id"]){

                }
            }


            activeCalls.put(value)
        }else {
            activeCalls = JSONArray()
            activeCalls.put(value)
            mapData["ACTIVE_CALLS"] = activeCalls
        }
        putMap(context, "DATA_CALLS", mapData)
    }else {
        val activeCalls = JSONArray()
        activeCalls.put(value)
        mapData = mutableMapOf()
        mapData["ACTIVE_CALLS"] = activeCalls
        putMap(context, "DATA_CALLS", mapData)
    }
}


fun putMap(context: Context?, key: String, value: Map<String, *>) {
    if (context == null) return
    try {
        putString(context, key, mapToJsonString(value))
    } catch (e: Exception) {
        // ignore
    }
}

fun getMap(context: Context?, key: String): Map<String, *>? {
    val map: String? = getString(context, key)

    if (TextUtils.isEmpty(map)) return null

    return getMapFromJsonString(map!!)
}

fun putString(context: Context?, key: String, value: String?) {
    if (context == null) return
    initInstance(context)
    editor?.putString(key, value)
    editor?.commit()
}

fun getString(context: Context?, key: String): String? {
    if (context == null) return null
    initInstance(context)
    return prefs?.getString(key, "")
}

fun remove(context: Context?, key: String) {
    if (context == null) return
    initInstance(context)
    editor?.remove(key)
    editor?.commit()
}
