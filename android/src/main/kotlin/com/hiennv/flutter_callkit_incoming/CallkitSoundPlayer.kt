package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class CallkitSoundPlayer(private val context: Context) {

    companion object {
        private var callkitSoundPlayer: CallkitSoundPlayer? = null
        fun getInstance(context: Context): CallkitSoundPlayer {
            if(callkitSoundPlayer == null){
                callkitSoundPlayer = CallkitSoundPlayer(context)
            }
            return callkitSoundPlayer!!
        }

    }



    private var mediaPlayer: MediaPlayer? = null
    private var duration: Long = 0L

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun play(data: Bundle?) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stop()
        }
        playSound(data)
        Handler(Looper.getMainLooper()).postDelayed({
            if (mediaPlayer?.isPlaying == true) {
                val intent = CallkitIncomingBroadcastReceiver.getIntentTimeout(context, data)
                context.sendBroadcast(intent)
                stop()
            }
        }, duration)
    }

    fun stop() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stopMusic()
        }
    }

    private fun stopMusic() {
        mediaPlayer?.run {
            stop()
            seekTo(0)
        }
    }

    private fun playSound(data: Bundle?) {
        val uri = getRingtoneUri("ringtone_default")
        mediaPlayer = MediaPlayer.create(context, uri).apply {
            isLooping = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attribution = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setAudioAttributes(attribution)
            }
            start()
        }
    }


    private fun getRingtoneUri(fileName: String) = try {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            Uri.parse("android.resource://${context.packageName}/${R.raw.ringtone_default}")
        }
    } catch (e: Exception) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.ringtone_default}")
    }


}