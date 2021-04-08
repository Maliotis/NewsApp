package com.maliotis.newsapp.fragments

import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.maliotis.newsapp.R


class ArticleWebViewClient(val context: Context): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            view?.loadUrl(it.url.toString())
        }
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val parent = view?.parent as ConstraintLayout
        val progressBar = parent.findViewById<LinearProgressIndicator>(R.id.articleProgressBar)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        //progressBar.sta

    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val parent = view?.parent as ConstraintLayout
        val progressBar = parent.findViewById<LinearProgressIndicator>(R.id.articleProgressBar)
        progressBar.visibility = View.GONE
    }
}