package com.hiennv.flutter_callkit_incoming.telecom

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.hiennv.flutter_callkit_incoming.CallkitConstants
import com.hiennv.flutter_callkit_incoming.Data
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.util.UUID

// the most important thing this does is registering the phone account
@RequiresApi(Build.VERSION_CODES.M)
class TelecomUtilities(private val applicationContext : Context) {

	private lateinit var telecomManager: TelecomManager
	private lateinit var handle: PhoneAccountHandle
	private lateinit var telephonyManager: TelephonyManager

	private var requiredPermissions: Array<String>

	init {
		registerPhoneAccount(applicationContext)

		requiredPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.RECORD_AUDIO)
		if(Build.VERSION.SDK_INT > 29){
			requiredPermissions += Manifest.permission.READ_PHONE_NUMBERS
		}
	}

	@RequiresApi(Build.VERSION_CODES.M)
	private fun registerPhoneAccount(appContext: Context) {

		val cName = ComponentName(applicationContext, TelecomConnectionService::class.java)
		val appName = getApplicationName(appContext)
		handle = PhoneAccountHandle(cName, appName)

		val identifier = appContext.resources.getIdentifier("ic_logo", "mipmap", appContext.packageName)
		val icon = Icon.createWithResource(appContext, identifier)

		val account = PhoneAccount.Builder(handle, appName)
			.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
			.setIcon(icon)
			.build()

		telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

		telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
		telecomManager.registerPhoneAccount(account)

		logToFile("[TelecomUtilities] REGISTERED PHONE ACCOUNT")
	}

	private fun getApplicationName(appContext: Context): String {
		val applicationInfo = appContext.applicationInfo
		val stringId = applicationInfo.labelRes
		return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else appContext.getString(stringId)
	}

	// incoming call
	@RequiresApi(Build.VERSION_CODES.M)
	fun reportIncomingCall(data: Data) {
		try {
			val extras = Bundle()

			val uuid: String = data.id
			extras.putString(CallkitConstants.EXTRA_CALLKIT_ID, uuid)

			// dnc
			val name: String = data.nameCaller
			extras.putString(CallkitConstants.EXTRA_CALLKIT_NAME_CALLER, name)
			// visible in cars
			val handleString: String = name // data.handle
			val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, name, null)
			extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)

			logToFile("[TelecomUtilities] reportIncomingCall number: $handleString, uuid: $uuid")

			telecomManager.addNewIncomingCall(handle, extras)

		} catch (er: Exception) {
			Log.e(TAG,"EXCEPTION reportIncomingCall -- $er", er)

			val stackTrace = StringWriter()
			er.printStackTrace(PrintWriter(stackTrace))

			logToFile("[TelecomUtilities] EXCEPTION reportIncomingCall -- $er -- message: ${er.message} -- stack: $stackTrace")
		}
	}

	// outgoing call
	@RequiresApi(Build.VERSION_CODES.M)
	@SuppressLint("MissingPermission")
	fun startCall(data: Data) {
		val extras = Bundle() // has the account handle
		val callExtras = Bundle() // has the caller's name/number

		val uuid = UUID.fromString(data.uuid)

		val number : String = data.handle
		val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null)
		callExtras.putString(CallkitConstants.EXTRA_CALLKIT_HANDLE, number)
		callExtras.putString(CallkitConstants.EXTRA_CALLKIT_ID, uuid.toString())

		logToFile("[TelecomUtilities] startCall -- number: $number")

		extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
		extras.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callExtras)
		telecomManager.placeCall(uri, extras)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	fun endCall(data: Data) {
		logToFile("[TelecomUtilities] endCall -- UUID: ${data.uuid}")

		val uuid: String = data.uuid
		val connection = TelecomConnectionService.getConnection(uuid)
		connection?.onDisconnect()
	}

	@RequiresApi(Build.VERSION_CODES.M)
	fun holdCall(data: Data) {
		logToFile("[TelecomUtilities] holdCall -- UUID = ${data.uuid} | hold = ${data.isOnHold}")
		val connection = TelecomConnectionService.getConnection(data.uuid)

		if (data.isOnHold) connection?.onHold()
		else connection?.onUnhold()
	}

	@RequiresApi(Build.VERSION_CODES.M)
	fun unHoldCall(data: Data) {
		logToFile("[TelecomUtilities] unHoldCall -- UUID = ${data.uuid} ")
		val connection = TelecomConnectionService.getConnection(data.uuid)
		connection?.onUnhold()
	}

	@RequiresApi(Build.VERSION_CODES.O)
	fun setAudioRoute(data: Data) {
		val connection = TelecomConnectionService.getConnection(data.uuid)

		logToFile("[TelecomUtilities] setAudioRoute -- UUID = ${data.uuid} | audioRoute = ${data.audioRoute}")

		val route = jsToAndroidRouteMap[data.audioRoute] ?: return
		connection?.setAudioRoute(route)

	}

	@RequiresApi(Build.VERSION_CODES.M)
	fun muteCall(data: Data) {
		logToFile("[TelecomUtilities] muteCall -- UUID = ${data.uuid} | hold = ${data.isMuted}")
		val uuid : String = data.uuid
		val muted : Boolean = data.isMuted
		val connection = TelecomConnectionService.getConnection(uuid) ?: return

		val newAudioState = if (muted) {
			CallAudioState(true, connection.callAudioState.route, connection.callAudioState.supportedRouteMask)
		} else {
			CallAudioState(false, connection.callAudioState.route, connection.callAudioState.supportedRouteMask)
		}

		connection.onCallAudioStateChanged(newAudioState)
	}

	fun acceptCall(data: Data) {
		val uuid : String = data.uuid

		val connection = TelecomConnectionService.getConnection(uuid)
		logToFile("[TelecomUtilities] acceptCall -- UUID = $uuid connection exists? ${connection!=null}")

		// avoid infinite loop by not calling onAnswer if the state isn't already ACTIVE
		if (connection?.state != Connection.STATE_ACTIVE) connection?.onAnswer()
		else logToFile("[TelecomUtilities] acceptCall -- UUID = $uuid is already active")

		logToFile("[TelecomUtilities] acceptCall -- AUDIO ROUTE: ${connection?.callAudioState?.route?.toString()}")
	}

	fun endAllActiveCalls() {
		Log.d(TAG, "endAllActiveCalls: ${TelecomConnectionService.currentConnections.size}")
		TelecomConnectionService.currentConnections.forEach { (_, c) -> c.onDisconnect() }
	}

	companion object {
		private const val TAG = "TelecomUtilities"

		public var telecomUtilitiesSingleton :TelecomUtilities? = null


		val androidToJsRouteMap = mapOf(
			CallAudioState.ROUTE_EARPIECE to 1,
			CallAudioState.ROUTE_BLUETOOTH to 2,
			CallAudioState.ROUTE_WIRED_HEADSET to 3,
			CallAudioState.ROUTE_SPEAKER to 4,
			CallAudioState.ROUTE_WIRED_OR_EARPIECE to 5,
		)

		val jsToAndroidRouteMap = mapOf(
			1 to CallAudioState.ROUTE_EARPIECE,
			2 to CallAudioState.ROUTE_BLUETOOTH,
			3 to CallAudioState.ROUTE_WIRED_HEADSET,
			4 to CallAudioState.ROUTE_SPEAKER,
			5 to CallAudioState.ROUTE_WIRED_OR_EARPIECE,
		)

		private const val logToFile = false // log to file flag
		fun logToFile(message: String) {
			Log.d("CallkitTelecom", message)

			val context = TelecomConnectionService.applicationContext ?: return

			if (!logToFile) return
			try {
				val timestamp = LocalDateTime.now(ZoneOffset.UTC)
				val path = "${context.cacheDir}/console_logs_${timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.txt"

				val file = File(path)
				file.appendText("${timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))} $message")

			} catch (e: Exception) {
				Log.e(TAG, e.message ?: "", e)
			}
		}
	}
}
