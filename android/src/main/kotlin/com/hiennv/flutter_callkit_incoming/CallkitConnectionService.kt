package com.hiennv.flutter_callkit_incoming

import android.content.Intent
import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class CallkitConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: android.telecom.PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d("CallkitIncoming", "onCreateIncomingConnection called")

        val connection = CallkitConnection(applicationContext)
        connection.setInitializing()
        connection.setActive()
        return connection
    }
}

