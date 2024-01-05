package com.hiennv.flutter_callkit_incoming.telecom

import android.content.Context
import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_HANDLE
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_ID
import com.hiennv.flutter_callkit_incoming.CallkitConstants.EXTRA_CALLKIT_NAME_CALLER
import java.util.UUID
import android.util.Log

// for now, I don't care about notifying anybody about connection creations/failures
// the connection itself is supposed to do that?
class TelecomConnectionService : ConnectionService() {



	override fun onCreate() {
		super.onCreate()
		TelecomConnectionService.applicationContext = applicationContext
	}

	override fun onDestroy() {

		try {
			Log.d(TAG, "[TelecomConnectionService] onDestroy")
			TelecomUtilities.logToFile("[TelecomConnectionService] onDestroy ")
			TelecomUtilities.logToFile("[TelecomConnectionService] onDestroy - kill all calls ");

			//We end all connections
			for ((key, value) in currentConnections) {
				value.endCall()

			}
		}
		catch (er: Exception) {
			TelecomUtilities.logToFile("EXCEPTION reportIncomingCall -- $er")
		}

	}

	override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle, request: ConnectionRequest): Connection {
		TelecomUtilities.logToFile("[TelecomConnectionService] OnCreateIncomingConnection -- UUID:  number:${request.extras.getString(EXTRA_CALLKIT_ID)}")

		// to test global exception handling
		// throw Exception("EXCEPTION from onCreateIncomingConnection")

		val incomingCallConnection = createConnection(request)
		incomingCallConnection.setRinging()
		incomingCallConnection.setInitialized()

		return incomingCallConnection
	}

	override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest) {
		super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
		TelecomUtilities.logToFile("[TelecomConnectionService] OnCreateIncomingConnection FAILED")
	}

	override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle, request: ConnectionRequest): Connection {
		val extras = request.extras
		val number = request.address?.schemeSpecificPart ?: "Outbound Call"
		val displayName = extras.getString(EXTRA_CALLKIT_NAME_CALLER)

		TelecomUtilities.logToFile("[TelecomConnectionService] onCreateOutgoingConnection -- UUID: ${request.extras.getString(EXTRA_CALLKIT_ID)} number: $number, displayName:$displayName")

		val outgoingCallConnection = createConnection(request)
		outgoingCallConnection.setDialing()

		TelecomUtilities.logToFile("[TelecomConnectionService] onCreateOutgoingConnection: dialing")

		val uuid = outgoingCallConnection.extras.getString(EXTRA_CALLKIT_ID) ?: ""
		setAllOthersOnHold(uuid)

		return outgoingCallConnection
	}

	override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest) {
		super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
		TelecomUtilities.logToFile("[TelecomConnectionService] OnCreateOutgoingConnectionFailed FAILED")
	}

	private fun createConnection(request: ConnectionRequest): Connection {
		TelecomUtilities.logToFile("[TelecomConnectionService] createConnection -- UUID: ${request.extras.getString(EXTRA_CALLKIT_ID)}")

		val extras = request.extras
		if (extras.getString(EXTRA_CALLKIT_ID) == null) {
			extras.putString(EXTRA_CALLKIT_ID, UUID.randomUUID().toString())
		}

		val extrasMap = bundleToMap(extras)
		extrasMap[EXTRA_CALLKIT_HANDLE] = request.address?.toString() ?: "Callkit Incoming Call"
		val connection = TelecomConnection(this, extrasMap)

		connection.setInitializing()
		connection.extras = extras
		currentConnections[extras.getString(EXTRA_CALLKIT_ID)] = connection

		return connection
	}

	private fun bundleToMap(extras: Bundle): HashMap<String, String> {
		val extrasMap = HashMap<String, String>()
		val keySet = extras.keySet()
		val iterator: Iterator<String> = keySet.iterator()
		while (iterator.hasNext()) {
			val key = iterator.next()
			if (extras[key] != null) {
				extrasMap[key] = extras[key].toString()
			}
		}
		return extrasMap
	}

	companion object {

		private const val TAG = "TelecomConnectionService"


		var applicationContext: Context? = null

		var currentConnections: MutableMap<String?, TelecomConnection> = HashMap()

		fun getConnection(connectionId: String?): Connection? {
			return if (currentConnections.containsKey(connectionId)) {
				currentConnections[connectionId]
			} else null
		}

		fun deinitConnection(connectionId: String) {
			TelecomUtilities.logToFile("[TelecomConnectionService] deinitConnection: $connectionId")
			if (currentConnections.containsKey(connectionId)) {
				currentConnections.remove(connectionId)
			}
		}

		// put all other calls on hold
		fun setAllOthersOnHold(myUID: String?) {
			for ((key, value) in currentConnections) {
				if (!key.contentEquals(myUID)) {
					value.onHold()
				}
			}
		}
	}
}
