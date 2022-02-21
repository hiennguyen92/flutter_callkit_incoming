package com.hiennv.flutter_callkit_incoming

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.text.TextUtils

class CallkitSoundPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var data: Bundle? = null
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val runnableTimeout = Runnable {
        val intent = CallkitIncomingBroadcastReceiver.getIntentTimeout(this@CallkitSoundPlayerService, data)
        sendBroadcast(intent)
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        this.playSound(intent)

        return START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnableTimeout)
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    private fun playSound(intent: Intent?) {
        this.data = intent?.extras
        val sound = this.data?.getString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_RINGTONE_PATH,
            ""
        )
        val duration = this.data?.getLong(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_DURATION,
            30000L
        )
        val uri = sound?.let { getRingtoneUri(it) }
        mediaPlayer = MediaPlayer.create(this@CallkitSoundPlayerService, uri).apply {
            isLooping = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attribution = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setAudioAttributes(attribution)
            } else {
                setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
            }
            start()
        }
        if (duration != null) {
            handler.postDelayed(runnableTimeout, duration)
        }
    }


    private fun getRingtoneUri(fileName: String) = try {
        if (TextUtils.isEmpty(fileName)) {
            RingtoneManager.getActualDefaultRingtoneUri(
                this@CallkitSoundPlayerService,
                RingtoneManager.TYPE_RINGTONE
            )
        }
        val resId = resources.getIdentifier(fileName, "raw", packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${packageName}/$resId")
        } else {
            if (fileName.equals("system_ringtone_default", true)) {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            } else {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            }
        }
    } catch (e: Exception) {
        if (fileName.equals("system_ringtone_default", true)) {
            RingtoneManager.getActualDefaultRingtoneUri(
                this@CallkitSoundPlayerService,
                RingtoneManager.TYPE_RINGTONE
            )
        } else {
            RingtoneManager.getActualDefaultRingtoneUri(
                this@CallkitSoundPlayerService,
                RingtoneManager.TYPE_RINGTONE
            )
        }
    }
}