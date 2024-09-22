package com.hiennv.flutter_callkit_incoming

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.hiennv.flutter_callkit_incoming.widgets.CircleTransform
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.OkHttpClient


class CallkitNotificationManager(private val context: Context) {

    companion object {
        const val PERMISSION_NOTIFICATION_REQUEST_CODE = 6969

        const val EXTRA_TIME_START_CALL = "EXTRA_TIME_START_CALL"

        private const val NOTIFICATION_CHANNEL_ID_INCOMING = "callkit_incoming_channel_id"
        private const val NOTIFICATION_CHANNEL_ID_MISSED = "callkit_missed_channel_id"
    }

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationViews: RemoteViews? = null
    private var notificationSmallViews: RemoteViews? = null
    private var notificationId: Int = 9696
    private var dataNotificationPermission: Map<String, Any> = HashMap()

    @SuppressLint("MissingPermission")
    private var targetLoadAvatarDefault = object : Target {

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            notificationBuilder.setLargeIcon(bitmap)
            getNotificationManager().notify(notificationId, notificationBuilder.build())
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }
    }

    @SuppressLint("MissingPermission")
    private var targetLoadAvatarCustomize = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            notificationViews?.setImageViewBitmap(R.id.ivAvatar, bitmap)
            notificationViews?.setViewVisibility(R.id.ivAvatar, View.VISIBLE)
            notificationSmallViews?.setImageViewBitmap(R.id.ivAvatar, bitmap)
            notificationSmallViews?.setViewVisibility(R.id.ivAvatar, View.VISIBLE)
            getNotificationManager().notify(notificationId, notificationBuilder.build())
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }
    }


    @SuppressLint("MissingPermission")
    fun showIncomingNotification(data: Bundle) {
        data.putLong(EXTRA_TIME_START_CALL, System.currentTimeMillis())

        notificationId =
            data.getString(CallkitConstants.EXTRA_CALLKIT_ID, "callkit_incoming").hashCode()
        createNotificationChanel(
            data.getString(
                CallkitConstants.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
                "Incoming Call"
            ),
            data.getString(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
                "Missed Call"
            ),
        )

        notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_INCOMING)
        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_INCOMING)
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL)
            notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        }
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setOngoing(true)
        notificationBuilder.setWhen(0)
        notificationBuilder.setTimeoutAfter(
            data.getLong(
                CallkitConstants.EXTRA_CALLKIT_DURATION,
                0L
            )
        )
        notificationBuilder.setOnlyAlertOnce(true)
        notificationBuilder.setSound(null)
        notificationBuilder.setFullScreenIntent(
            getActivityPendingIntent(notificationId, data), true
        )
        notificationBuilder.setContentIntent(getActivityPendingIntent(notificationId, data))
        notificationBuilder.setDeleteIntent(getTimeOutPendingIntent(notificationId, data))
        val typeCall = data.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, -1)
        var smallIcon = context.applicationInfo.icon
        if (typeCall > 0) {
            smallIcon = R.drawable.ic_video
        } else {
            if (smallIcon >= 0) {
                smallIcon = R.drawable.ic_accept
            }
        }
        notificationBuilder.setSmallIcon(smallIcon)
        val actionColor = data.getString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50")
        try {
            notificationBuilder.color = Color.parseColor(actionColor)
        } catch (_: Exception) {
        }
        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_INCOMING)
        notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        val isCustomNotification =
            data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false)
        val isCustomSmallExNotification =
            data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION, false)
        if (isCustomNotification) {
            notificationViews =
                RemoteViews(context.packageName, R.layout.layout_custom_notification)
            initNotificationViews(notificationViews!!, data)

            if ((Build.MANUFACTURER.equals(
                    "Samsung",
                    ignoreCase = true
                ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) || isCustomSmallExNotification
            ) {
                notificationSmallViews =
                    RemoteViews(context.packageName, R.layout.layout_custom_small_ex_notification)
                initNotificationViews(notificationSmallViews!!, data)
            } else {
                notificationSmallViews =
                    RemoteViews(context.packageName, R.layout.layout_custom_small_notification)
                initNotificationViews(notificationSmallViews!!, data)
            }

            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(notificationSmallViews)
            notificationBuilder.setCustomBigContentView(notificationViews)
            notificationBuilder.setCustomHeadsUpContentView(notificationSmallViews)
        } else {
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
                getPicassoInstance(context, headers).load(avatarUrl)
                    .into(targetLoadAvatarDefault)
            }
            val caller = data.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val person = Person.Builder()
                    .setName(caller)
                    .setImportant(
                        data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_IMPORTANT, false)
                    )
                    .setBot(data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_BOT, false))
                    .build()
                notificationBuilder.setStyle(
                    NotificationCompat.CallStyle.forIncomingCall(
                        person,
                        getDeclinePendingIntent(notificationId, data),
                        getAcceptPendingIntent(notificationId, data),
                    )
                        .setIsVideo(typeCall > 0)
                )
            } else {
                notificationBuilder.setContentTitle(caller)
                val textDecline = data.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_DECLINE, "")
                val declineAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                    R.drawable.ic_decline,
                    if (TextUtils.isEmpty(textDecline)) context.getString(R.string.text_decline) else textDecline,
                    getDeclinePendingIntent(notificationId, data)
                ).build()
                notificationBuilder.addAction(declineAction)
                val textAccept = data.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_ACCEPT, "")
                val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                    R.drawable.ic_accept,
                    if (TextUtils.isEmpty(textDecline)) context.getString(R.string.text_accept) else textAccept,
                    getAcceptPendingIntent(notificationId, data)
                ).build()
                notificationBuilder.addAction(acceptAction)
            }
        }
        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_INSISTENT
        getNotificationManager().notify(notificationId, notification)
    }

    private fun initNotificationViews(remoteViews: RemoteViews, data: Bundle) {
        remoteViews.setTextViewText(
            R.id.tvNameCaller,
            data.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, "")
        )
        val isShowCallID = data?.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID, false)
        if (isShowCallID == true) {
            remoteViews.setTextViewText(
                R.id.tvNumber,
                data.getString(CallkitConstants.EXTRA_CALLKIT_HANDLE, "")
            )
        }
        remoteViews.setOnClickPendingIntent(
            R.id.llDecline,
            getDeclinePendingIntent(notificationId, data)
        )
        val textDecline = data.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_DECLINE, "")
        remoteViews.setTextViewText(
            R.id.tvDecline,
            if (TextUtils.isEmpty(textDecline)) context.getString(R.string.text_decline) else textDecline
        )
        remoteViews.setOnClickPendingIntent(
            R.id.llAccept,
            getAcceptPendingIntent(notificationId, data)
        )
        val textAccept = data.getString(CallkitConstants.EXTRA_CALLKIT_TEXT_ACCEPT, "")
        remoteViews.setTextViewText(
            R.id.tvAccept,
            if (TextUtils.isEmpty(textAccept)) context.getString(R.string.text_accept) else textAccept
        )
        val avatarUrl = data.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, "")
        if (avatarUrl != null && avatarUrl.isNotEmpty()) {
            val headers =
                data.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>
            getPicassoInstance(context, headers).load(avatarUrl)
                .transform(CircleTransform())
                .into(targetLoadAvatarCustomize)
        }
    }

    @SuppressLint("MissingPermission")
    fun showMissCallNotification(data: Bundle) {
        val missedNotificationId = data.getInt(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID,
            data.getString(CallkitConstants.EXTRA_CALLKIT_ID, "callkit_incoming").hashCode() + 1
        )
        createNotificationChanel(
            data.getString(
                CallkitConstants.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
                "Incoming Call"
            ),
            data.getString(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
                "Missed Call"
            ),
        )
        val missedCallSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val typeCall = data.getInt(CallkitConstants.EXTRA_CALLKIT_TYPE, -1)
        var smallIcon = context.applicationInfo.icon
        if (typeCall > 0) {
            smallIcon = R.drawable.ic_video_missed
        } else {
            if (smallIcon >= 0) {
                smallIcon = R.drawable.ic_call_missed
            }
        }
        notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_MISSED)
        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_MISSED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                notificationBuilder.setCategory(Notification.CATEGORY_MISSED_CALL)
            }
        }
        val textMissedCall = data.getString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_SUBTITLE, "")
        notificationBuilder.setSubText(if (TextUtils.isEmpty(textMissedCall)) context.getString(R.string.text_missed_call) else textMissedCall)
        notificationBuilder.setSmallIcon(smallIcon)
        val isCustomNotification =
            data.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false)
        val count = data.getInt(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_COUNT, 1)
        if (count > 1) {
            notificationBuilder.setNumber(count)
        }
        if (isCustomNotification) {
            notificationViews =
                RemoteViews(context.packageName, R.layout.layout_custom_miss_notification)
            notificationViews?.setTextViewText(
                R.id.tvNameCaller,
                data.getString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, "")
            )
            val isShowCallID =
                data?.getBoolean(CallkitConstants.EXTRA_CALLKIT_IS_SHOW_CALL_ID, false)
            if (isShowCallID == true) {
                notificationViews?.setTextViewText(
                    R.id.tvNumber,
                    data.getString(CallkitConstants.EXTRA_CALLKIT_HANDLE, "")
                )
            }
            notificationViews?.setOnClickPendingIntent(
                R.id.llCallback,
                getCallbackPendingIntent(notificationId, data)
            )
            val isShowCallback = data.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_SHOW,
                true
            )
            notificationViews?.setViewVisibility(
                R.id.llCallback,
                if (isShowCallback) View.VISIBLE else View.GONE
            )
            val textCallback =
                data.getString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_TEXT, "")
            notificationViews?.setTextViewText(
                R.id.tvCallback,
                if (TextUtils.isEmpty(textCallback)) context.getString(R.string.text_call_back) else textCallback
            )

            val avatarUrl = data.getString(CallkitConstants.EXTRA_CALLKIT_AVATAR, "")
            if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                val headers =
                    data.getSerializable(CallkitConstants.EXTRA_CALLKIT_HEADERS) as HashMap<String, Any?>

                getPicassoInstance(context, headers).load(avatarUrl)
                    .transform(CircleTransform()).into(targetLoadAvatarCustomize)
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

                getPicassoInstance(context, headers).load(avatarUrl)
                    .into(targetLoadAvatarDefault)
            }
            val isShowCallback = data.getBoolean(
                CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_SHOW,
                true
            )
            if (isShowCallback) {
                val textCallback =
                    data.getString(CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_CALLBACK_TEXT, "")
                val callbackAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                    R.drawable.ic_accept,
                    if (TextUtils.isEmpty(textCallback)) context.getString(R.string.text_call_back) else textCallback,
                    getCallbackPendingIntent(notificationId, data)
                ).build()
                notificationBuilder.addAction(callbackAction)
            }
        }
        notificationBuilder.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            Notification.PRIORITY_HIGH
        }
        notificationBuilder.setSound(missedCallSound)
        notificationBuilder.setContentIntent(getAppPendingIntent(notificationId, data))
        val actionColor = data.getString(CallkitConstants.EXTRA_CALLKIT_ACTION_COLOR, "#4CAF50")
        try {
            notificationBuilder.color = Color.parseColor(actionColor)
        } catch (_: Exception) {
        }
        val notification = notificationBuilder.build()
        getNotificationManager().notify(missedNotificationId, notification)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                getNotificationManager().notify(missedNotificationId, notification)
            } catch (_: Exception) {
            }
        }, 1000)
    }


    fun clearIncomingNotification(data: Bundle, isAccepted: Boolean) {
        context.sendBroadcast(CallkitIncomingActivity.getIntentEnded(context, isAccepted))
        notificationId =
            data.getString(CallkitConstants.EXTRA_CALLKIT_ID, "callkit_incoming").hashCode()
        getNotificationManager().cancel(notificationId)
    }

    fun clearMissCallNotification(data: Bundle) {
        val missedNotificationId = data.getInt(
            CallkitConstants.EXTRA_CALLKIT_MISSED_CALL_ID,
            data.getString(CallkitConstants.EXTRA_CALLKIT_ID, "callkit_incoming").hashCode() + 1
        )
        getNotificationManager().cancel(missedNotificationId)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                getNotificationManager().cancel(missedNotificationId)
            } catch (_: Exception) {
            }
        }, 1000)
    }

    fun incomingChannelEnabled(): Boolean = getNotificationManager().run {
        val channel = getNotificationChannel(NOTIFICATION_CHANNEL_ID_INCOMING)

        return areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        channel != null &&
                        channel.importance > NotificationManagerCompat.IMPORTANCE_NONE) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    }

    private fun createNotificationChanel(
        incomingCallChannelName: String,
        missedCallChannelName: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationManager().apply {
                var channelCall = getNotificationChannel(NOTIFICATION_CHANNEL_ID_INCOMING)
                if (channelCall != null) {
                    channelCall.setSound(null, null)
                } else {
                    channelCall = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID_INCOMING,
                        incomingCallChannelName,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = ""
                        vibrationPattern =
                            longArrayOf(0, 1000, 500, 1000, 500)
                        lightColor = Color.RED
                        enableLights(true)
                        enableVibration(true)
                        setSound(null, null)
                    }
                }
                channelCall.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                channelCall.importance = NotificationManager.IMPORTANCE_HIGH

                createNotificationChannel(channelCall)

                val channelMissedCall = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_MISSED,
                    missedCallChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = ""
                    vibrationPattern = longArrayOf(0, 1000)
                    lightColor = Color.RED
                    enableLights(true)
                    enableVibration(true)
                }
                channelMissedCall.importance = NotificationManager.IMPORTANCE_DEFAULT
                createNotificationChannel(channelMissedCall)
            }
        }
    }

    private fun getAcceptPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intentTransparent = TransparentActivity.getIntent(
            context,
            CallkitConstants.ACTION_CALL_ACCEPT,
            data
        )
        return PendingIntent.getActivity(context, id, intentTransparent, getFlagPendingIntent())
    }

    private fun getDeclinePendingIntent(id: Int, data: Bundle): PendingIntent {
        val declineIntent = CallkitIncomingBroadcastReceiver.getIntentDecline(context, data)
        return PendingIntent.getBroadcast(context, id, declineIntent, getFlagPendingIntent())
    }

    private fun getTimeOutPendingIntent(id: Int, data: Bundle): PendingIntent {
        val timeOutIntent = CallkitIncomingBroadcastReceiver.getIntentTimeout(context, data)
        return PendingIntent.getBroadcast(context, id, timeOutIntent, getFlagPendingIntent())
    }

    private fun getCallbackPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intentTransparent = TransparentActivity.getIntent(
            context,
            CallkitConstants.ACTION_CALL_CALLBACK,
            data
        )
        return PendingIntent.getActivity(context, id, intentTransparent, getFlagPendingIntent())
    }

    private fun getActivityPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intent = CallkitIncomingActivity.getIntent(context, data)
        return PendingIntent.getActivity(context, id, intent, getFlagPendingIntent())
    }

    private fun getAppPendingIntent(id: Int, data: Bundle): PendingIntent {
        val intent: Intent? = AppUtils.getAppIntent(context, data = data)
        return PendingIntent.getActivity(context, id, intent, getFlagPendingIntent())
    }

    private fun getFlagPendingIntent(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }


    private fun getNotificationManager(): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
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


    fun requestNotificationPermission(activity: Activity?, map: Map<String, Any>) {
        this.dataNotificationPermission = map
        if (Build.VERSION.SDK_INT > 32) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_NOTIFICATION_REQUEST_CODE
                )
            }
        }
    }

    fun onRequestPermissionsResult(activity: Activity?, requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_NOTIFICATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    // allow
                } else {
                    //deny
                    activity?.let {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                it,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        ) {
                            //showDialogPermissionRationale()
                            if (this.dataNotificationPermission["rationaleMessagePermission"] != null) {
                                showDialogMessage(
                                    it,
                                    this.dataNotificationPermission["rationaleMessagePermission"] as String
                                ) { dialog, _ ->
                                    dialog?.dismiss()
                                    requestNotificationPermission(
                                        activity,
                                        this.dataNotificationPermission
                                    )
                                }
                            } else {
                                requestNotificationPermission(
                                    activity,
                                    this.dataNotificationPermission
                                )
                            }
                        } else {
                            //Open Setting
                            if (this.dataNotificationPermission["postNotificationMessageRequired"] != null) {
                                showDialogMessage(
                                    it,
                                    this.dataNotificationPermission["postNotificationMessageRequired"] as String
                                ) { dialog, _ ->
                                    dialog?.dismiss()
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", it.packageName, null)
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    it.startActivity(intent)
                                }
                            } else {
                                showDialogMessage(
                                    it,
                                    it.resources.getString(R.string.text_post_notification_message_required)
                                ) { dialog, _ ->
                                    dialog?.dismiss()
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", it.packageName, null)
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    it.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDialogMessage(
        activity: Activity?,
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        activity?.let {
            AlertDialog.Builder(it, R.style.DialogTheme)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
        }
    }


}


