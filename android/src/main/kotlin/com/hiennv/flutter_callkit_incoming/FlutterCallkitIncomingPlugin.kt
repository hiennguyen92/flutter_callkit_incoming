package com.hiennv.flutter_callkit_incoming

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    companion object {


        private val eventHandler = EventCallbackHandler()

        fun sendEvent(event: String, body: Map<String, Any>) {
            eventHandler.send(event, body)
        }

    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var callkitNotificationManager: CallkitNotificationManager
    private lateinit var channel: MethodChannel
    private lateinit var events: EventChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        callkitNotificationManager = CallkitNotificationManager(this.context)
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming")
        channel.setMethodCallHandler(this)
        events =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming_events")
        events.setStreamHandler(eventHandler)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            callkitNotificationManager = CallkitNotificationManager(this.context)
            when (call.method) {
                "showCallkitIncoming" -> {
                    val data = Data(call.arguments())
                    data.from = "notification"
                    callkitNotificationManager.showIncomingNotification(data.toBundle())
                    //send BroadcastReceiver
                    context.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentIncoming(
                            context,
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "startCall" -> {
                    val data = Data(call.arguments())
                    context.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentStart(
                            context,
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "endCall" -> {
                    val data = Data(call.arguments())
                    context.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentEnded(
                            context,
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "endAllCalls" -> {
                    val data = Data(call.arguments())
                    context.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentEnded(
                            context,
                            data.toBundle()
                        )
                    )
                    removeAllCalls(context)
                    result.success("OK")
                }
                "activeCalls" -> {
                    result.success(getActiveCalls(context))
                }
            }
        } catch (error: Exception) {
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
