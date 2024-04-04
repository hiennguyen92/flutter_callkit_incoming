package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.content.Intent
import android.content.ComponentName
import android.app.Activity
import android.content.pm.ResolveInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

object AppUtils {
    fun getAppIntent(context: Context, action: String? = null, data: Bundle? = null): Intent? {

        val resolvedActivityClass = getCustomActivityClassWithIntentFilter(context)

        var intent = Intent()
        
        if (resolvedActivityClass == "") {
            intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.cloneFilter()!!
        }else{

            intent.setComponent(ComponentName(context.packageName, resolvedActivityClass))
        }
        intent.setComponent(ComponentName(context.packageName, resolvedActivityClass))
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(FlutterCallkitIncomingPlugin.EXTRA_CALLKIT_CALL_DATA, data)
        intent.action = action
        return intent
    }

    private fun getCustomActivityClassWithIntentFilter(context: Context): String{
        val searchedIntent: Intent =
            Intent().setAction("com.hiennv.flutter_callkit_incoming.CALL_ACTIVITY").setPackage(context.getPackageName())
        val resolveInfos: List<ResolveInfo> = context.getPackageManager().queryIntentActivities(
            searchedIntent,
            PackageManager.GET_RESOLVED_FILTER
        )
        if (resolveInfos.size > 0) {
            val resolveInfo: ResolveInfo = resolveInfos[0]
            try {
                Log.d("APPUTILS","******"+resolveInfo.activityInfo.toString())
                return resolveInfo.activityInfo.name
            } catch (e: java.lang.ClassNotFoundException) {
                //Should not happen, print it to the log!
                Log.e(
                    "APPUTILS",
                    "Failed when resolving the custom activity class via intent filter, stack trace follows!",
                    e
                )
            }
        }
        return ""
    }
}