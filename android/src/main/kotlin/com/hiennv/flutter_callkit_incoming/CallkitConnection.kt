package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.ConcurrentHashMap

/**
 * Self-managed Telecom [Connection] implementation for flutter_callkit_incoming.
 *
 * Hosts the call in the Android Telecom framework with `PROPERTY_SELF_MANAGED` so
 * the OS treats it as a first-party phone call — granting it keyguard-bypass /
 * full-screen-intent priority on strict OEMs (Samsung Knox, Xiaomi, etc.) that
 * otherwise ignore [android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED]
 * on ordinary Activities.
 *
 * Lifecycle:
 *  1. [CallkitConnectionService.onCreateIncomingConnection] returns a
 *     ringing Connection instance.
 *  2. User taps Accept in our notification → plugin's BroadcastReceiver maps the
 *     broadcast to the matching connection via [find] and calls [setActive].
 *  3. User taps Decline / End → [setDisconnected] + [destroy] → unregister.
 *
 * Connection lookup uses a process-wide [ConcurrentHashMap] keyed by the call id
 * (the plugin's existing `Data.id` field). The OS holds the Connection object in
 * the Telecom framework; we keep a parallel reference so our BroadcastReceiver
 * can drive state transitions.
 */
@RequiresApi(Build.VERSION_CODES.M)
class CallkitConnection(
    private val appContext: Context,
    val callId: String,
    val bundle: Bundle,
) : Connection() {

    companion object {
        private const val TAG = "CallkitConnection"
        private const val RESUME_POLL_GRACE_MS = 2000L
        private const val RESUME_POLL_INTERVAL_MS = 1000L

        /** Bundle key — pass the full call Data bundle through Telecom extras. */
        const val EXTRA_CALL_BUNDLE = "com.hiennv.flutter_callkit_incoming.CALL_BUNDLE"

        /**
         * Routes this plugin exposes to the app, as `CallAudioState.ROUTE_*`
         * values. Anything else is rejected.
         */
        private val KNOWN_ROUTES = setOf(
            CallAudioState.ROUTE_EARPIECE,
            CallAudioState.ROUTE_SPEAKER,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_WIRED_HEADSET,
        )

        private val activeConnections = ConcurrentHashMap<String, CallkitConnection>()

        fun find(callId: String): CallkitConnection? = activeConnections[callId]

        /**
         * Connection the app should be routing audio on: the one registered
         * under [callId] when it is known, otherwise the only active
         * self-managed connection (the plugin hosts a single call in practice).
         * Null when there is nothing to route.
         */
        fun resolveActive(callId: String?): CallkitConnection? {
            if (callId != null) {
                activeConnections[callId]?.let { return it }
                Log.d(TAG, "resolveActive: no connection for id=$callId, falling back")
            }
            return activeConnections.values.singleOrNull()
        }

        fun register(callId: String, conn: CallkitConnection) {
            activeConnections[callId] = conn
        }

        fun unregister(callId: String) {
            activeConnections.remove(callId)
        }

        /** For testing / cleanup — release all refs (Connection objects already destroyed by OS). */
        fun clearAll() {
            activeConnections.clear()
        }

        fun activeCount(): Int = activeConnections.size
    }

    init {
        connectionProperties = PROPERTY_SELF_MANAGED
        audioModeIsVoip = true
        // CAPABILITY_HOLD ("can be held right now") and CAPABILITY_SUPPORT_HOLD
        // ("hold feature exists") are distinct flags and Telecom requires BOTH:
        // when the user answers another call, CallsManager only holds the active
        // call if it has CAPABILITY_HOLD — otherwise onHold() is never invoked
        // and the call is disconnected or left running unheld.
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD
        register(callId, this)
        Log.d(TAG, "Connection created id=$callId active=${activeCount()}")
    }

    // -------------------------------------------------------------------------
    // Telecom → app lifecycle callbacks
    //
    // These fire when the user interacts with the OS-level call UI (system
    // dialer, car Bluetooth, watch, etc.). In our app-driven model we still
    // handle the primary Accept/Decline via our own notification buttons, but
    // the OS can also trigger these — we must honor both paths.
    // -------------------------------------------------------------------------

    override fun onAnswer() {
        super.onAnswer()
        Log.d(TAG, "onAnswer id=$callId")
        setActive()
    }

    override fun onReject() {
        super.onReject()
        Log.d(TAG, "onReject id=$callId")
        finishWithCause(DisconnectCause.REJECTED)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        Log.d(TAG, "onDisconnect id=$callId")
        finishWithCause(DisconnectCause.LOCAL)
    }

    override fun onAbort() {
        super.onAbort()
        Log.d(TAG, "onAbort id=$callId")
        finishWithCause(DisconnectCause.UNKNOWN)
    }

    // Telecom holds this self-managed connection when another call takes over
    // the audio (e.g. the user dials or answers a cellular call). Forward the
    // transition to Dart so the app can pause its media — mirrors what iOS
    // does via CXSetHeldCallAction.
    //
    // Telecom does NOT unhold self-managed calls when the other call ends —
    // the self-managed contract is that the app resumes its own call. So on
    // hold we start a watcher that polls for the managed call's presence and
    // self-resumes once it is gone (see resumeWhenManagedCallEnds).
    override fun onHold() {
        super.onHold()
        Log.d(TAG, "onHold id=$callId")
        setOnHold()
        FlutterCallkitIncomingPlugin.sendEvent(
            CallkitConstants.ACTION_CALL_TOGGLE_HOLD,
            mapOf("id" to callId, "isOnHold" to true),
        )
        startResumeWatcher()
    }

    /**
     * While a Telecom call is ringing, volume-key presses never reach
     * AudioService — PhoneWindowManager intercepts them and calls
     * [android.telecom.TelecomManager.silenceRinger], which lands here.
     * Self-managed connections do their own ringing, so stop our ringtone.
     */
    override fun onSilence() {
        super.onSilence()
        Log.d(TAG, "onSilence id=$callId")
        FlutterCallkitIncomingPlugin.getInstance()?.getCallkitSoundPlayerManager()?.stop()
    }

    override fun onUnhold() {
        super.onUnhold()
        Log.d(TAG, "onUnhold id=$callId")
        resume()
    }

    private fun resume() {
        stopResumeWatcher()
        setActive()
        FlutterCallkitIncomingPlugin.sendEvent(
            CallkitConstants.ACTION_CALL_TOGGLE_HOLD,
            mapOf("id" to callId, "isOnHold" to false),
        )
    }

    // -------------------------------------------------------------------------
    // Audio routing (self-managed call contract)
    //
    // For a self-managed call Telecom owns the platform audio mode, so the app
    // can no longer route audio with AudioManager: measured on Android 14,
    // setCommunicationDevice(SPEAKER) is accepted but the active communication
    // device stays on earpiece, and the legacy setSpeakerphoneOn(true) does not
    // even flip its own flag. Android's contract for a self-managed call is
    // that the app states its route on its own Connection — this section
    // implements exactly that via Connection.setAudioRoute(int) and the
    // android.telecom.CallAudioState.ROUTE_* constants.
    // -------------------------------------------------------------------------

    /** Route the app asked for, or null when it never asked (system default). */
    private var requestedRoute: Int? = null

    /**
     * Apply a `CallAudioState.ROUTE_*` route to this connection and remember it
     * so it can be restored on the next audio state change.
     *
     * @return true when the route was handed to Telecom, false otherwise.
     */
    fun requestAudioRoute(route: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Connection.setAudioRoute() and CallAudioState are API 23+, while
            // this module's minSdk is 16 — guard instead of crashing.
            Log.w(TAG, "requestAudioRoute unsupported below API 23 id=$callId")
            return false
        }
        if (route !in KNOWN_ROUTES) {
            Log.w(TAG, "requestAudioRoute unknown route=$route id=$callId")
            return false
        }
        requestedRoute = route
        val supported = currentSupportedRouteMask()
        val resolved = resolveRoute(route, supported)
        Log.d(
            TAG,
            "requestAudioRoute id=$callId requested=$route supported=$supported resolved=$resolved",
        )
        return applyAudioRoute(resolved)
    }

    /**
     * Telecom notifies route changes here — a Bluetooth headset connecting or
     * disconnecting, another call taking the audio, the system re-picking a
     * device. Re-apply the route the app asked for, mapped onto the routes
     * Telecom reports as available right now; without this override a single
     * system event silently drops the app's choice.
     */
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        val state = audioState ?: return
        // The app never asked for a route: keep the system default behaviour.
        val requested = requestedRoute ?: return
        val resolved = resolveRoute(requested, state.supportedRouteMask)
        // Already there — also stops a setAudioRoute / onCallAudioStateChanged loop.
        if (state.route == resolved) return
        Log.d(
            TAG,
            "onCallAudioStateChanged id=$callId requested=$requested current=${state.route} " +
                "supported=${state.supportedRouteMask} resolved=$resolved",
        )
        applyAudioRoute(resolved)
    }

    private fun applyAudioRoute(route: Int): Boolean {
        return try {
            setAudioRoute(route)
            true
        } catch (e: Exception) {
            // Stale or destroyed connection: report the failure instead of
            // throwing at the MethodChannel caller.
            Log.w(TAG, "setAudioRoute($route) failed id=$callId: ${e.message}")
            false
        }
    }

    /**
     * Map the route the app asked for onto the routes Telecom reports as
     * available, so a request is not dropped just because a headset is, or is
     * not, connected at that moment.
     *
     * Asking for earpiece means "speaker off", not "earpiece": when a headset
     * is connected the call must go back to it — this is what the platform
     * itself does when the user turns the speaker off on the system call UI.
     */
    private fun resolveRoute(requested: Int, supportedRouteMask: Int): Int {
        if (supportedRouteMask == 0) return requested // no route hint from Telecom
        if (requested == CallAudioState.ROUTE_EARPIECE) {
            return preferredHeadsetRoute(supportedRouteMask)
        }
        if (isRouteSupported(requested, supportedRouteMask)) return requested
        return preferredHeadsetRoute(supportedRouteMask)
    }

    /** Bluetooth, then wired headset, then earpiece — the non-speaker preference order. */
    private fun preferredHeadsetRoute(supportedRouteMask: Int): Int = when {
        isRouteSupported(CallAudioState.ROUTE_BLUETOOTH, supportedRouteMask) ->
            CallAudioState.ROUTE_BLUETOOTH
        isRouteSupported(CallAudioState.ROUTE_WIRED_HEADSET, supportedRouteMask) ->
            CallAudioState.ROUTE_WIRED_HEADSET
        isRouteSupported(CallAudioState.ROUTE_EARPIECE, supportedRouteMask) ->
            CallAudioState.ROUTE_EARPIECE
        else -> CallAudioState.ROUTE_SPEAKER
    }

    private fun isRouteSupported(route: Int, supportedRouteMask: Int): Boolean =
        supportedRouteMask and route == route

    private fun currentSupportedRouteMask(): Int = try {
        callAudioState?.supportedRouteMask ?: 0
    } catch (e: Exception) {
        0
    }

    // -------------------------------------------------------------------------
    // Self-resume watcher
    // -------------------------------------------------------------------------

    private val watcherHandler = Handler(Looper.getMainLooper())
    private var resumeWatcher: Runnable? = null

    private fun startResumeWatcher() {
        stopResumeWatcher()
        val watcher = object : Runnable {
            override fun run() {
                if (state != STATE_HOLDING) {
                    resumeWatcher = null
                    return
                }
                if (isManagedCallActive()) {
                    watcherHandler.postDelayed(this, RESUME_POLL_INTERVAL_MS)
                    return
                }
                Log.d(TAG, "managed call gone — self-resuming id=$callId")
                resumeWatcher = null
                resume()
            }
        }
        resumeWatcher = watcher
        // Grace delay before the first check: at the instant Telecom holds us
        // the native call may still be setting up, so an immediate poll could
        // read "no managed call" and resume prematurely.
        watcherHandler.postDelayed(watcher, RESUME_POLL_GRACE_MS)
    }

    private fun stopResumeWatcher() {
        resumeWatcher?.let { watcherHandler.removeCallbacks(it) }
        resumeWatcher = null
    }

    /**
     * Is a managed (carrier/telephony) call ringing, dialing or active?
     *
     * Primary signal: [TelecomManager.isInManagedCall] — precise, counts only
     * managed calls (never our own self-managed one), but needs the
     * READ_PHONE_STATE runtime permission. Fallback when not granted: the
     * audio mode, which telephony sets to MODE_IN_CALL (active) or
     * MODE_RINGTONE (ringing) for the lifetime of a native call.
     */
    private fun isManagedCallActive(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telecom =
                appContext.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (telecom != null) {
                try {
                    return telecom.isInManagedCall
                } catch (e: SecurityException) {
                    Log.d(TAG, "isInManagedCall needs READ_PHONE_STATE — audio-mode fallback")
                }
            }
        }
        val audio = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false
        return audio.mode == AudioManager.MODE_IN_CALL ||
            audio.mode == AudioManager.MODE_RINGTONE
    }

    // -------------------------------------------------------------------------
    // App → Telecom driving helpers (invoked by the plugin's BroadcastReceiver)
    // -------------------------------------------------------------------------

    /** Mark the call as answered — user accepted via app notification. */
    fun markAccepted() {
        Log.d(TAG, "markAccepted id=$callId")
        setActive()
    }

    /**
     * Mark the call as declined/ended — user declined via app notification.
     *
     * Cold-launch DECLINE recovery is fired
     * from inside the self-managed [Connection] context (and *before*
     * [setDisconnected] runs) so the call is still in the RINGING state when
     * [Context.startActivity] is invoked. The hope: Android 14+ BAL grants a
     * PHONE_CALL exemption for active self-managed Telecom calls. Previous
     * placement inside the BroadcastReceiver hit BAL_BLOCK with
     * `callingUidProcState: BOUND_FOREGROUND_SERVICE; callingUidHasVisibleActivity: false`
     * (Galaxy logcat 2026-05-05 03:49:04.039).
     *
     * If this position still BAL_BLOCK's, the next escalation is to promote
     * `CallkitConnectionService` to a `foregroundServiceType="phoneCall"`
     * FGS for the lifetime of the connection.
     */
    fun markDeclined(context: Context) {
        Log.d(TAG, "markDeclined id=$callId")
        // Do not launch the app on decline. End the self-managed Telecom call
        // immediately so declining from the notification/lock screen leaves a
        // backgrounded or terminated app closed (3.0.0 behavior).
        finishWithCause(DisconnectCause.REJECTED)
    }

    /**
     * Persist declined nonce and start MainActivity. Idempotent — safe even
     * if the BroadcastReceiver fallback also fires (single-task launch flag,
     * one-shot consumePending).
     */
    private fun triggerDeclineRecovery(context: Context) {
        try {
            val prefs = context.getSharedPreferences(
                "flutter_callkit_incoming_decline",
                Context.MODE_PRIVATE,
            )
            prefs.edit()
                .putString("pending_nonce", callId)
                .putLong("pending_at", System.currentTimeMillis())
                .apply()
            Log.d(TAG, "[DIAG-DECLINE-CS] prefs written nonce=$callId")
            val launchIntent = AppUtils.getAppIntent(context, null, null)
            launchIntent?.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                Log.d(TAG, "[DIAG-DECLINE-CS] startActivity dispatched (CS ctx, RINGING)")
            } else {
                Log.w(TAG, "[DIAG-DECLINE-CS] launchIntent null — recovery skipped")
            }
        } catch (e: Exception) {
            Log.w(TAG, "[DIAG-DECLINE-CS] recovery failed: ${e.message}")
        }
    }

    /** Mark the call as terminated — call ended (either side hung up). */
    fun markEnded() {
        Log.d(TAG, "markEnded id=$callId")
        finishWithCause(DisconnectCause.LOCAL)
    }

    /** Mark the call as missed — timeout without answer. */
    fun markMissed() {
        Log.d(TAG, "markMissed id=$callId")
        finishWithCause(DisconnectCause.MISSED)
    }

    private fun finishWithCause(cause: Int) {
        stopResumeWatcher()
        try {
            setDisconnected(DisconnectCause(cause))
        } catch (e: Exception) {
            Log.w(TAG, "setDisconnected failed: ${e.message}")
        }
        unregister(callId)
        try {
            destroy()
        } catch (e: Exception) {
            Log.w(TAG, "destroy failed: ${e.message}")
        }
    }
}
