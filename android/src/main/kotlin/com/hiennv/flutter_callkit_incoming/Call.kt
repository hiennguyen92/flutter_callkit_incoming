package com.hiennv.flutter_callkit_incoming

import android.os.Bundle

class Call {
}

@Suppress("UNCHECKED_CAST")
data class Data(val args: Map<String, Any?>) {

    var id: String = (args["id"] as? String) ?: ""
    var uuid: String = (args["id"] as? String) ?: ""
    var nameCaller: String = (args["nameCaller"] as? String) ?: ""
    var appName: String = (args["appName"] as? String) ?: ""
    var handle: String = (args["handle"] as? String) ?: ""
    var avatar: String = (args["avatar"] as? String) ?: ""
    var type: Int = (args["type"] as? Int) ?: 0
    var duration: Long = (args["duration"] as? Long) ?: ((args["duration"] as? Int)?.toLong() ?: 30000L)
    var textAccept: String = (args["textAccept"] as? String) ?: ""
    var textDecline: String = (args["textDecline"] as? String) ?: ""
    var textMissedCall: String = (args["textMissedCall"] as? String) ?: ""
    var textCallback: String = (args["textCallback"] as? String) ?: ""
    var extra: HashMap<String, Any?> =
            (args["extra"] ?: HashMap<String, Any?>()) as HashMap<String, Any?>
    var headers: HashMap<String, Any?> =
            (args["headers"] ?: HashMap<String, Any?>()) as HashMap<String, Any?>
    var from: String = ""

    var isCustomNotification: Boolean = false
    var isCustomSmallExNotification: Boolean = false
    var isShowLogo: Boolean = false
    var isShowCallback: Boolean = true
    var ringtonePath: String
    var backgroundColor: String
    var backgroundUrl: String
    var actionColor: String
    var isShowMissedCallNotification: Boolean = true
    var incomingCallNotificationChannelName: String? = null
    var missedCallNotificationChannelName: String? = null

    var isAccepted: Boolean = false

    init {
        val android: HashMap<String, Any?>? = args["android"] as? HashMap<String, Any?>?
        if (android != null) {
            isCustomNotification = (android["isCustomNotification"] as? Boolean) ?: false
            isCustomSmallExNotification =
                (android["isCustomSmallExNotification"] as? Boolean) ?: false
            isShowLogo = (android["isShowLogo"] as? Boolean) ?: false
            isShowCallback = (android["isShowCallback"] as? Boolean) ?: true
            ringtonePath = (android["ringtonePath"] as? String) ?: ""
            backgroundColor = (android["backgroundColor"] as? String) ?: "#0955fa"
            backgroundUrl = (android["backgroundUrl"] as? String) ?: ""
            actionColor = (android["actionColor"] as? String) ?: "#4CAF50"
            isShowMissedCallNotification = (android["isShowMissedCallNotification"] as? Boolean) ?: true
            incomingCallNotificationChannelName = android["incomingCallNotificationChannelName"] as? String
            missedCallNotificationChannelName = android["missedCallNotificationChannelName"] as? String
        } else {
            isCustomNotification = (args["isCustomNotification"] as? Boolean) ?: false
            isCustomSmallExNotification = (args["isCustomSmallExNotification"] as? Boolean) ?: false
            isShowLogo = (args["isShowLogo"] as? Boolean) ?: false
            isShowCallback = (args["isShowCallback"] as? Boolean) ?: true
            ringtonePath = (args["ringtonePath"] as? String) ?: ""
            backgroundColor = (args["backgroundColor"] as? String) ?: "#0955fa"
            backgroundUrl = (args["backgroundUrl"] as? String) ?: ""
            actionColor = (args["actionColor"] as? String) ?: "#4CAF50"
            isShowMissedCallNotification = (args["isShowMissedCallNotification"] as? Boolean) ?: true
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val e: Data = other as Data
        return this.id == e.id
    }


    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ID, id)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, nameCaller)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HANDLE, handle)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_AVATAR, avatar)
        bundle.putInt(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TYPE, type)
        bundle.putLong(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_DURATION, duration)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_ACCEPT, textAccept)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_DECLINE, textDecline)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_MISSED_CALL, textMissedCall)
        bundle.putString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_CALLBACK, textCallback)
        bundle.putSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_EXTRA, extra)
        bundle.putSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HEADERS, headers)
        bundle.putBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
                isCustomNotification
        )
        bundle.putBoolean(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION,
            isCustomSmallExNotification
        )
        bundle.putBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_LOGO,
                isShowLogo
        )
        bundle.putBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_CALLBACK,
                isShowCallback
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
        bundle.putBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION,
                isShowMissedCallNotification
        )
        bundle.putString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
            incomingCallNotificationChannelName
        )
        bundle.putString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
            missedCallNotificationChannelName
        )
        return bundle
    }

    companion object {

        fun fromBundle(bundle: Bundle): Data {
            val data = Data(emptyMap())
            data.id = bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ID, "")
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
            data.textAccept =
                    bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_ACCEPT, "")
            data.textDecline =
                    bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_DECLINE, "")
            data.textMissedCall =
                    bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_MISSED_CALL, "")
            data.textCallback =
                    bundle.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_CALLBACK, "")
            data.extra =
                    bundle.getSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>
            data.headers =
                    bundle.getSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>

            data.isCustomNotification = bundle.getBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
                false
            )
            data.isCustomSmallExNotification = bundle.getBoolean(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION,
                false
            )
            data.isShowLogo = bundle.getBoolean(
                    CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_LOGO,
                    false
            )
            data.isShowCallback = bundle.getBoolean(
                    CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_CALLBACK,
                    true
            )
            data.ringtonePath = bundle.getString(
                    CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_RINGTONE_PATH,
                    ""
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
            data.isShowMissedCallNotification = bundle.getBoolean(
                    CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION,
                    true
            )
            data.incomingCallNotificationChannelName = bundle.getString(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME
            )
            data.missedCallNotificationChannelName = bundle.getString(
                CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME
            )
            return data
        }
    }

}