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
        Log.e("onReceive", action)
        when (action) {
            ACTION_CALL_INCOMING -> {
                callkitSoundPlayer.setDuration(20000L)
                val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA)
                callkitSoundPlayer.play(data)
            }
            ACTION_CALL_ACCEPT -> {
                Utils.backToForeground(context)
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearNotification(9696)
            }
            ACTION_CALL_DECLINE -> {
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearNotification(9696)
            }
            ACTION_CALL_ENDED -> {
                callkitSoundPlayer.stop()
            }
            ACTION_CALL_TIMEOUT -> {
                callkitSoundPlayer.stop()
                callkitNotificationManager.clearNotification(9696)
            }
        }
    }
}