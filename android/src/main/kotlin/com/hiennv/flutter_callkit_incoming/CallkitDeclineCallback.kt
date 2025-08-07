package com.hiennv.flutter_callkit_incoming

import android.os.Bundle

/**
 * Callback interface for handling call decline events natively.
 * This allows other plugins or services to receive decline events
 * even when the Flutter engine is terminated.
 */
interface CallkitDeclineCallback {
    /**
     * Called when a call is declined.
     * @param callData Bundle containing call information (id, nameCaller, etc.)
     */
    fun onCallDeclined(callData: Bundle)
}