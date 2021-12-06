package com.hiennv.flutter_callkit_incoming

import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith { it ->
    when (val value = this[it]) {
        is JSONArray -> {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }
        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else -> value
    }
}

fun mapToJsonString(map: Map<String, *>): String? {
    return try {
        JSONObject(map).toString()
    } catch (e: Exception) {
        null
    }
}

fun getMapFromJsonString(json: String): Map<String, String>? {
    if (TextUtils.isEmpty(json)) return null

    val result: Map<String, String>?

    try {
        val jsonObj = JSONObject(json)
        result = jsonObj.toMap().mapValues {
            it.value.toString()
        }
    } catch (e: Exception) {
        return null
    }

    return result
}
