package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.app.Notification
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
                    if(it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                        ContextCompat.startForegroundService(context, intent)
                    }else {
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
        when (intent?.action) {
            CallkitConstants.ACTION_CALL_START -> {
                intent.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                    ?.let {
                        if (it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                            getCallkitNotificationManager()?.createNotificationChanel(it)
                            showOngoingCallNotification(it)
                        } else {
                            stopSelf()
                        }
                    }
            }
            CallkitConstants.ACTION_CALL_ACCEPT -> {
                intent.getBundleExtra(CallkitConstants.EXTRA_CALLKIT_INCOMING_DATA)
                    ?.let {
                        getCallkitNotificationManager()?.clearIncomingNotification(it, true)
                        if (it.getBoolean(CallkitConstants.EXTRA_CALLKIT_CALLING_SHOW, true)) {
                            showOngoingCallNotification(it)
                        } else {
                            stopSelf()
                        }
                    }
            }
            null -> {
                // OS restarted the service after kill (START_STICKY with null intent).
                // Flutter engine may not be running yet — create a standalone manager
                // using only Context so we can restore startForeground() without the plugin.
                val activeCalls = getDataActiveCalls(this)
                val bundle = activeCalls.firstOrNull()?.toBundle()
                if (bundle != null) {
                    val pluginManager = getCallkitNotificationManager()
                    val manager = pluginManager
                        ?: CallkitNotificationManager(this, CallkitSoundPlayerManager(this))
                    manager.createNotificationChanel(bundle)
                    val notification = manager.getOnGoingCallNotification(bundle, false)
                    if (notification != null) {
                        val typeCall = bundle.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, -1)
                        startForeground(notification.id, notification.notification, typeCall > 0)
                        if (pluginManager == null) manager.destroy()
                    } else {
                        if (pluginManager == null) manager.destroy()
                        stopSelf()
                    }
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
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
                mask = mask or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                if (isVideo) {
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
