package com.hiennv.flutter_callkit_incoming

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class InAppCallManager(private val context: Context) {

    companion object {
        private const val ACCOUNT_ID = "flutter_callkit_incoming_in_app_call_account"
        private const val TAG = "InAppCallManager"
    }

    fun registerPhoneAccount() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Runs on the plugin-attach path, so anything thrown here crashes the host app
        // at startup. Telecom raises SecurityException without MANAGE_OWN_CALLS and
        // IllegalArgumentException on some OEM builds; neither is worth a crash.
        try {
            val telecomManager =
                context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return
            val componentName = ComponentName(context, CallkitConnectionService::class.java)
            val handle = PhoneAccountHandle(componentName, ACCOUNT_ID)

            // The account's name in Telecom (what getPhoneAccount().label
            // returns to the system and other apps): the app's own, not the
            // plugin's.
            val label = context.applicationInfo.loadLabel(context.packageManager)
            val phoneAccount = PhoneAccount.builder(handle, label)
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .apply {
                    // The app's calls are its own, not the phone app's call
                    // history: Android's documented default, stated explicitly.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        setExtras(Bundle().apply {
                            putBoolean(PhoneAccount.EXTRA_LOG_SELF_MANAGED_CALLS, false)
                        })
                    }
                }
                .build()

            telecomManager.registerPhoneAccount(phoneAccount)
            Log.d(TAG, "PhoneAccount registered.")
        } catch (e: Exception) {
            Log.e(TAG, "registerPhoneAccount failed — self-managed Telecom unavailable", e)
        }
    }

    fun unregisterPhoneAccount() {
        try {
            val telecomManager =
                context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return
            val componentName = ComponentName(context, CallkitConnectionService::class.java)
            val handle = PhoneAccountHandle(componentName, ACCOUNT_ID)

            telecomManager.unregisterPhoneAccount(handle)
            Log.d(TAG, "PhoneAccount unregistered.")
        } catch (e: Exception) {
            Log.e(TAG, "unregisterPhoneAccount failed", e)
        }
    }

    fun getPhoneAccountHandle(): PhoneAccountHandle {
        return PhoneAccountHandle(
            ComponentName(context, CallkitConnectionService::class.java),
            ACCOUNT_ID
        )
    }
}
