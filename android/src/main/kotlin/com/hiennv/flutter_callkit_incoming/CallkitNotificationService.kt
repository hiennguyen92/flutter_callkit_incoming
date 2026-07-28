package com.hiennv.flutter_callkit_incoming

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class CallkitNotificationService : Service() {

    companion object {
        private const val TAG = "CallkitNotificationSvc"
        private const val PLACEHOLDER_NOTIFICATION_ID = 999991

        private val ActionForeground = listOf(
            CallkitConstants.ACTION_CALL_START,
            CallkitConstants.ACTION_CALL_ACCEPT
        )

        fun startServiceWithAction(context: Context, action: String, data: Bundle?) {
            val intent = Intent(context, CallkitNotificationService::class.java).apply {
                this.action = action
                putExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && intent.action in ActionForeground) {
                data?.let {
                    if (it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                        ContextCompat.startForegroundService(context, intent)
                    } else {
                        context.startService(intent)
                    }
                }
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CallkitNotificationService::class.java)
            context.stopService(intent)
        }
    }

    // Get notification manager dynamically to handle plugin lifecycle properly
    private fun getCallkitNotificationManager(): CallkitNotificationManager? {
        return FlutterCallkitIncomingPlugin.getInstance()?.getCallkitNotificationManager()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action in ActionForeground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showPlaceholderForegroundNotification()
        }

        try {
            if (action == CallkitConstants.ACTION_CALL_START) {
                intent?.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                    ?.let {
                        if (it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                            getCallkitNotificationManager()?.createNotificationChanel(it)
                            showOngoingCallNotification(it)
                        } else {
                            stopSelf()
                        }
                    }
            }
            if (action == CallkitConstants.ACTION_CALL_ACCEPT) {
                intent?.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                    ?.let {
                        getCallkitNotificationManager()?.clearIncomingNotification(it, true)
                        if (it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                            showOngoingCallNotification(it)
                        } else {
                            stopSelf()
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showPlaceholderForegroundNotification() {
        try {
            val channelId = CallkitNotificationManager.NOTIFICATION_CHANNEL_ID_ONGOING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                var channel = notificationManager.getNotificationChannel(channelId)
                if (channel == null) {
                    channel = NotificationChannel(
                        channelId,
                        "Ongoing Call",
                        NotificationManager.IMPORTANCE_LOW
                    )
                    notificationManager.createNotificationChannel(channel)
                }
            }

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentTitle("")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)

            startForeground(PLACEHOLDER_NOTIFICATION_ID, builder.build(), false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show placeholder foreground notification", e)
            stopSelf()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showOngoingCallNotification(bundle: Bundle) {
        val callkitNotification =
            getCallkitNotificationManager()?.getOnGoingCallNotification(bundle, false)
        if (callkitNotification != null) {
            val typeCall = bundle.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, -1)
            startForeground(
                callkitNotification.id,
                callkitNotification.notification,
                typeCall > 0
            )
        }
    }

    private fun startForeground(notificationId: Int, notification: Notification, isVideo: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var mask = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    mask = mask or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
                if (isVideo && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mask = mask or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                }
            }
            startForeground(notificationId, notification, mask)
        } else {
            startForeground(notificationId, notification)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Don't destroy the notification manager here as it's shared across the app
        // The plugin will handle cleanup when all engines are detached
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Don't kill the FGS. The app might be closed by user but the call is still ongoing
    }
}
