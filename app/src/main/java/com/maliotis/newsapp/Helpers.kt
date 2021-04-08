package com.maliotis.newsapp

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.TypedValue
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.google.gson.Gson
import com.maliotis.newsapp.repository.realm.Article

import io.reactivex.Observable
import io.realm.RealmModel
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

fun <T: androidx.appcompat.widget.Toolbar> T.getNavigationOnClickObservable(): Observable<View> {
    return Observable.create { emitter ->
        this.setNavigationOnClickListener {
            emitter.onNext(it)
        }
    }
}

fun <T: androidx.appcompat.widget.Toolbar> T.getMenuItemClickObservable(): Observable<MenuItem> {
    return Observable.create { emitter ->
        this.setOnMenuItemClickListener {
            emitter.onNext(it)
            true
        }
    }
}


fun ImageView.load(url: String?, loadOnlyFromCache: Boolean = false, onLoadingFinished: () -> Unit = {}) {
    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            onLoadingFinished()
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            onLoadingFinished()
            return false
        }
    }
    val requestOptions = RequestOptions.placeholderOf(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(18))
            .onlyRetrieveFromCache(loadOnlyFromCache)

    Glide.with(this)
            .load(url)
            .apply(requestOptions)
            .listener(listener)
            .into(this)
}

