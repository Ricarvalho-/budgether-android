package br.edu.ifsp.scl.common

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

fun ImageView.setCircularImage(resources: Resources, @DrawableRes imageRes: Int) =
    setCircularBitmap(resources, BitmapFactory.decodeResource(resources, imageRes))

fun ImageView.setCircularBitmap(resources: Resources, bitmap: Bitmap) {
    val drawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
    drawable.isCircular = true
    setImageDrawable(drawable)
}