package com.hiennv.flutter_callkit_incoming

import android.os.Bundle
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Suppress("UNCHECKED_CAST")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Data(val args: Map<String, Any?>) {

    constructor() : this(emptyMap())

    @JsonProperty("id")
    var id: String = args["id"] as? String ?: ""

    @JsonProperty("uuid")
    var uuid: String = args["uuid"] as? String ?: ""

    @JsonProperty("nameCaller")
    var nameCaller: String = args["nameCaller"] as? String ?: ""

    @JsonProperty("appName")
    var appName: String = args["appName"] as? String ?: ""

    @JsonProperty("handle")
    var handle: String = args["handle"] as? String ?: ""

    @JsonProperty("avatar")
    var avatar: String = args["avatar"] as? String ?: ""

    @JsonProperty("type")
    var type: Int = args["type"]?.toString()?.toIntOrNull() ?: 0

    @JsonProperty("duration")
    var duration: Long = args["duration"]?.toString()?.toLongOrNull() ?: 30000L

    @JsonProperty("textAccept")
    var textAccept: String = args["textAccept"] as? String ?: ""

    @JsonProperty("extra")
    var extra: HashMap<String, Any?> = (args["extra"] as? HashMap<String, Any?>) ?: HashMap()

    @JsonProperty("headers")
    var headers: HashMap<String, Any?> = (args["headers"] as? HashMap<String, Any?>) ?: HashMap()

    @JsonProperty("from")
    var from: String = args["from"] as? String ?: ""

    @JsonProperty("isCustomNotification")
    var isCustomNotification: Boolean = args["isCustomNotification"] as? Boolean ?: false

    @JsonProperty("isCustomSmallExNotification")
    var isCustomSmallExNotification: Boolean = args["isCustomSmallExNotification"] as? Boolean ?: false

    @JsonProperty("isShowLogo")
    var isShowLogo: Boolean = args["isShowLogo"] as? Boolean ?: false

    @JsonProperty("isShowCallID")
    var isShowCallID: Boolean = args["isShowCallID"] as? Boolean ?: false

    @JsonProperty("ringtonePath")
    var ringtonePath: String = args["ringtonePath"] as? String ?: ""

    @JsonProperty("backgroundColor")
    var backgroundColor: String = args["backgroundColor"] as? String ?: "#0955fa"

    @JsonProperty("backgroundUrl")
    var backgroundUrl: String = args["backgroundUrl"] as? String ?: ""

    @JsonProperty("textColor")
    var textColor: String = args["textColor"] as? String ?: "#ffffff"

    @JsonProperty("actionColor")
    var actionColor: String = args["actionColor"] as? String ?: "#4CAF50"

    @JsonProperty("incomingCallNotificationChannelName")
    var incomingCallNotificationChannelName: String? = args["incomingCallNotificationChannelName"] as? String

    @JsonProperty("missedCallNotificationChannelName")
    var missedCallNotificationChannelName: String? = args["missedCallNotificationChannelName"] as? String

    @JsonProperty("missedNotificationId")
    var missedNotificationId: Int? = (args["missedCallNotification"] as? Map<String, Any?>)?.get("id") as? Int

    @JsonProperty("isShowMissedCallNotification")
    var isShowMissedCallNotification: Boolean = (args["missedCallNotification"] as? Map<String, Any?>)?.get("showNotification") as? Boolean ?: true

    @JsonProperty("missedNotificationCount")
    var missedNotificationCount: Int = (args["missedCallNotification"] as? Map<String, Any?>)?.get("count") as? Int ?: 1

    @JsonProperty("missedNotificationSubtitle")
    var missedNotificationSubtitle: String? = (args["missedCallNotification"] as? Map<String, Any?>)?.get("subtitle") as? String

    @JsonProperty("missedNotificationCallbackText")
    var missedNotificationCallbackText: String? = (args["missedCallNotification"] as? Map<String, Any?>)?.get("callbackText") as? String

    @JsonProperty("isShowCallback")
    var isShowCallback: Boolean = (args["missedCallNotification"] as? Map<String, Any?>)?.get("isShowCallback") as? Boolean ?: true

    @JsonProperty("isAccepted")
    var isAccepted: Boolean = args["isAccepted"] as? Boolean ?: false

    @JsonProperty("isOnHold")
    var isOnHold: Boolean = args["isOnHold"] as? Boolean ?: false

    @JsonProperty("audioRoute")
    var audioRoute: Int = args["audioRoute"]?.toString()?.toIntOrNull() ?: 1

    @JsonProperty("isMuted")
    var isMuted: Boolean = args["isMuted"] as? Boolean ?: false

    @JsonProperty("isShowFullLockedScreen")
    var isShowFullLockedScreen: Boolean = args["isShowFullLockedScreen"] as? Boolean ?: true

    @JsonProperty("isImportant")
    var isImportant: Boolean = args["isImportant"] as? Boolean ?: false

    @JsonProperty("isBot")
    var isBot: Boolean = args["isBot"] as? Boolean ?: false

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Data) return false
        return this.id == other.id
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
            bundle.putInt(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID, it)
        }
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SHOW, isShowMissedCallNotification)
        bundle.putInt(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_COUNT, missedNotificationCount)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SUBTITLE, missedNotificationSubtitle)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_SHOW, isShowCallback)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_TEXT, missedNotificationCallbackText)

        bundle.putSerializable(CallkitConstants.EXTRA_CALLKIT_EXTRA, extra)
        bundle.putSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS, headers)

        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, isCustomNotification)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION, isCustomSmallExNotification)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_LOGO, isShowLogo)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID, isShowCallID)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_RINGTONE_PATH, ringtonePath)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_BACKGROUND_COLOR, backgroundColor)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_BACKGROUND_URL, backgroundUrl)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_TEXT_COLOR, textColor)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, actionColor)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_ACTION_FROM, from)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME, incomingCallNotificationChannelName)
        bundle.putString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME, missedCallNotificationChannelName)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_FULL_LOCKED_SCREEN, isShowFullLockedScreen)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_IMPORTANT, isImportant)
        bundle.putBoolean(CallkitConstants.EXTRA_CALLKIT_IS_BOT, isBot)

        return bundle
    }

    companion object {

        fun fromBundle(bundle: Bundle): Data {
            return Data(mapOf(
                "id" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_ID, ""),
                "nameCaller" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, ""),
                "appName" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_APP_NAME, ""),
                "handle" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_HANDLE, ""),
                "avatar" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, ""),
                "type" to bundle.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, 0),
                "duration" to bundle.getLong(CallkitConstants.EXTRA_CALLKIT_DURATION, 30000L),
                "textAccept" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_ACCEPT, ""),
                "isImportant" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_IMPORTANT, false),
                "isBot" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_BOT, false),
                "extra" to (bundle.getSerializable(CallkitConstants.EXTRA_CALLKIT_EXTRA) as? HashMap<String, Any?> ?: HashMap()),
                "headers" to (bundle.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as? HashMap<String, Any?> ?: HashMap()),
                "isCustomNotification" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false),
                "isCustomSmallExNotification" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION, false),
                "isShowLogo" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_LOGO, false),
                "isShowCallID" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID, false),
                "ringtonePath" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_RINGTONE_PATH, ""),
                "backgroundColor" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_BACKGROUND_COLOR, "#0955fa"),
                "backgroundUrl" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_BACKGROUND_URL, ""),
                "actionColor" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50"),
                "textColor" to bundle.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_COLOR, "#ffffff"),
                "isShowFullLockedScreen" to bundle.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_FULL_LOCKED_SCREEN, true)
            ))
        }
    }
}
