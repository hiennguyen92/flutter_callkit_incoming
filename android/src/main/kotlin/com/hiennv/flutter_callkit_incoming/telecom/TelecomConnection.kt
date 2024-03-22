package com.hiennv.flutter_callkit_incoming.telecom


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.os.bundleOf
import com.hiennv.flutter_callkit_incoming.CallkitConstants.ACTION_CALL_ACCEPT
import com.hiennv.flutter_callkit_incoming.CallkitConstants.ACTION_CALL_AUDIO_STATE_CHANGE
import com.hiennv.flutter_callkit_incoming.CallkitConstants.ACTION_CALL_ENDED
import com.hiennv.flutter_callkit_incoming.CallkitConstants.ACTION_CALL_HELD
import com.hiennv.flutter_callkit_incoming.CallkitConstants.ACTION_CALL_UNHELD
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_HANDLE
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_ID
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_NAME_CALLER
import com.hiennv.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver
import com.hiennv.flutter_callkit_incoming.telecom.TelecomUtilities.Companion.androidToJsRouteMap
import java.io.PrintWriter
import java.io.StringWriter


// REF https://developer.android.com/reference/android/telecom/Connection
// the handle hashmap has the uuid under `EXTRA_CALLKIT_ID`
class TelecomConnection internal constructor(private val context: Context, private val handle: HashMap<String, String>) : Connection() {
	init {
		// previously, the caps and voip mode were set in two different places for incoming/outgoing connections
		// moreover, the voip mode was set in the "onAnswer" method for incoming calls which caused the connection to be incorrectly set up if it was answered from the app UI
		connectionCapabilities = PROPERTY_SELF_MANAGED or CAPABILITY_MUTE or CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD
		audioModeIsVoip = true

		val number = handle[EXTRA_CALLKIT_HANDLE]
		val name = handle[EXTRA_CALLKIT_NAME_CALLER]

		if (number != null) setAddress(Uri.parse(number), TelecomManager.PRESENTATION_ALLOWED)
		if (name != null && name != "") setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
	}

	// called when answered from bt device/car
	override fun onAnswer() {
		super.onAnswer()
		TelecomUtilities.logToFile("[TelecomConnection] onAnswer called")

		val uuid = handle[EXTRA_CALLKIT_ID] ?: ""
		val data: Map<String, Any> = object : HashMap<String, Any>() {
			init {
				put("event", ACTION_CALL_ACCEPT)
				put(EXTRA_CALLKIT_ID, uuid)
			}
		}
		TelecomUtilities.logToFile("[TelecomConnection] On Answer data: $data")

		context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentAccept(context, bundleOf(*data.toList().toTypedArray())))

		TelecomUtilities.logToFile("[TelecomConnection] onAnswer executed")
		setActive()
	}

	override fun onAbort() {
		super.onAbort()
		TelecomUtilities.logToFile("[TelecomConnection] onAbort")
		setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
		endCall()
		TelecomUtilities.logToFile("[TelecomConnection] onAbort executed")
	}

	override fun onReject() {
		super.onReject()
		setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
		TelecomUtilities.logToFile("[TelecomConnection] onReject")
		endCall()
		TelecomUtilities.logToFile("[TelecomConnection] onReject executed")
	}
	override fun onDisconnect() {
		super.onDisconnect()
		TelecomUtilities.logToFile("[TelecomConnection] onDisconnect")
		setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
		endCall()
	}
	public fun endCall() {
		TelecomUtilities.logToFile("[TelecomConnection] Ending call - disconnectCause: $disconnectCause")
		val uuid = handle[EXTRA_CALLKIT_ID] ?: ""
		val data: Map<String, Any> = object : HashMap<String, Any>() {
			init {
				put("event", ACTION_CALL_ENDED)
				put("disconnectCause", disconnectCause.toString())
				put(EXTRA_CALLKIT_ID, uuid)
			}
		}
		context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentEnded(context, bundleOf(*data.toList().toTypedArray())))
		try {
			TelecomConnectionService.deinitConnection(handle[EXTRA_CALLKIT_ID] ?: "")

		} catch (exception: Throwable) {
			Log.e(TAG, "Handle map error", exception)

			val stackTrace = StringWriter()
			exception.printStackTrace(PrintWriter(stackTrace))

			TelecomUtilities.logToFile("[TelecomUtilities] EXCEPTION reportIncomingCall -- $exception -- message: ${exception.message} -- stack: $stackTrace")
		}

		destroy()
	}

	override fun onHold() {
		TelecomUtilities.logToFile("[TelecomConnection] On hold")
		super.onHold()
		//GF not needed

		val uuid = handle[EXTRA_CALLKIT_ID] ?: ""
		val data: Map<String, Any> = object : HashMap<String, Any>() {
			init {
				put("event", ACTION_CALL_HELD)
				put(EXTRA_CALLKIT_ID, uuid)
				put("args", 1)
			}
		}
		context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentHeldByCell(context, bundleOf(*data.toList().toTypedArray())))

		setOnHold();

		//context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntent(context, ACTION_CALL_HELD, bundleOf(*data.toList().toTypedArray())))



	}
	override fun onUnhold() {
		super.onUnhold()
		val uuid = handle[EXTRA_CALLKIT_ID] ?: ""
		val data: Map<String, Any> = object : HashMap<String, Any>() {
			init {
				put("event", ACTION_CALL_UNHELD)
				put(EXTRA_CALLKIT_ID, uuid)
				put("args", 0)
			}
		}
		//context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentCallback(context, bundleOf(*data.toList().toTypedArray())))

		context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntentUnHeldByCell(context, bundleOf(*data.toList().toTypedArray())))
		TelecomConnectionService.setAllOthersOnHold(uuid)
		setActive()
	}

	// dnc
	override fun onPlayDtmfTone(dtmf: Char) {
		TelecomUtilities.logToFile("[TelecomConnection] OnPlayDTMFTone")
	}

	// dnc - should be used to show the (fullscreen) notification for the user
	override fun onShowIncomingCallUi() {
		super.onShowIncomingCallUi()
		TelecomUtilities.logToFile("[TelecomConnection] Show incoming call UI")
	}
	// dnc - should be used to silence the ringer when the user presses the volume down button
	override fun onSilence() {
		super.onSilence()
		TelecomUtilities.logToFile("[TelecomConnection] TODO silence ringer")
	}

	// from inCallService (not used in self_managed)
	override fun onCallEvent(event: String, extras: Bundle?) {
		super.onCallEvent(event, extras)
		TelecomUtilities.logToFile("[TelecomConnection] CALL EVENT: $event")
	}

	override fun onStateChanged(state: Int) {
		super.onStateChanged(state)
		TelecomUtilities.logToFile("[TelecomConnection] ON STATE CHANGED: $state")
		// Toast.makeText(context, "onStateChanged $state", Toast.LENGTH_LONG).show()
	}

	// IMPORTANT (note: deprecated in Android 14 - API 34)
	// this event triggers for both mute state and audio route
	// actually it doesn't trigger for mute changes!!
	override fun onCallAudioStateChanged(state: CallAudioState) {
		super.onCallAudioStateChanged(state)
		TelecomUtilities.logToFile("[TelecomConnection] On Call Audio State Changed -- route: ${state.route} -- is muted: ${state.isMuted}")

		val uuid = handle[EXTRA_CALLKIT_ID] ?: ""
		val data: Map<String, Any> = object : HashMap<String, Any>() {
			init {
				put("event", ACTION_CALL_AUDIO_STATE_CHANGE)
				put(EXTRA_CALLKIT_ID, uuid)
				put("args", androidToJsRouteMap[state.route] ?: 1) // TODO use a different key than "args"?
			}
		}

		context.sendBroadcast(CallkitIncomingBroadcastReceiver.getIntent(context, ACTION_CALL_AUDIO_STATE_CHANGE, bundleOf(*data.toList().toTypedArray())))
	}

	companion object {
		private const val TAG = "TelecomConnection"
	}
}
