package com.hiennv.flutter_callkit_incoming

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.ref.WeakReference


class Utils {

    companion object {

        private var mapper: ObjectMapper? = null

        fun isApplicationForeground(context: Context): Boolean {
            val keyguardManager =
                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?

            if (keyguardManager != null && keyguardManager.isKeyguardLocked) {
                return false
            }

            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            if (activityManager == null) {
                return false
            }

            val appProcesses = activityManager.runningAppProcesses
            if (appProcesses == null) {
                return false
            }

            val packageName = context.packageName
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName == packageName
                ) {
                    return true
                }
            }

            return false
        }

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
            val packageName = context.packageName
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.cloneFilter()
            intent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
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
