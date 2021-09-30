package com.hiennv.flutter_callkit_incoming

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_AVATAR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_NUMBER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_TYPE

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin : FlutterPlugin, MethodCallHandler {

    companion object {

        lateinit var context: Context
        lateinit var channel: MethodChannel
        lateinit var events: EventChannel

        val eventHandler = EventStreamHandler()


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
            return keyguardManager.isDeviceSecure && simState != TelephonyManager.SIM_STATE_ABSENT
        }

    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    //private lateinit var context: Context
    //private lateinit var channel: MethodChannel
    //private lateinit var events: EventChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming")
        channel.setMethodCallHandler(this)
        events =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming_events")
        events.setStreamHandler(eventHandler)


    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        val bundle = Bundle()
        bundle.putString(EXTRA_CALLKIT_NAME_CALLER, "Hello A")
        bundle.putString(EXTRA_CALLKIT_NUMBER, "Callkit: 0123456789")
        bundle.putString(EXTRA_CALLKIT_AVATAR, "")
        bundle.putInt(EXTRA_CALLKIT_TYPE, 1)
        when (call.method) {
            "showCallkitIncoming" -> {
                context.startActivity(CallkitIncomingActivity.getIntent(bundle))
//                if (isDeviceScreenLocked(context)) {
//                    context.startActivity(CallkitIncomingActivity.start(bundle))
//                } else {
//                    //Show notification
//                }
                //send BroadcastReceiver
                context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentIncoming(bundle))
            }
        }
//    if (call.method == "getPlatformVersion") {
//      result.success("Android ${android.os.Build.VERSION.RELEASE}")
//    } else {
//      result.notImplemented()
//    }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    class EventStreamHandler : EventChannel.StreamHandler {

        private var eventSink: EventChannel.EventSink? = null

        override fun onListen(arguments: Any?, sink: EventChannel.EventSink) {
            eventSink = sink
        }

        fun send(event: String, body: Map<String, Any?>) {
            val data = mapOf(
                "event" to event,
                "body" to body
            )
            Handler(Looper.getMainLooper()).post {
                eventSink?.success(data)
            }
        }

        override fun onCancel(p0: Any?) {
            eventSink = null
        }
    }

}
