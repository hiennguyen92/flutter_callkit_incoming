package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.silenceEvents
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodChannel

object CallkitHeadlessDart {
    private const val TAG = "CallkitHeadlessDart"
    private const val ENGINE_ID = "callkit_bg_engine"
    private const val CHANNEL = "flutter_callkit_incoming_background"

    @Volatile
    private var engine: FlutterEngine? = null

    private var channel: MethodChannel? = null

    @Volatile
    var started = false

    fun start(context: Context) {
        if (started) {
            Log.d(TAG, "Engine already running")
            return
        }
        val appCtx = context.applicationContext

        val loader = FlutterLoader()
        loader.startInitialization(appCtx)
        loader.ensureInitializationComplete(appCtx, null)

        val engine = FlutterEngine(appCtx)
        val entrypoint = DartExecutor.DartEntrypoint(
            loader.findAppBundlePath(),
            "callkitBackgroundCallback" // entrypoint Dart
        )
        engine.dartExecutor.executeDartEntrypoint(entrypoint)

        CallkitHeadlessDart.engine = engine
        FlutterEngineCache.getInstance().put(ENGINE_ID, engine)
        channel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL)
        started = true

        Log.d(TAG, "Engine started")
    }


    fun send(event: String, body: Map<String, Any?>) {
        if (!started) {
            Log.e(TAG, "Engine not started, cannot send event: $event")
            return
        }
        channel!!.invokeMethod(event, body)
    }
}
