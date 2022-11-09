package com.hiennv.flutter_callkit_incoming

object Cache {
    var latestEvent: Map<String, Any>? = null
        private set

    fun updateLatestEvent(action: String, data: Map<String, Any>) {
        latestEvent = mapOf(
            "event" to action,
            "body" to data,
        )
    }

    fun clearLatestEvent() {
        latestEvent = null
    }
}
