package com.hiennv.flutter_callkit_incoming

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle

class CallkitNotificationManager(private val context: Context) {

    companion object {

    }

    fun showIncomingNotification(data: Bundle) {

    }

    fun showMissCallNotification(data: Bundle) {

    }


    fun clearNotification(notificationId: Int) {

    }

    fun createNotificationChanel() {

    }

    private fun getActivityPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intent = CallkitIncomingActivity.getIntent(data)
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}