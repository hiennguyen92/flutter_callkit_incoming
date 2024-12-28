package com.hiennv.flutter_callkit_incoming

import android.os.Bundle

@Suppress("UNCHECKED_CAST")
data class Data(val args: Map<String, Any?>) {

    constructor() : this(emptyMap())

    var id: String = (args["id"] as? String) ?: ""
    var uuid: String = (args["id"] as? String) ?: ""
    var nameCaller: String = (args["nameCaller"] as? String) ?: ""
    var appName: String = (args["appName"] as? String) ?: ""
    var handle: String = (args["handle"] as? String) ?: ""
    var avatar: String = (args["avatar"] as? String) ?: ""
    var type: Int = (args["type"] as? Int) ?: 0
    var duration: Long = (args["duration"] as? Long) ?: ((args["duration"] as? Int)?.toLong() ?: 30000L)
    var textAccept: String = (args["textAccept"] as? String) ?: ""
    var extra: HashMap<String, Any?> =
        (args["extra"] ?: HashMap<String, Any?>()) as HashMap<String, Any?>
    var headers: HashMap<String, Any?> =
        (args["headers"] ?: HashMap<String, Any?>()) as HashMap<String, Any?>
    var from: String = ""

    var isCustomNotification: Boolean = false
    var isCustomSmallExNotification: Boolean = false
    var isShowLogo: Boolean = false
    var isShowCallID: Boolean = false
    var ringtonePath: String
    var backgroundColor: String
    var backgroundUrl: String
    var textColor: String
    var actionColor: String
    var incomingCallNotificationChannelName: String? = null
    var missedCallNotificationChannelName: String? = null
    var missedNotificationId: Int? = null
    var isShowMissedCallNotification: Boolean = true
    var missedNotificationCount: Int = 1
    var missedNotificationSubtitle: String? = null
    var missedNotificationCallbackText: String? = null
    var isShowCallback: Boolean = true
    var isAccepted: Boolean = false

    var isOnHold: Boolean = (args["isOnHold"] as? Boolean) ?: false
    var audioRoute: Int = (args["audioRoute"] as? Int) ?: 1
    var isMuted: Boolean = (args["isMuted"] as? Boolean) ?: false

    var isShowFullLockedScreen: Boolean = true

    var isImportant: Boolean = false
    var isBot: Boolean = false

    init {
        var android: Map<String, Any?>? = args["android"] as? HashMap<String, Any?>?
        android = android ?: args
        isCustomNotification = android["isCustomNotification"] as? Boolean ?: false
        isCustomSmallExNotification = android["isCustomSmallExNotification"] as? Boolean ?: false
        isShowLogo = android["isShowLogo"] as? Boolean ?: false
        isShowCallID = android["isShowCallID"] as? Boolean ?: false
        ringtonePath = android["ringtonePath"] as? String ?: ""
        backgroundColor = android["backgroundColor"] as? String ?: "#0955fa"
        backgroundUrl = android["backgroundUrl"] as? String ?: ""
        actionColor = android["actionColor"] as? String ?: "#4CAF50"
        textColor = android["textColor"] as? String ?: "#ffffff"
        incomingCallNotificationChannelName =
            android["incomingCallNotificationChannelName"] as? String
        missedCallNotificationChannelName = android["missedCallNotificationChannelName"] as? String
        isShowFullLockedScreen = android["isShowFullLockedScreen"] as? Boolean ?: true
        isImportant = android["isImportant"] as? Boolean ?: false
        isBot = android["isBot"] as? Boolean ?: false

        val missedNotification: Map<String, Any?>? =
            args["missedCallNotification"] as? Map<String, Any?>?

        if (missedNotification != null) {
            missedNotificationId = missedNotification["id"] as? Int?
            missedNotificationSubtitle = missedNotification["subtitle"] as? String?
            missedNotificationCount = missedNotification["count"] as? Int? ?: 1
            missedNotificationCallbackText = missedNotification["callbackText"] as? String?
            isShowCallback = missedNotification["isShowCallback"] as? Boolean ?: true
            isShowMissedCallNotification =
                missedNotification["showNotification"] as? Boolean ?: true
        } else {
            missedNotificationSubtitle = args["textMissedCall"] as? String ?: ""
            missedNotificationCallbackText = args["textCallback"] as? String ?: ""
            isShowCallback = android["isShowCallback"] as? Boolean ?: true
            isShowMissedCallNotification =
                android["isShowMissedCallNotification"] as? Boolean ?: true
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
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_ID, id)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, nameCaller)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_HANDLE, handle)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_AVATAR, avatar)
        bundle.putInt(CallkitConstants.EXTRA_CALLKIT_TYPE, type)
        bundle.putLong(CallkitConstants.EXTRA_CALLKIT_DURATION, duration)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_TEXT_ACCEPT, textAccept)

        missedNotificationId?.let {
            bundle.putInt(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID,
                it
            )
        }
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SHOW,
            isShowMissedCallNotification
        )
        bundle.putInt(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_COUNT,
            missedNotificationCount
        )
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SUBTITLE,
            missedNotificationSubtitle
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_SHOW,
            isShowCallback
        )
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_TEXT,
            missedNotificationCallbackText
        )

        bundle.putSerializable(CallkitConstants.EXTRA_CALLKIT_EXTRA, extra)
        bundle.putSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS, headers)

        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
            isCustomNotification
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION,
            isCustomSmallExNotification
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_SHOW_LOGO,
            isShowLogo
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID,
            isShowCallID
        )
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_RINGTONE_PATH, ringtonePath)
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_BACKGROUND_COLOR,
            backgroundColor
        )
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_BACKGROUND_URL,
            backgroundUrl
        )
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_TEXT_COLOR, textColor)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, actionColor)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_ACTION_FROM, from)
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
            incomingCallNotificationChannelName
        )
        bundle.putString(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
            missedCallNotificationChannelName
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_SHOW_FULL_LOCKED_SCREEN,
            isShowFullLockedScreen
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_IMPORTANT,
            isImportant,
        )
        bundle.putBoolean(
            CallkitConstants.EXTRA_CALLKIT_IS_BOT,
            isBot,
        )
        return bundle
    }

    companion object {

        fun fromBundle(bundle: Bundle): Data {
            val data = Data(emptyMap())
            data.id = bundle.getString(CallkitConstants.EXTRA_CALLKIT_ID, "")
            data.nameCaller =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, "")
            data.appName =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_APP_NAME, "")
            data.handle =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_HANDLE, "")
            data.avatar =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, "")
            data.type = bundle.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, 0)
            data.duration =
                bundle.getLong(CallkitConstants.EXTRA_CALLKIT_DURATION, 30000L)
            data.textAccept =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_ACCEPT, "")
            data.isImportant =
                bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_IMPORTANT, false)
            data.isBot =
                bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_BOT, false)

            data.missedNotificationId =
                bundle.getInt(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID)
            data.isShowMissedCallNotification =
                bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SHOW, true)
            data.missedNotificationCount =
                bundle.getInt(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_COUNT, 1)
            data.missedNotificationSubtitle =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SUBTITLE, "")
            data.isShowCallback =
                bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_SHOW, false)
            data.missedNotificationCallbackText =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_TEXT, "")

            data.extra =
                bundle.getSerializable(CallkitConstants.EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>
            data.headers =
                bundle.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>

            data.isCustomNotification = bundle.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
                false
            )
            data.isCustomSmallExNotification = bundle.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION,
                false
            )
            data.isShowLogo = bundle.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_IS_SHOW_LOGO,
                false
            )
            data.isShowCallID = bundle.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID,
                false
            )
            data.ringtonePath = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_RINGTONE_PATH,
                ""
            )
            data.backgroundColor = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_BACKGROUND_COLOR,
                "#0955fa"
            )
            data.backgroundUrl =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_BACKGROUND_URL, "")
            data.actionColor = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR,
                "#4CAF50"
            )
            data.textColor = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_TEXT_COLOR,
                "#FFFFFF"
            )
            data.from =
                bundle.getString(CallkitConstants.EXTRA_CALLKIT_ACTION_FROM, "")

            data.incomingCallNotificationChannelName = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME
            )
            data.missedCallNotificationChannelName = bundle.getString(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME
            )
            data.isShowFullLockedScreen = bundle.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_IS_SHOW_FULL_LOCKED_SCREEN,
                true
            )
            return data
        }
    }
}
