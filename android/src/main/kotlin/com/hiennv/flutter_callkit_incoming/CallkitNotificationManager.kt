package com.hiennv.flutter_callkit_incoming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class CallkitNotificationManager(private val context: Context) {

    companion object {

    }

    fun showIncomingNotification(data: Bundle) {

        val notificationID = 9696

        createNotificationChanel()

        val notificationBuilder = NotificationCompat.Builder(context, "callkit_incoming_channel_id")
        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        notificationBuilder.setCategory(Notification.CATEGORY_CALL)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setOngoing(true)
        notificationBuilder.setTimeoutAfter(20000L)
        notificationBuilder.setOnlyAlertOnce(true)
        notificationBuilder.setSound(null)
        notificationBuilder.setFullScreenIntent(
            getActivityPendingIntent(notificationID, data), true
        )
        notificationBuilder.setContentIntent(getActivityPendingIntent(notificationID, data))
        notificationBuilder.setSmallIcon(R.drawable.ic_accept) //context.applicationInfo.icon
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_video)
        notificationBuilder.setLargeIcon(largeIcon)
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            Notification.PRIORITY_MAX
        }
        notificationBuilder.setContentTitle("Hello XXX")
        notificationBuilder.setContentText("Callkit: 0123456789")
        val declineAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_decline,
            context.getString(R.string.text_decline),
            getDeclinePendingIntent(notificationID, data)
        ).build()
        notificationBuilder.addAction(declineAction)
        val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_accept,
            context.getString(R.string.text_accept),
            getAcceptPendingIntent(notificationID, data)
        ).build()
        notificationBuilder.addAction(acceptAction)
        notificationBuilder.color = Color.parseColor("#4CAF50")
        notificationBuilder.setChannelId("callkit_incoming_channel_id")
        val notification = notificationBuilder.build()

        getNotificationManager().notify(notificationID, notification)

    }

    fun showMissCallNotification(data: Bundle) {

    }


    fun clearNotification(notificationId: Int) {
        getNotificationManager().cancel(notificationId)
    }

    private fun createNotificationChanel() {
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelCall = NotificationChannel("callkit_incoming_channel_id", "Incoming Call", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Call Notifications"
            }
            getNotificationManager().createNotificationChannel(channelCall)



            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()
            val channelMissedCall = NotificationChannel("callkit_missed_channel_id", "Missed Call", NotificationManager.IMPORTANCE_HIGH).apply {
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