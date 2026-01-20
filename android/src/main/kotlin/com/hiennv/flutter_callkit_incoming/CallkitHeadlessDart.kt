package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.util.Log
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

object CallkitHeadlessDart {
    private const val TAG = "CallkitHeadlessDart"
    private const val CHANNEL = "flutter_callkit_incoming_background"

    @Volatile
    private var engine: FlutterEngine? = null

    private var channel: MethodChannel? = null

    @Volatile
    var started = false

    fun start(context: Context, pluginCallbackHandle: Long) {
        if (started) {
            Log.d(TAG, "Engine already running")
            return
        }

        val appCtx = context.applicationContext

        val loader: FlutterLoader = FlutterInjector.instance().flutterLoader()
        loader.startInitialization(appCtx)
        loader.ensureInitializationComplete(appCtx, null)

        engine = FlutterEngine(appCtx)

        val callbackInfo =
            FlutterCallbackInformation.lookupCallbackInformation(pluginCallbackHandle)

        val args = DartExecutor.DartCallback(
            appCtx.assets,
            loader.findAppBundlePath(),
            callbackInfo
        )

        engine!!.dartExecutor.executeDartCallback(args)
        channel = MethodChannel(engine!!.dartExecutor.binaryMessenger, CHANNEL)
        started = true

        Log.d(TAG, "Background FlutterEngine started")
    }

    fun send(event: String, body: Map<String, Any?>) {
        if (!started) {
            Log.e(TAG, "Engine not started, cannot send event: $event")
            return
        }
        channel!!.invokeMethod(event, body)
    }
}
