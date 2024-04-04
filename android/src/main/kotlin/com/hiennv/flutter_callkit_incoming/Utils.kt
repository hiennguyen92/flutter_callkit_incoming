package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.ref.WeakReference
import android.content.ComponentName
import android.content.pm.ResolveInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


class Utils {

    companion object {

        private var mapper: ObjectMapper? = null


        fun getGsonInstance(): ObjectMapper {
            if (mapper == null) {
                mapper = ObjectMapper()
            }
            return mapper!!
        }

        @JvmStatic
        fun dpToPx(dp: Float): Float {
            return dp * Resources.getSystem().displayMetrics.density
        }

        @JvmStatic
        fun pxToDp(px: Float): Float {
            return px / Resources.getSystem().displayMetrics.density
        }

        @JvmStatic
        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }

        @JvmStatic
        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }

        fun getNavigationBarHeight(context: Context): Int {
            val resources = context.resources
            val id = resources.getIdentifier(
                    "navigation_bar_height", "dimen", "android"
            )
            return if (id > 0) {
                resources.getDimensionPixelSize(id)
            } else 0
        }

        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            val id: Int =
                    resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (id > 0) {
                resources.getDimensionPixelSize(id)
            } else 0
        }

        fun backToForeground(context: Context) {
            val resolvedActivityClass = getCustomActivityClassWithIntentFilter(context)
            var intent = Intent()
            if (resolvedActivityClass == "") {
                 intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.cloneFilter()!!
            }else{

                intent.setComponent(ComponentName(context.packageName, resolvedActivityClass))
            }

            intent.setComponent(ComponentName(context.packageName, resolvedActivityClass))
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
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

                    return resolveInfo.activityInfo.name
                } catch (e: java.lang.ClassNotFoundException) {
                    //Should not happen, print it to the log!

                }
            }
            return ""
        }

        fun <T, C : MutableCollection<WeakReference<T>>> C.reapCollection(): C {
            this.removeAll {
                it.get() == null
            }
            return this
        }

        fun isTablet(context: Context): Boolean {
            return context.resources.getBoolean(R.bool.isTablet)
        }
    }
}
