package com.hiennv.flutter_callkit_incoming

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Self-managed Telecom [ConnectionService] — hosts in-app calls in the Android
 * Telecom framework.
 *
 * Registered via [InAppCallManager.registerPhoneAccount] with
 * [android.telecom.PhoneAccount.CAPABILITY_SELF_MANAGED]. Activated when the
 * plugin's incoming-call flow calls `telecomManager.addNewIncomingCall(...)` —
 * the OS then invokes [onCreateIncomingConnection] which returns a ringing
 * [CallkitConnection]. The Telecom layer grants the resulting call OS-level
 * privileges equivalent to a dialer app:
 *  - Keyguard bypass on the linked call Activity
 *  - Ringtone + vibration via Audio focus
 *  - Audio routing (Bluetooth / earpiece / speaker) via AudioManager call mode
 *  - Compatibility with strict OEMs (Samsung Knox, Xiaomi, Oppo) that block
 *    non-Telecom `showWhenLocked` Activities.
 *
 * Prior versions of this file were a stub with no overrides — the PhoneAccount
 * was registered but never used. Filling in the overrides + wiring
 * `addNewIncomingCall` in the BroadcastReceiver converts the plugin from an
 * "app that looks like a call" into a real first-party call.
 */
@RequiresApi(Build.VERSION_CODES.M)
class CallkitConnectionService : ConnectionService() {

    companion object {
        private const val TAG = "CallkitConnectionSvc"

        /**
         * The calls handed to Telecom each way, oldest first, until their
         * connection is created or refused. A refused request no longer
         * carries the call's extras (seen on Android 10), so this is how the
         * refusal finds its call: Telecom answers requests in order, so it is
         * the oldest one still waiting.
         */
        val pendingIncoming = PendingCalls()

        val pendingOutgoing = PendingCalls()
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ): Connection {
        val callBundle = extractCallBundle(request?.extras) ?: run {
            Log.w(TAG, "onCreateIncomingConnection: missing call bundle")
            return failed("Missing call data")
        }

        val data = Data.fromBundle(callBundle)
        val callId = data.id
        if (callId.isEmpty()) {
            Log.w(TAG, "onCreateIncomingConnection: empty call id")
            return failed("Empty call id")
        }

        Log.d(TAG, "onCreateIncomingConnection id=$callId caller=${data.nameCaller}")
        pendingIncoming.remove(callId)

        val connection = CallkitConnection(applicationContext, callId, callBundle).apply {
            if (data.nameCaller.isNotEmpty()) {
                setCallerDisplayName(data.nameCaller, TelecomManager.PRESENTATION_ALLOWED)
            }
            val handle = data.handle.ifEmpty { callId }
            setAddress(
                Uri.fromParts("tel", handle, null),
                TelecomManager.PRESENTATION_ALLOWED,
            )
            setRinging()
        }
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.w(TAG, "onCreateIncomingConnectionFailed")
        refused(request, pendingIncoming)
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ): Connection {
        val callBundle = extractCallBundle(request?.extras) ?: run {
            Log.w(TAG, "onCreateOutgoingConnection: missing call bundle")
            return failed("Missing call data")
        }

        val data = Data.fromBundle(callBundle)
        val callId = data.id
        if (callId.isEmpty()) {
            Log.w(TAG, "onCreateOutgoingConnection: empty call id")
            return failed("Empty call id")
        }

        Log.d(TAG, "onCreateOutgoingConnection id=$callId callee=${data.nameCaller}")
        pendingOutgoing.remove(callId)

        val connection = CallkitConnection(applicationContext, callId, callBundle).apply {
            if (data.nameCaller.isNotEmpty()) {
                setCallerDisplayName(data.nameCaller, TelecomManager.PRESENTATION_ALLOWED)
            }
            val handle = data.handle.ifEmpty { callId }
            setAddress(
                Uri.fromParts("tel", handle, null),
                TelecomManager.PRESENTATION_ALLOWED,
            )
            setDialing()
        }
        return connection
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.w(TAG, "onCreateOutgoingConnectionFailed")
        refused(request, pendingOutgoing)
    }

    private fun extractCallBundle(extras: Bundle?): Bundle? {
        if (extras == null) return null
        // Primary path — our helper nests the full plugin Data under the
        // dedicated key. Telecom also exposes the caller-supplied extras under
        // EXTRA_INCOMING_CALL_EXTRAS for the incoming path.
        extras.getBundle(CallkitConnection.EXTRA_CALL_BUNDLE)?.let { return it }
        extras.getBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS)?.let { inner ->
            inner.getBundle(CallkitConnection.EXTRA_CALL_BUNDLE)?.let { return it }
            if (inner.containsKey("id")) return inner
        }
        return if (extras.containsKey("id")) extras else null
    }

    /**
     * Telecom refused the call (e.g. another app's call is ringing, or can't be
     * held for an outgoing one). The plugin's own notification would ring on
     * and the app would carry on with the call: end the UI and tell the app.
     */
    private fun refused(request: ConnectionRequest?, pending: PendingCalls) {
        val named = extractCallBundle(request?.extras)
        val data = named?.also { pending.remove(Data.fromBundle(it).id) } ?: pending.takeOldest() ?: run {
            Log.w(TAG, "refused: no call data")
            return
        }
        applicationContext.sendBroadcast(
            CallkitIncomingBroadcastReceiver.getIntentRefused(applicationContext, data),
        )
    }

    private fun failed(reason: String): Connection {
        return Connection.createFailedConnection(
            DisconnectCause(DisconnectCause.ERROR, reason),
        )
    }
}

/** Calls waiting for Telecom's answer, oldest first, by id. */
class PendingCalls {
    private val calls = LinkedHashMap<String, Bundle>()

    @Synchronized
    fun add(id: String, data: Bundle) {
        calls.remove(id)
        calls[id] = data
        // Never more than a few in flight; never grows without end.
        while (calls.size > 8) calls.remove(calls.keys.first())
    }

    @Synchronized
    fun remove(id: String) {
        calls.remove(id)
    }

    @Synchronized
    fun takeOldest(): Bundle? = calls.keys.firstOrNull()?.let { calls.remove(it) }
}
