package com.hiennv.flutter_callkit_incoming

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class TransparentActivity : Activity() {

    companion object {
        fun getIntent(context: Context, data: Bundle?): Intent {
            val intent = Intent(context, TransparentActivity::class.java)
            intent.putExtra("data", data)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            return intent
        }
    }


    override fun onStart() {
        super.onStart()
        setVisible(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getBundleExtra("data")

        val acceptIntent = CallkitIncomingBroadcastReceiver.getIntentAccept(this, data)
        sendBroadcast(acceptIntent)

        val activityIntent = AppUtils.getAppIntentAccept(this, data)
        startActivity(activityIntent)

        finish()
        overridePendingTransition(0, 0)
    }
}