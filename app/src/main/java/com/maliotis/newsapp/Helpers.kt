package com.maliotis.newsapp

import android.content.Context
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.TypedValue
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*



fun convertDPToPixels(dip: Float, context: Context): Float {
    val r = context.resources
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip,
        r.displayMetrics
    )
    return px
}

fun isNetworkConnected(context: Context?): Boolean {
    val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
    return isConnected
}

fun isoToDate(isoString: String?): String {
    if (isoString != null) {
        val isoDateFormat: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date: Date? = isoDateFormat.parse(isoString)

        if (date != null) {
            return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(date)
        }
    }
    return ""
}

inline fun <T> T.guard(block: T.() -> Unit): T {
    if (this == null) block(); return this
}

fun <T: View> T.getClickObservable(): Observable<T> {
    return Observable.create { emitter ->
        this.setOnClickListener {
            emitter.onNext(this)
        }
    }
}

