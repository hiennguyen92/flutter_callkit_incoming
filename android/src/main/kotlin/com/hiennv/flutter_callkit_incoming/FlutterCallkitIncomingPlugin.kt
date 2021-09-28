package com.hiennv.flutter_callkit_incoming

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.NonNull
import com.hiennv.flutter_callkit_incoming.CallkitIncomingActivity.Companion.EXTRA_CALLKIT_AVATAR
import com.hiennv.flutter_callkit_incoming.CallkitIncomingActivity.Companion.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingActivity.Companion.EXTRA_CALLKIT_NUMBER

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin: FlutterPlugin, MethodCallHandler {

  companion object {
    var activity: Activity? = null
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
        val lockPatternEnable: Int = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED)
        lockPatternEnable == 1
      } catch (e: Settings.SettingNotFoundException) {
        false
      }
    }

    private fun isPassOrPinSet(context: Context): Boolean {
      val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
      return keyguardManager.isKeyguardSecure
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun isDeviceLocked(context: Context): Boolean {
      val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      val simState = telMgr.simState
      val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
      return keyguardManager.isDeviceSecure && simState != TelephonyManager.SIM_STATE_ABSENT
    }

  }

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var events: EventChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming")
    channel.setMethodCallHandler(this)
    events = EventChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming_events")
    events.setStreamHandler(eventHandler)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

    val bundle = Bundle()
    bundle.putString(EXTRA_CALLKIT_NAME_CALLER, "Hello")
    bundle.putString(EXTRA_CALLKIT_NUMBER, "Hihi")
    bundle.putString(EXTRA_CALLKIT_AVATAR, "")
    Log.d("XXX", call.method)
    when(call.method) {
      "showCallkitIncoming" -> {
          if(activity != null){
            if(isDeviceScreenLocked(activity!!)){
              activity?.startActivity(CallkitIncomingActivity.start(bundle))
            }else {
              //Show notification
            }
            //send broadcast receiver
            //activity.sendBroadcast()
          }
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

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }



  class EventStreamHandler: EventChannel.StreamHandler {

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
