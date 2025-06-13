package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat

class CallkitNotificationService : Service() {

    companion object {

        const val CALLKIT_NOTIFICATION_SERVICE_ACTION_STARTED =
            "com.hiennv.flutter_callkit_incoming.CALLKIT_NOTIFICATION_SERVICE_ACTION_STARTED"
        const val CALLKIT_NOTIFICATION_SERVICE_ACTION_CONNECTED =
            "com.hiennv.flutter_callkit_incoming.CALLKIT_NOTIFICATION_SERVICE_ACTION_CONNECTED"

        const val CALLKIT_NOTIFICATION_SERVICE_ACTION_INCOMING =
            "com.hiennv.flutter_callkit_incoming.CALLKIT_NOTIFICATION_SERVICE_ACTION_INCOMING"


        fun startServiceWithAction(context: Context, action: String, data: Bundle?) {
            val intent = Intent(context, CallkitNotificationService::class.java).apply {
                this.action = action
                putExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

    }


    private var callkitNotificationManager: CallkitNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        this.callkitNotificationManager =
            CallkitNotificationManager(this@CallkitNotificationService)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action === CALLKIT_NOTIFICATION_SERVICE_ACTION_STARTED) {
            intent.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                ?.let { showOngoingCallNotification(it, false) }
        }
        if (intent?.action === CALLKIT_NOTIFICATION_SERVICE_ACTION_CONNECTED) {
            intent.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                ?.let { showOngoingCallNotification(it, true) }
        }
        if (intent?.action === CALLKIT_NOTIFICATION_SERVICE_ACTION_INCOMING) {
            intent.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                ?.let { showInComingNotification(it) }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun showInComingNotification(bundle: Bundle) {

        val callkitNotification =
            this.callkitNotificationManager?.getIncomingNotification(bundle)

        if (callkitNotification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    callkitNotification.id,
                    callkitNotification.notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                startForeground(callkitNotification.id, callkitNotification.notification)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showOngoingCallNotification(bundle: Bundle, isConnected: Boolean? = false) {

        val callkitNotification =
            this.callkitNotificationManager?.getOnGoingCallNotification(bundle, isConnected)

        if (callkitNotification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    callkitNotification.id,
                    callkitNotification.notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                startForeground(callkitNotification.id, callkitNotification.notification)
            }
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }


}

