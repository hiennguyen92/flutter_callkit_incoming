package com.hiennv.flutter_callkit_incoming

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_ACTION_COLOR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_AVATAR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_BACKGROUND
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_BACKGROUND_COLOR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_DURATION
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_EXTRA
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_ID
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NUMBER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_SOUND
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_TYPE

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    companion object {

        private const val EXTRA_INTERNAL_FROM = "EXTRA_INTERNAL_FROM"


        private val eventHandler = EventCallbackHandler()

        fun sendEvent(event: String, body: Map<String, Any>) {
            eventHandler.send(event, body)
        }


        fun isDeviceScreenLocked(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isDeviceLocked(context)
            } else {
                isPatternSet(context) || isPassOrPinSet(context)
            }
        }

        private fun isPatternSet(context: Context): Boolean {
            val cr: ContentResolver = context.contentResolver
            return try {
                val lockPatternEnable: Int =
                    Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED)
                lockPatternEnable == 1
            } catch (e: Settings.SettingNotFoundException) {
                false
            }
        }

        private fun isPassOrPinSet(context: Context): Boolean {
            val keyguardManager =
                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardSecure
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun isDeviceLocked(context: Context): Boolean {
            val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simState = telMgr.simState
            val keyguardManager =
                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isDeviceLocked && keyguardManager.isDeviceSecure && simState != TelephonyManager.SIM_STATE_ABSENT
        }

    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var activity: Activity
    private lateinit var context: Context

    private lateinit var channel: MethodChannel
    private lateinit var events: EventChannel
    private lateinit var callkitNotificationManager: CallkitNotificationManager

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        this.callkitNotificationManager = CallkitNotificationManager(this.context)
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming")
        channel.setMethodCallHandler(this)
        events =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming_events")
        events.setStreamHandler(eventHandler)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        try {
            val bundle = Bundle()
            call.argument<String>("id")?.let { bundle.putString(EXTRA_CALLKIT_ID, it) }
            call.argument<String>("nameCaller")
                ?.let { bundle.putString(EXTRA_CALLKIT_NAME_CALLER, it) }
            call.argument<String>("number")?.let { bundle.putString(EXTRA_CALLKIT_NUMBER, it) }
            call.argument<String>("avatar")?.let { bundle.putString(EXTRA_CALLKIT_AVATAR, it) }
            call.argument<Int>("type")?.let { bundle.putInt(EXTRA_CALLKIT_TYPE, it) }
            call.argument<Int>("duration")
                ?.let { bundle.putLong(EXTRA_CALLKIT_DURATION, it.toLong()) }
            call.argument<Map<String, Any>>("extra")
                ?.let { bundle.putString(EXTRA_CALLKIT_EXTRA, JSONObject(it).toString()) }
            call.argument<Map<String, Any>>("android")?.let {
                bundle.putBoolean(
                    EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION,
                    it["isCustomNotification"] as Boolean
                )
                bundle.putString(EXTRA_CALLKIT_SOUND, it["sound"].toString())
                bundle.putString(EXTRA_CALLKIT_BACKGROUND_COLOR, it["backgroundColor"].toString())
                bundle.putString(EXTRA_CALLKIT_BACKGROUND, it["background"].toString())
                bundle.putString(EXTRA_CALLKIT_ACTION_COLOR, it["actionColor"].toString())
            }

            when (call.method) {
                "showCallkitIncoming" -> {
                    if (isDeviceScreenLocked(context)) {
                        bundle.putString(EXTRA_INTERNAL_FROM, "activity")
                        context.startActivity(CallkitIncomingActivity.getIntent(bundle))
                    } else {
                        bundle.putString(EXTRA_INTERNAL_FROM, "notification")
                        callkitNotificationManager.showIncomingNotification(bundle)
                    }
                    //send BroadcastReceiver
                    context.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentIncoming(
                            context,
                            bundle
                        )
                    )
                }
            }
            result.success("OK")
        }catch (error: Exception){
            result.error("error", error.message, "")
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onDetachedFromActivity() {}


    class EventCallbackHandler : EventChannel.StreamHandler {

        private var eventSink: EventChannel.EventSink? = null

        override fun onListen(arguments: Any?, sink: EventChannel.EventSink) {
            eventSink = sink
        }

        fun send(event: String, body: Map<String, Any>) {
            val data = mapOf(
                "event" to event,
                "body" to body
            )
            Handler(Looper.getMainLooper()).post {
                eventSink?.success(data)
            }
        }

        override fun onCancel(arguments: Any?) {
            eventSink = null
        }
    }


}
