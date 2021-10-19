package com.hiennv.flutter_callkit_incoming

import android.os.Bundle

class Call {
}

@Suppress("UNCHECKED_CAST")
data class Data(val args: Map<String, Any?>) {

    var uuid: String = (args["id"] as? String) ?: ""
    var nameCaller: String = (args["nameCaller"] as? String) ?: ""
    var appName: String = (args["appName"] as? String) ?: ""
    var handle: String = (args["handle"] as? String) ?: ""
    var avatar: String = (args["avatar"] as? String) ?: ""
    var type: Int = (args["avatar"] as? Int) ?: 0
    var duration: Long = (args["duration"] as? Long) ?: 30000L
    var extra: HashMap<String, Any?> =
        (args["extra"] ?: HashMap<String, Any?>()) as HashMap<String, Any?>
    var from: String = ""

    var isCustomNotification: Boolean = false
    var ringtonePath: String
    var backgroundColor: String
    var backgroundUrl: String
    var actionColor: String

    init {
        val android: HashMap<String, Any?>? = args["android"] as? HashMap<String, Any?>?
        if (android != null) {
            isCustomNotification = (android["isCustomNotification"] as? Boolean) ?: false
            ringtonePath = (android["ringtonePath"] as? String) ?: "ringtone_default"
            backgroundColor = (android["backgroundColor"] as? String) ?: "#0955fa"
            backgroundUrl = (android["backgroundUrl"] as? String) ?: ""
            actionColor = (android["actionColor"] as? String) ?: "#4CAF50"
        } else {
            isCustomNotification = false
            ringtonePath = "ringtone_default"
            backgroundColor = "#0955fa"
            backgroundUrl = ""
            actionColor = "#4CAF50"
        }
    }


    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ID, uuid)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, nameCaller)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HANDLE, handle)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_AVATAR, avatar)
        bundle.putInt(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TYPE, type)
        bundle.putLong(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_DURATION, duration)
        bundle.putSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_EXTRA, extra)
        bundle.putBoolean(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
            isCustomNotification
        )
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_RINGTONE_PATH, ringtonePath)
        bundle.putString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_BACKGROUND_COLOR,
            backgroundColor
        )
        bundle.putString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_BACKGROUND_URL,
            backgroundUrl
        )
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ACTION_COLOR, actionColor)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ACTION_FROM, from)
        return bundle
    }

    companion object {

        fun fromBundle(bundle: Bundle): Data {
            val data = Data(emptyMap())
            data.uuid = bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ID, "")
            data.nameCaller =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, "")
            data.appName =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_APP_NAME, "")
            data.handle =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HANDLE, "")
            data.avatar =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_AVATAR, "")
            data.type = bundle.getInt(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TYPE, 0)
            data.duration =
                bundle.getLong(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_DURATION, 30000L)
            data.extra =
                bundle.getSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>

            data.isCustomNotification = bundle.getBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
                false
            )
            data.ringtonePath = bundle.getString(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_RINGTONE_PATH,
                "ringtone_default"
            )
            data.backgroundColor = bundle.getString(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_BACKGROUND_COLOR,
                "#0955fa"
            )
            data.backgroundUrl =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_BACKGROUND_URL, "")
            data.actionColor = bundle.getString(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ACTION_COLOR,
                "#4CAF50"
            )
            data.from =
                bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ACTION_FROM, "")
            return data
        }
    }

}