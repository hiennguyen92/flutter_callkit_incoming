package com.hiennv.flutter_callkit_incoming

import android.os.Bundle

/**
 * Callback interface for handling call accept events natively.
 * This allows other plugins or services to receive accept events
 * even when the Flutter engine is terminated.
 */
interface CallkitAcceptCallback {
    /**
     * Called when a call is accepted.
     * @param callData Bundle containing call information (id, nameCaller, etc.)
     */
    fun onCallAccepted(callData: Bundle)
}