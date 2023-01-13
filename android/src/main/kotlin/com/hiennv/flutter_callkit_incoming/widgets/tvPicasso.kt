package com.hiennv.flutter_callkit_incoming.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class TextViewWithImageLoader @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), Target {
    enum class Direction { START, END, TOP, BOTTOM }

    private var mWidthInPixels: Int = 0
    private var mHeightInPixels: Int = 0
    private var mDirection: Direction = Direction.START //default value

    // This method initialize the required parameters for the TextView to load the image
    fun setupImageLoader(widthInPixels: Int, heightInPixels: Int, direction: Direction) {
        mWidthInPixels = widthInPixels
        mHeightInPixels = heightInPixels
        mDirection = direction
    }

    // sets the size of the drawable
    private fun setDrawableBounds(drawable: Drawable) {
        drawable.setBounds(0, 0, mWidthInPixels, mHeightInPixels)
    }

    // Sets the initial placeholder drawable
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        //checking if height and width are valid
        if (placeHolderDrawable != null && mWidthInPixels > 0 && mHeightInPixels > 0) {
            setDrawableBounds(placeHolderDrawable)
            setDrawable(placeHolderDrawable)

        }
    }

    // set the drawable based on the Direction enum
    private fun setDrawable(placeHolderDrawable: Drawable?) {
        when (mDirection) {
            Direction.START -> setCompoundDrawables(placeHolderDrawable, null, null, null);
            Direction.END -> setCompoundDrawables(null, null, placeHolderDrawable, null);
            Direction.TOP -> setCompoundDrawables(null, placeHolderDrawable, null, null);
            Direction.BOTTOM -> setCompoundDrawables(null, null, null, placeHolderDrawable);
        }
    }

    //In this method we receive the image from the url
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        //checking if height and width are valid
        if (mWidthInPixels > 0 && mHeightInPixels > 0) {
            val drawable = BitmapDrawable(resources, bitmap)
            setDrawableBounds(drawable)
            setDrawable(drawable)
        }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        //Do nothing as we are already setting a default value in onPrepareLoad method
        // you can add your logic here if required
    }
}