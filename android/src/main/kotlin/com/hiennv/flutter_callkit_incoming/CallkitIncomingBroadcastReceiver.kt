package com.hiennv.flutter_callkit_incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class CallkitIncomingBroadcastReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_CALL_INCOMING =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
        const val ACTION_CALL_START = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_START"
        const val ACTION_CALL_ACCEPT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        const val ACTION_CALL_DECLINE =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        const val ACTION_CALL_ENDED =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
        const val ACTION_CALL_TIMEOUT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
        const val ACTION_CALL_CALLBACK =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK"


        const val EXTRA_CALLKIT_INCOMING_DATA = "EXTRA_CALLKIT_INCOMING_DATA"

        const val EXTRA_CALLKIT_ID = "EXTRA_CALLKIT_ID"
        const val EXTRA_CALLKIT_NAME_CALLER = "EXTRA_CALLKIT_NAME_CALLER"
        const val EXTRA_CALLKIT_APP_NAME = "EXTRA_CALLKIT_APP_NAME"
        const val EXTRA_CALLKIT_HANDLE = "EXTRA_CALLKIT_HANDLE"
        const val EXTRA_CALLKIT_TYPE = "EXTRA_CALLKIT_TYPE"
        const val EXTRA_CALLKIT_AVATAR = "EXTRA_CALLKIT_AVATAR"
        const val EXTRA_CALLKIT_DURATION = "EXTRA_CALLKIT_DURATION"
        const val EXTRA_CALLKIT_EXTRA = "EXTRA_CALLKIT_EXTRA"
        const val EXTRA_CALLKIT_HEADERS = "EXTRA_CALLKIT_HEADERS"
        const val EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION = "EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_SHOW_LOGO = "EXTRA_CALLKIT_IS_SHOW_LOGO"
        const val EXTRA_CALLKIT_RINGTONE_PATH = "EXTRA_CALLKIT_RINGTONE_PATH"
        const val EXTRA_CALLKIT_BACKGROUND_COLOR = "EXTRA_CALLKIT_BACKGROUND_COLOR"
        const val EXTRA_CALLKIT_BACKGROUND_URL = "EXTRA_CALLKIT_BACKGROUND_URL"
        const val EXTRA_CALLKIT_ACTION_COLOR = "EXTRA_CALLKIT_ACTION_COLOR"

        const val EXTRA_CALLKIT_ACTION_FROM = "EXTRA_CALLKIT_ACTION_FROM"

        fun getIntentIncoming(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_INCOMING
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentStart(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_START
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentAccept(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_ACCEPT
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentDecline(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_DECLINE
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentEnded(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_ENDED
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentTimeout(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_TIMEOUT
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentCallback(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_CALLBACK
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }
    }



    override fun onReceive(context: Context, intent: Intent) {
        val callkitSoundPlayer = CallkitSoundPlayer.getInstance(context.applicationContext)
        val callkitNotificationManager = CallkitNotificationManager(context)
        val action = intent.action ?: return
        val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA) ?: return
        when (action) {
            ACTION_CALL_INCOMING -> {
                try {
                    sendEventFlutter(ACTION_CALL_INCOMING, data)
                    val duration = data.getLong(EXTRA_CALLKIT_DURATION, 0L)
                    callkitSoundPlayer.setDuration(duration)
                    callkitSoundPlayer.play(data)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_START -> {
                try {
                    sendEventFlutter(ACTION_CALL_START, data)
                    addCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ACCEPT -> {
                try {
                    Utils.backToForeground(context)
                    sendEventFlutter(ACTION_CALL_ACCEPT, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                    addCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_DECLINE -> {
                try {
                    sendEventFlutter(ACTION_CALL_DECLINE, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ENDED -> {
                try {
                    sendEventFlutter(ACTION_CALL_ENDED, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_TIMEOUT -> {
                try {
                    sendEventFlutter(ACTION_CALL_TIMEOUT, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.showMissCallNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_CALLBACK -> {
                try {
                    callkitNotificationManager.clearMissCallNotification(data)
                    sendEventFlutter(ACTION_CALL_CALLBACK, data)
                    Utils.backToForeground(context)
                    val closeNotificationPanel = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    context.sendBroadcast(closeNotificationPanel)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun sendEventFlutter(event: String, data: Bundle) {
        val android = mapOf(
            "isCustomNotification" to data.getBoolean(EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false),
            "ringtonePath" to data.getString(EXTRA_CALLKIT_RINGTONE_PATH, ""),
            "backgroundColor" to data.getString(EXTRA_CALLKIT_BACKGROUND_COLOR, ""),
            "backgroundUrl" to data.getString(EXTRA_CALLKIT_BACKGROUND_URL, ""),
            "actionColor" to data.getString(EXTRA_CALLKIT_ACTION_COLOR, "")
        )
        val forwardData = mapOf(
            "id" to data.getString(EXTRA_CALLKIT_ID, ""),
            "nameCaller" to data.getString(EXTRA_CALLKIT_NAME_CALLER, ""),
            "avatar" to data.getString(EXTRA_CALLKIT_AVATAR, ""),
            "number" to data.getString(EXTRA_CALLKIT_HANDLE, ""),
            "type" to data.getInt(EXTRA_CALLKIT_TYPE, 0),
            "duration" to data.getLong(EXTRA_CALLKIT_DURATION, 0L),
            "extra" to data.getSerializable(EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>,
            "android" to android
        )
        FlutterCallkitIncomingPlugin.sendEvent(event, forwardData)
    }
}