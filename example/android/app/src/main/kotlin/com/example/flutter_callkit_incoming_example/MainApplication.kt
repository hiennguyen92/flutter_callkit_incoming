package com.example.flutter_callkit_incoming_example

import android.app.Application
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hiennv.flutter_callkit_incoming.CallkitEventCallback
import com.hiennv.flutter_callkit_incoming.FlutterCallkitIncomingPlugin
import io.flutter.embedding.android.FlutterActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
private const val TAG = "com.example.flutter_callkit_incoming_example.MainApplication"

class MainApplication : Application() {  // or FlutterApplication

    private var callkitEventCallback = object: CallkitEventCallback{
        override fun onCallEvent(event: CallkitEventCallback.CallEvent, callData: Bundle) {
            when (event) {
                CallkitEventCallback.CallEvent.ACCEPT -> {
                    // Save accepted call id to SharedPreferences
                    Log.d(TAG, "onAccept - Kotlin")

                }
                CallkitEventCallback.CallEvent.DECLINE -> {
                    Log.d(TAG, "on Decline - Kotlin")
                    val extra = callData.getSerializable("EXTRA_CALLKIT_EXTRA") as? HashMap<String, Any?>

                    Log.d(TAG, "on Decline - $extra")
                }
                else -> {
                    // Handle other cases or do nothing
                }
            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        FlutterCallkitIncomingPlugin.registerEventCallback(callkitEventCallback)

    }




}
