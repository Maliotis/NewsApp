package com.maliotis.newsapp

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.maliotis.newsapp.enums.ScrollState

import io.reactivex.Observable
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

fun <T: SwipeRefreshLayout> T.onRefreshObservable(): Observable<Unit> {
    return Observable.create { emitter ->
        this.setOnRefreshListener {
            emitter.onNext(Unit)
        }
    }
}

fun <T: RecyclerView> T.onScrollObservable(): Observable<ScrollState> {
    return Observable.create { emitter ->
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    emitter.onNext(ScrollState.BOTTOM)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) {
                    emitter.onNext(ScrollState.CANCEL)
                }
            }

        })
    }
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


/**
 * Convenient method to load ImageView content using the Glide library
 */
fun ImageView.load(url: String?, cornerRadius: Int) {
    val requestOptions = RequestOptions.placeholderOf(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))

    Glide.with(this)
            .load(url)
            .apply(requestOptions)
            .into(this)
}

