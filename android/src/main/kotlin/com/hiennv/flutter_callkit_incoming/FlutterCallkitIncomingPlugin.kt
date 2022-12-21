package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.annotation.Nullable

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: FlutterCallkitIncomingPlugin? = null

        public fun getInstance(): FlutterCallkitIncomingPlugin {
            if (instance == null) {
                instance = FlutterCallkitIncomingPlugin()
            }
            return instance!!
        }

        public fun hasInstance(): Boolean {
            return instance != null
        }

        private val eventHandler = EventCallbackHandler()

        fun sendEvent(event: String, body: Map<String, Any>) {
            eventHandler.send(event, body)
        }

        fun sharePluginWithRegister(
            @NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding,
            @Nullable handler: MethodCallHandler?
        ) {
            initSharedInstance(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger, handler)
        }

        private fun initSharedInstance(
            @NonNull context: Context,
            @NonNull binaryMessenger: BinaryMessenger,
            @Nullable handler: MethodCallHandler?
        ) {
            if (instance == null) {
                instance = FlutterCallkitIncomingPlugin()
            }
            instance!!.context = context
            instance!!.callkitNotificationManager = CallkitNotificationManager(context)
            instance!!.channel = MethodChannel(binaryMessenger, "flutter_callkit_incoming")
            instance!!.channel?.setMethodCallHandler(handler ?: instance!!)
            instance!!.events = EventChannel(binaryMessenger, "flutter_callkit_incoming_events")
            instance!!.events?.setStreamHandler(eventHandler)
        }
    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var activity: Activity? = null
    private var context: Context? = null
    private var callkitNotificationManager: CallkitNotificationManager? = null
    private var channel: MethodChannel? = null
    private var events: EventChannel? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        callkitNotificationManager = CallkitNotificationManager(flutterPluginBinding.applicationContext)
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming")
        channel?.setMethodCallHandler(this)
        events =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_callkit_incoming_events")
        events?.setStreamHandler(eventHandler)
        sharePluginWithRegister(flutterPluginBinding, this)
    }

    public fun showIncomingNotification(data: Data) {
        data.from = "notification"
        callkitNotificationManager?.showIncomingNotification(data.toBundle())
        //send BroadcastReceiver
        context?.sendBroadcast(
            CallkitIncomingBroadcastReceiver.getIntentIncoming(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

    public fun showMissCallNotification(data: Data) {
        callkitNotificationManager?.showIncomingNotification(data.toBundle())
    }

    public fun startCall(data: Data) {
        context?.sendBroadcast(
            CallkitIncomingBroadcastReceiver.getIntentStart(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

    public fun endCall(data: Data) {
        context?.sendBroadcast(
            CallkitIncomingBroadcastReceiver.getIntentEnded(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

    public fun endAllCalls() {
        val calls = getDataActiveCalls(context)
        calls.forEach {
            context?.sendBroadcast(
                CallkitIncomingBroadcastReceiver.getIntentEnded(
                    requireNotNull(context),
                    it.toBundle()
                )
            )
        }
        removeAllCalls(context)
    }

    public fun sendEventCustom(event: String, body: Map<String, Any>) {
        eventHandler.send(event, body)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            when (call.method) {
                "showCallkitIncoming" -> {
                    val data = Data(call.arguments() ?: HashMap<String, Any?>())
                    data.from = "notification"
                    //send BroadcastReceiver
                    context?.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentIncoming(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "showMissCallNotification" -> {
                    val data = Data(call.arguments() ?: HashMap<String, Any?>())
                    data.from = "notification"
                    callkitNotificationManager?.showMissCallNotification(data.toBundle())
                    result.success("OK")
                }
                "startCall" -> {
                    val data = Data(call.arguments() ?: HashMap<String, Any?>())
                    context?.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentStart(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "endCall" -> {
                    val data = Data(call.arguments() ?: HashMap<String, Any?>())
                    context?.sendBroadcast(
                        CallkitIncomingBroadcastReceiver.getIntentEnded(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )
                    result.success("OK")
                }
                "endAllCalls" -> {
                    val calls = getDataActiveCalls(context)
                    calls.forEach {
                        if (it.isAccepted) {
                            context?.sendBroadcast(
                                CallkitIncomingBroadcastReceiver.getIntentEnded(
                                    requireNotNull(context),
                                    it.toBundle()
                                )
                            )
                        } else {
                            context?.sendBroadcast(
                                CallkitIncomingBroadcastReceiver.getIntentDecline(
                                    requireNotNull(context),
                                    it.toBundle()
                                )
                            )
                        }
                    }
                    removeAllCalls(context)
                    result.success("OK")
                }
                "activeCalls" -> {
                    result.success(getDataActiveCallsForFlutter(context))
                }
                "getDevicePushTokenVoIP" -> {
                    result.success("")
                }
            }
        } catch (error: Exception) {
            result.error("error", error.message, "")
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        this.context = binding.activity.applicationContext
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        this.context = binding.activity.applicationContext
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
