package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Connection
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class CallkitConnection(private val context: Context) : Connection() {

    override fun onAnswer() {
        super.onAnswer()
        Log.d("CallkitIncoming", "onAnswer called - sending broadcast to accept call")

        val intent = Intent("com.hiennv.flutter_callkit_incoming.ACTION_ANSWER_CALL")
        context.sendBroadcast(intent)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        Log.d("CallkitIncoming", "onDisconnect called - sending broadcast to decline call")

        val intent = Intent("com.hiennv.flutter_callkit_incoming.ACTION_DECLINE_CALL")
        context.sendBroadcast(intent)
    }
}
