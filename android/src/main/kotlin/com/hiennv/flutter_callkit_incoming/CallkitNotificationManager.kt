package com.hiennv.flutter_callkit_incoming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_AVATAR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_DURATION
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_ID
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NUMBER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_TYPE
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception
import kotlin.properties.Delegates


class CallkitNotificationManager(private val context: Context) {

    companion object {

    }

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationId: Int = 9696


    private var targetLoadAvatar = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            notificationBuilder.setLargeIcon(bitmap)
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
        Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, "")).into(targetLoadAvatar)
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            Notification.PRIORITY_MAX
        }
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
        notificationBuilder.color = Color.parseColor("#4CAF50")
        notificationBuilder.setChannelId("callkit_incoming_channel_id")
        getNotificationManager().notify(notificationId, notificationBuilder.build())

    }

    fun showMissCallNotification(data: Bundle) {
        createNotificationChanel()
        val missedCallSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val typeCall = data.getInt(EXTRA_CALLKIT_TYPE, -1)
        var smallIcon = context.applicationInfo.icon
        if (typeCall > 0) {
            smallIcon = R.drawable.ic_video
        } else {
            if (smallIcon >= 0) {
                smallIcon = R.drawable.ic_accept
            }
        }
        notificationBuilder = NotificationCompat.Builder(context, "callkit_missed_channel_id")
        notificationBuilder.setContentTitle(data.getString(EXTRA_CALLKIT_NAME_CALLER, ""))
        notificationBuilder.setContentText(data.getString(EXTRA_CALLKIT_NUMBER, ""))
        notificationBuilder.setSmallIcon(smallIcon)
        Picasso.get().load(data.getString(EXTRA_CALLKIT_AVATAR, "")).into(targetLoadAvatar)
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_DEFAULT
        } else {
            Notification.PRIORITY_DEFAULT
        }
        notificationBuilder.setSound(missedCallSound)
        notificationBuilder.setContentIntent(getActivityPendingIntent(notificationId, data))

        getNotificationManager().notify(notificationId, notificationBuilder.build())
    }


    fun clearIncomingNotification() {
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
                description = "Call Notifications"
            }
            getNotificationManager().createNotificationChannel(channelCall)

            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()
            val channelMissedCall = NotificationChannel(
                "callkit_missed_channel_id",
                "Missed Call",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Call Notifications"
                setSound(sound, attributes)
                vibrationPattern = longArrayOf(0, 1000)
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