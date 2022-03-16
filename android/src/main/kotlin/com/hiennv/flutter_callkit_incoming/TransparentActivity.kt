package com.hiennv.flutter_callkit_incoming

import android.app.Activity
import android.os.Bundle
import android.util.Log

class TransparentActivity : Activity() {

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.getBundleExtra("data")
        Log.i("data", data.toString())
        val acceptIntent = CallkitIncomingBroadcastReceiver.getIntentAccept(this@TransparentActivity, data)
        sendBroadcast(acceptIntent)
        finish()
        overridePendingTransition(0, 0)
    }
}