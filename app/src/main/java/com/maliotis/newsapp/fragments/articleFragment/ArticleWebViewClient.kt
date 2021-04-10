package com.maliotis.newsapp.fragments.articleFragment

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.maliotis.newsapp.R

class ArticleWebViewClient : WebViewClient() {

    /**
     * Load the current url and consume by returning true to avoid open in external browser
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            view?.loadUrl(it.url.toString())
        }
        return true
    }

    /**
     * Show a progressbar when loading the content
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val parent = view?.parent as ConstraintLayout
        val progressBar = parent.findViewById<LinearProgressIndicator>(R.id.articleProgressBar)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

    }

    /**
     * Hide the progressbar when the content finished loading
     * Changes visibility to [View.GONE] so that doesn't take any space for layout
     * purposes
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val parent = view?.parent as ConstraintLayout
        val progressBar = parent.findViewById<LinearProgressIndicator>(R.id.articleProgressBar)
        progressBar.visibility = View.GONE
    }
}