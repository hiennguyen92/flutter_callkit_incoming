package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import com.hiennv.flutter_callkit_incoming.Utils.Companion.reapCollection
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.ref.WeakReference


/** FlutterCallkitIncomingPlugin */
class FlutterCallkitIncomingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
    companion object {

        const val EXTRA_CALLKIT_CALL_DATA = "EXTRA_CALLKIT_CALL_DATA"

        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: FlutterCallkitIncomingPlugin

        public fun getInstance(): FlutterCallkitIncomingPlugin {
            return instance
        }

        public fun hasInstance(): Boolean {
            return ::instance.isInitialized
        }

        private val methodChannels = mutableMapOf<BinaryMessenger, MethodChannel>()
        private val eventChannels = mutableMapOf<BinaryMessenger, EventChannel>()
        private val eventHandlers = mutableListOf<WeakReference<EventCallbackHandler>>()

        fun sendEvent(event: String, body: Map<String, Any>) {
            eventHandlers.reapCollection().forEach {
                it.get()?.send(event, body)
            }
        }

        public fun sendEventCustom(event: String, body: Map<String, Any>) {
            eventHandlers.reapCollection().forEach {
                it.get()?.send(event, body)
            }
        }


        fun sharePluginWithRegister(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
            initSharedInstance(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
        }

        fun initSharedInstance(context: Context, binaryMessenger: BinaryMessenger) {
            if (!::instance.isInitialized) {
                instance = FlutterCallkitIncomingPlugin()
                instance.callkitNotificationManager = CallkitNotificationManager(context)
                instance.context = context
            }

            val channel = MethodChannel(binaryMessenger, "flutter_callkit_incoming")
            methodChannels[binaryMessenger] = channel
            channel.setMethodCallHandler(instance)

            val events = EventChannel(binaryMessenger, "flutter_callkit_incoming_events")
            eventChannels[binaryMessenger] = events
            val handler = EventCallbackHandler()
            eventHandlers.add(WeakReference(handler))
            events.setStreamHandler(handler)

        }

    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var activity: Activity? = null
    private var context: Context? = null
    private var callkitNotificationManager: CallkitNotificationManager? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        sharePluginWithRegister(flutterPluginBinding)
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

    public fun sendEventCustom(body: Map<String, Any>) {
        eventHandlers.reapCollection().forEach {
            it.get()?.send(CallkitConstants.ACTION_CALL_CUSTOM, body)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            when (call.method) {
                "showCallkitIncoming" -> {
                    val data = Data(call.arguments() ?: HashMap())
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

                "showCallkitIncomingSilently" -> {
                    val data = Data(call.arguments() ?: HashMap())
                    data.from = "notification"

                    result.success("OK")
                }

                "showMissCallNotification" -> {
                    val data = Data(call.arguments() ?: HashMap())
                    data.from = "notification"
                    callkitNotificationManager?.showMissCallNotification(data.toBundle())
                    result.success("OK")
                }

                "startCall" -> {
                    val data = Data(call.arguments() ?: HashMap())
                    context?.sendBroadcast(
                            CallkitIncomingBroadcastReceiver.getIntentStart(
                                    requireNotNull(context),
                                    data.toBundle()
                            )
                    )

                    result.success("OK")
                }

                "muteCall" -> {
                    val map = buildMap {
                        val args = call.arguments
                        if (args is Map<*, *>) {
                            putAll(args as Map<String, Any>)
                        }
                    }
                    sendEvent(CallkitConstants.ACTION_CALL_TOGGLE_MUTE, map)

                    result.success("OK")
                }

                "holdCall" -> {
                    val map = buildMap {
                        val args = call.arguments
                        if (args is Map<*, *>) {
                            putAll(args as Map<String, Any>)
                        }
                    }
                    sendEvent(CallkitConstants.ACTION_CALL_TOGGLE_HOLD, map)

                    result.success("OK")
                }

                "isMuted" -> {
                    result.success(false)
                }

                "endCall" -> {
                    val data = Data(call.arguments() ?: HashMap())
                    context?.sendBroadcast(
                            CallkitIncomingBroadcastReceiver.getIntentEnded(
                                    requireNotNull(context),
                                    data.toBundle()
                            )
                    )

                    result.success("OK")
                }

                "callConnected" -> {
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

                "silenceEvents" -> {
                    val silence = call.arguments as? Boolean ?: false
                    CallkitIncomingBroadcastReceiver.silenceEvents = silence
                    result.success("")
                }

                "requestNotificationPermission" -> {
                    val map = buildMap {
                        val args = call.arguments
                        if (args is Map<*, *>) {
                            putAll(args as Map<String, Any>)
                        }
                    }
                    callkitNotificationManager?.requestNotificationPermission(activity, map)
                }
                // EDIT - clear the incoming notification/ring (after accept/decline/timeout)
                "hideCallkitIncoming" -> {
                    val data = Data(call.arguments() ?: HashMap())
                    context?.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager?.clearIncomingNotification(data.toBundle(), false)
                }

                "endNativeSubsystemOnly" -> {

                }

                "setAudioRoute" -> {

                }
            }
        } catch (error: Exception) {
            result.error("error", error.message, "")
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannels.remove(binding.binaryMessenger)?.setMethodCallHandler(null)
        eventChannels.remove(binding.binaryMessenger)?.setStreamHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        instance.context = binding.activity.applicationContext
        instance.activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        instance.context = binding.activity.applicationContext
        instance.activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {

    }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        instance.callkitNotificationManager?.onRequestPermissionsResult(instance.activity, requestCode, grantResults)
        return true
    }


}
