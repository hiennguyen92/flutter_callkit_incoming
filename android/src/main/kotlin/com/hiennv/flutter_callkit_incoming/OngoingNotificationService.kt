package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.hiennv.flutter_callkit_incoming.widgets.CircleTransform
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.OkHttpClient

class OngoingNotificationService : Service() {


    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationViews: RemoteViews? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOngoingCallNotification(intent?.extras!!)
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun showOngoingCallNotification(data: Bundle) {

        val onGoingNotificationId = data.getInt(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID,
            data.getString(CallkitConstants.EXTRA_CALLKIT_ID, "callkit_incoming").hashCode() + 999
        )

        notificationBuilder = NotificationCompat.Builder(
            this,
            CallkitNotificationManager.NOTIFICATION_CHANNEL_ID_ONGOING
        )
        notificationBuilder.setChannelId(CallkitNotificationManager.NOTIFICATION_CHANNEL_ID_ONGOING)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                notificationBuilder.setCategory(Notification.CATEGORY_CALL)
            }
        }
        val textCalling = data.getString(CallkitConstants.EXTRA_CALLKIT_CALLING_SUBTITLE, "")
        notificationBuilder.setSubText(if (TextUtils.isEmpty(textCalling)) getString(R.string.text_calling) else textCalling)
        notificationBuilder.setSmallIcon(R.drawable.ic_accept)
        val isCustomNotification =
            data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false)
        if (isCustomNotification) {
            notificationViews =
                RemoteViews(packageName, R.layout.layout_custom_ongoing_notification)
            notificationViews?.setTextViewText(
                R.id.tvNameCaller,
                data.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, "")
            )
            val isShowCallID =
                data?.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID, false)
            if (isShowCallID == true) {
                notificationViews?.setTextViewText(
                    R.id.tvNumber,
                    String.format(
                        " • %1s",
                        data.getString(CallkitConstants.EXTRA_CALLKIT_HANDLE, "")
                    )
                )
            }
            notificationViews?.setOnClickPendingIntent(
                R.id.llHangup,
                getHangupPendingIntent(onGoingNotificationId, data)
            )
            val isShowHangup = data.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_CALLING_HANG_UP_SHOW,
                true
            )
            notificationViews?.setViewVisibility(
                R.id.llHangup,
                if (isShowHangup) View.VISIBLE else View.GONE
            )
            val textHangup =
                data.getString(CallkitConstants.EXTRA_CALLKIT_CALLING_HANG_UP_TEXT, "")
            notificationViews?.setTextViewText(
                R.id.tvHangUp,
                if (TextUtils.isEmpty(textHangup)) getString(R.string.text_hang_up) else textHangup
            )
            val textTapOpen =
                data.getString(CallkitConstants.EXTRA_CALLKIT_CALLING_TAP_OPEN_TEXT, "")
            notificationViews?.setTextViewText(
                R.id.tvTapOpen,
                if (TextUtils.isEmpty(textTapOpen)) getString(R.string.text_tab_open) else textTapOpen
            )

            val avatarUrl = data.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, "")
            if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                val headers =
                    data.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>

                getPicassoInstance(this@OngoingNotificationService, headers).load(avatarUrl)
                    .transform(CircleTransform())
                    .into(createAvatarTargetCustom(onGoingNotificationId))
            }
            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(notificationViews)
            notificationBuilder.setCustomBigContentView(notificationViews)
        } else {
            notificationBuilder.setContentTitle(
                data.getString(
                    CallkitConstants.EXTRA_CALLKIT_NAME_CALLER,
                    ""
                )
            )
            notificationBuilder.setContentText(
                data.getString(
                    CallkitConstants.EXTRA_CALLKIT_HANDLE,
                    ""
                )
            )
            val avatarUrl = data.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, "")
            if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                val headers =
                    data.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>

                getPicassoInstance(this@OngoingNotificationService, headers).load(avatarUrl)
                    .into(createAvatarTargetDefault(onGoingNotificationId))
            }
            val isShowHangup = data.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_CALLING_HANG_UP_SHOW,
                true
            )
            if (isShowHangup) {
                val textHangup =
                    data.getString(CallkitConstants.EXTRA_CALLKIT_CALLING_HANG_UP_TEXT, "")
                val hangUpAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                    R.drawable.transparent,
                    if (TextUtils.isEmpty(textHangup)) this.getString(R.string.text_hang_up) else textHangup,
                    getHangupPendingIntent(onGoingNotificationId, data)
                ).build()
                notificationBuilder.addAction(hangUpAction)
            }
        }
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_LOW
        } else {
            Notification.PRIORITY_LOW
        }
        notificationBuilder.setSound(null)
        notificationBuilder.setContentIntent(getAppPendingIntent(onGoingNotificationId, data))
        val actionColor = data.getString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50")
        try {
            notificationBuilder.color = Color.parseColor(actionColor)
        } catch (_: Exception) {
        }
        notificationBuilder.setOngoing(true)
        val notification = notificationBuilder.build()
        val typeCall = data.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            if (typeCall > 0){
                serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }
            else {
                serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(
                onGoingNotificationId,
                notification,
                serviceType
            )
        } else {
            startForeground(onGoingNotificationId, notification)
        }

    }

    private fun getHangupPendingIntent(notificationId: Int, data: Bundle): PendingIntent {
        val endedIntent = CallkitIncomingBroadcastReceiver.getIntentEnded(this, data)
        return PendingIntent.getBroadcast(this, notificationId, endedIntent, getFlagPendingIntent())
    }


    private fun getAppPendingIntent(notificationId: Int, data: Bundle): PendingIntent {
        val intent: Intent? = AppUtils.getAppIntent(this@OngoingNotificationService, data = data)
        return PendingIntent.getActivity(
            this@OngoingNotificationService,
            notificationId,
            intent,
            getFlagPendingIntent()
        )
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    private fun getFlagPendingIntent(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun getPicassoInstance(context: Context, headers: HashMap<String, Any?>): Picasso {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val newRequestBuilder: okhttp3.Request.Builder = chain.request().newBuilder()
                for ((key, value) in headers) {
                    newRequestBuilder.addHeader(key, value.toString())
                }
                chain.proceed(newRequestBuilder.build())
            }
            .build()
        return Picasso.Builder(context)
            .downloader(OkHttp3Downloader(client))
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun createAvatarTargetCustom(notificationId: Int): Target {
        return object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                notificationViews?.setImageViewBitmap(R.id.ivAvatar, bitmap)
                notificationViews?.setViewVisibility(R.id.ivAvatar, View.VISIBLE)
                getNotificationManager().notify(notificationId, notificationBuilder.build())
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAvatarTargetDefault(notificationId: Int): Target {
        return object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                notificationBuilder.setLargeIcon(bitmap)
                getNotificationManager().notify(notificationId, notificationBuilder.build())
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
        }

    }

    private fun getNotificationManager(): NotificationManagerCompat {
        return NotificationManagerCompat.from(this@OngoingNotificationService)
    }

}

