package com.maliotis.newsapp

import android.content.Context
import android.util.TypedValue

object Helpers {

    fun convertDPToPixels(dip: Float, context: Context): Float {
        val r = context.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )
        return px
    }
}