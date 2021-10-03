package com.hiennv.flutter_callkit_incoming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_ACTION_COLOR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_AVATAR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_DURATION
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_ID
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NUMBER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_TYPE
import com.hiennv.flutter_callkit_incoming.widgets.CircleTransform
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target


class CallkitNotificationManager(private val context: Context) {

    companion object {

    }

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationViews: RemoteViews? = null
    private var notificationId: Int = 9696


    private var targetLoadAvatarDefault = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            notificationBuilder.setLargeIcon(bitmap)
            getNotificationManager().notify(notificationId, notificationBuilder.build())
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
    }

    private var targetLoadAvatarCustomize = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            notificationViews?.setImageViewBitmap(R.id.ivAvatar, bitmap)
            notificationViews?.setViewVisibility(R.id.ivAvatar, View.VISIBLE)
            getNotificationManager().notify(notificationId, notificationBuilder.build())
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
    }


    fun showIncomingNotification(data: Bundle) {

        notificationId = data.getString(EXTRA_CALLKIT_ID, "callkit_incoming").hashCode()

        createNotificationChanel()

        notificationBuilder = NotificationCompat.Builder(context, "callkit_incoming_channel_id")
        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_CALL)
        }
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setOngoing(true)
        notificationBuilder.setTimeoutAfter(data.getLong(EXTRA_CALLKIT_DURATION, 0L))
        notificationBuilder.setOnlyAlertOnce(true)
        notificationBuilder.setSound(null)
        notificationBuilder.setFullScreenIntent(
            getActivityPendingIntent(notificationId, data), true
        )
        notificationBuilder.setContentIntent(getActivityPendingIntent(notificationId, data))
        val typeCall = data.getInt(EXTRA_CALLKIT_TYPE, -1)
        var smallIcon = context.applicationInfo.icon
        if (typeCall > 0) {
            smallIcon = R.drawable.ic_video
        } else {
            if (smallIcon >= 0) {
                smallIcon = R.drawable.ic_accept
            }
        }
        notificationBuilder.setSmallIcon(smallIcon)
        val actionColor = data.getString(EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50")
        try {
            notificationBuilder.color = Color.parseColor(actionColor)
        } catch (error: Exception) {
        }
        notificationBuilder.setChannelId("callkit_incoming_channel_id")
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            Notification.PRIORITY_MAX
        }
        val isCustomNotification = data.getBoolean(EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false)
        if (isCustomNotification) {
            notificationViews =
                RemoteViews(context.packageName, R.layout.layout_custom_notification)
            notificationViews?.setTextViewText(
                R.id.tvNameCaller,
                data.getString(EXTRA_CALLKIT_NAME_CALLER, "")
            )
            notificationViews?.setTextViewText(
                R.id.tvNumber,
                data.getString(EXTRA_CALLKIT_NUMBER, "")
            )
            notificationViews?.setOnClickPendingIntent(
                R.id.llDecline,
                getDeclinePendingIntent(notificationId, data)
            )
            notificationViews?.setOnClickPendingIntent(
                R.id.llAccept,
                getAcceptPendingIntent(notificationId, data)
            )
            Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, ""))
                .transform(CircleTransform())
                .into(targetLoadAvatarCustomize)
            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(notificationViews)
        } else {
            Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, ""))
                .into(targetLoadAvatarDefault)
            notificationBuilder.setContentTitle(data.getString(EXTRA_CALLKIT_NAME_CALLER, ""))
            notificationBuilder.setContentText(data.getString(EXTRA_CALLKIT_NUMBER, ""))
            val declineAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_decline,
                context.getString(R.string.text_decline),
                getDeclinePendingIntent(notificationId, data)
            ).build()
            notificationBuilder.addAction(declineAction)
            val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_accept,
                context.getString(R.string.text_accept),
                getAcceptPendingIntent(notificationId, data)
            ).build()
            notificationBuilder.addAction(acceptAction)
        }
        getNotificationManager().notify(notificationId, notificationBuilder.build())
    }

    fun showMissCallNotification(data: Bundle) {
        createNotificationChanel()
        val missedCallSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val typeCall = data.getInt(EXTRA_CALLKIT_TYPE, -1)
        var smallIcon = context.applicationInfo.icon
        if (typeCall > 0) {
            smallIcon = R.drawable.ic_video_missed
        } else {
            if (smallIcon >= 0) {
                smallIcon = R.drawable.ic_call_missed
            }
        }
        notificationBuilder = NotificationCompat.Builder(context, "callkit_missed_channel_id")
        notificationBuilder.setContentTitle(data.getString(EXTRA_CALLKIT_NAME_CALLER, ""))
        notificationBuilder.setContentText(data.getString(EXTRA_CALLKIT_NUMBER, ""))
        notificationBuilder.setSmallIcon(smallIcon)
        val isCustomNotification = data.getBoolean(EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false)
        if (isCustomNotification){
            Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, "")).transform(CircleTransform()).into(targetLoadAvatarDefault)
        }else {
            Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, "")).into(targetLoadAvatarDefault)
        }
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_DEFAULT
        } else {
            Notification.PRIORITY_DEFAULT
        }
        notificationBuilder.setSound(missedCallSound)
        notificationBuilder.setContentIntent(getActivityPendingIntent(notificationId, data))
        val actionColor = data.getString(EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50")
        try {
            notificationBuilder.color = Color.parseColor(actionColor)
        } catch (error: Exception) {
        }
        notificationBuilder.setChannelId("callkit_missed_channel_id")

        getNotificationManager().notify(notificationId, notificationBuilder.build())
    }


    fun clearIncomingNotification(data: Bundle) {
        notificationId = data.getString(EXTRA_CALLKIT_ID, "callkit_incoming").hashCode()
        getNotificationManager().cancel(notificationId)
    }

    private fun createNotificationChanel() {
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelCall = NotificationChannel(
                "callkit_incoming_channel_id",
                "Incoming Call",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ""
                vibrationPattern =
                    longArrayOf(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000)
                lightColor = Color.RED
                enableLights(true)
                enableVibration(true)
            }
            getNotificationManager().createNotificationChannel(channelCall)

            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()
            val channelMissedCall = NotificationChannel(
                "callkit_missed_channel_id",
                "Missed Call",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = ""
                vibrationPattern = longArrayOf(0, 1000)
                lightColor = Color.RED
                enableLights(true)
                enableVibration(true)
            }
            getNotificationManager().createNotificationChannel(channelMissedCall)
        }
    }

    private fun getAcceptPendingIntent(id: Int, data: Bundle): PendingIntent {
        val acceptIntent = CallkitIncomingBroadcastReceiver.getIntentAccept(context, data)
        return PendingIntent.getBroadcast(
            context,
            id,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getDeclinePendingIntent(id: Int, data: Bundle): PendingIntent {
        val declineIntent = CallkitIncomingBroadcastReceiver.getIntentDecline(context, data)
        return PendingIntent.getBroadcast(
            context,
            id,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getActivityPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intent = CallkitIncomingActivity.getIntent(data)
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNotificationManager(): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

}