package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.OkHttpClient

object PicassoProvider {
    @SuppressLint("StaticFieldLeak")
    private var instance: Picasso? = null

    fun get(context: Context, headers: HashMap<String, Any?>?): Picasso {
        if (instance == null) {
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val newRequestBuilder: okhttp3.Request.Builder = chain.request().newBuilder()
                    if (headers != null) {
                        for ((key, value) in headers) {
                            newRequestBuilder.addHeader(key, value.toString())
                        }
                    }
                    chain.proceed(newRequestBuilder.build())
                }
                .build()
            instance = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(client))
                .build()
        }
        return instance!!
    }
}


open class SafeTarget(
    private val notificationId: Int,
    private val onLoaded: (Bitmap) -> Unit
) : Target {

    var isCancelled = false

    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
        if (!isCancelled) {
            onLoaded(bitmap)
        }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
}