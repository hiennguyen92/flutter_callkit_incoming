package com.hiennv.flutter_callkit_incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CallkitIncomingBroadcastReceiver: BroadcastReceiver() {

    companion object {

        private const val ACTION_CALLKIT_INCOMING =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALLKIT_INCOMING"
        private const val ACTION_CALL_ACCEPT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        private const val ACTION_CALL_DECLINE =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        private const val ACTION_CALL_ENDED =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
        private const val ACTION_CALL_TIMEOUT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"

    }


    override fun onReceive(context: Context, intent: Intent) {
        //build notification
        //build ringtone

        val action = intent.action ?: return
        Log.d("onReceive", action)
        when(action){
            ACTION_CALLKIT_INCOMING -> {

            }
            ACTION_CALL_ACCEPT -> {

            }
            ACTION_CALL_DECLINE -> {

            }
            ACTION_CALL_ENDED -> {

            }
            ACTION_CALL_TIMEOUT -> {

            }
        }
    }
}