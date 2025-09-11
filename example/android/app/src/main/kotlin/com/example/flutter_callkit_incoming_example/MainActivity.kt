package com.example.flutter_callkit_incoming_example

import android.os.Bundle
import com.hiennv.flutter_callkit_incoming.CallkitEventCallback
import com.hiennv.flutter_callkit_incoming.FlutterCallkitIncomingPlugin
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity(){

    private var callkitEventCallback = object: CallkitEventCallback{
        override fun onCallEvent(event: CallkitEventCallback.CallEvent, callData: Bundle) {
            when (event) {
                CallkitEventCallback.CallEvent.ACCEPT -> {
                    // Do something with answer
                }
                CallkitEventCallback.CallEvent.DECLINE -> {
                    // Do something with decline
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlutterCallkitIncomingPlugin.registerEventCallback(callkitEventCallback)
    }

    override fun onDestroy() {
        FlutterCallkitIncomingPlugin.unregisterEventCallback(callkitEventCallback)
        super.onDestroy()
    }


}
