package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.content.Intent
import android.content.ComponentName

object AppUtils {
    fun getAppIntent(context: Context, action: String? = null, data: Bundle? = null): Intent? {
        val intent = Intent()
        intent.setComponent(ComponentName(context.packageName, "com.noknox.morador.flutter.FlutterCallActivity"))
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(FlutterCallkitIncomingPlugin.EXTRA_CALLKIT_CALL_DATA, data)
        intent.action = action
        return intent
    }
}