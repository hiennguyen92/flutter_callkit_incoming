package com.hiennv.flutter_callkit_incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class CallkitIncomingBroadcastReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_CALL_INCOMING =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
        const val ACTION_CALL_ACCEPT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        const val ACTION_CALL_DECLINE =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        const val ACTION_CALL_ENDED =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
        const val ACTION_CALL_TIMEOUT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"


        const val EXTRA_CALLKIT_INCOMING_DATA = "EXTRA_CALLKIT_INCOMING_DATA"

        const val EXTRA_CALLKIT_ID = "EXTRA_CALLKIT_ID"
        const val EXTRA_CALLKIT_NAME_CALLER = "EXTRA_CALLKIT_NAME_CALLER"
        const val EXTRA_CALLKIT_NUMBER = "EXTRA_CALLKIT_NUMBER"
        const val EXTRA_CALLKIT_TYPE = "EXTRA_CALLKIT_TYPE"
        const val EXTRA_CALLKIT_AVATAR = "EXTRA_CALLKIT_AVATAR"
        const val EXTRA_CALLKIT_DURATION = "EXTRA_CALLKIT_DURATION"
        const val EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION = "EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION"
        const val EXTRA_CALLKIT_BACKGROUND_COLOR = "EXTRA_CALLKIT_BACKGROUND_COLOR"
        const val EXTRA_CALLKIT_BACKGROUND = "EXTRA_CALLKIT_BACKGROUND"
        const val EXTRA_CALLKIT_ACTION_COLOR = "EXTRA_CALLKIT_ACTION_COLOR"

        fun getIntentIncoming(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_INCOMING
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

        fun getIntentTimeout(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_TIMEOUT
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val callkitNotificationManager = CallkitNotificationManager(context)
        val callkitSoundPlayer = CallkitSoundPlayer.getInstance(context)
        //build ringtone

        val action = intent.action ?: return
        val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA) ?: return
        Log.e("onReceive", action)
        when (action) {
            ACTION_CALL_INCOMING -> {
                val duration = data.getLong(EXTRA_CALLKIT_DURATION, 0L)
                callkitSoundPlayer.setDuration(duration)
                callkitSoundPlayer.play(data)
            }
            ACTION_CALL_ACCEPT -> {
                Utils.backToForeground(context)
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearIncomingNotification(data)
            }
            ACTION_CALL_DECLINE -> {
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearIncomingNotification(data)
            }
            ACTION_CALL_ENDED -> {
                callkitSoundPlayer.stop()
            }
            ACTION_CALL_TIMEOUT -> {
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearIncomingNotification(data)
                callkitNotificationManager.showMissCallNotification(data)
            }
        }
    }
}