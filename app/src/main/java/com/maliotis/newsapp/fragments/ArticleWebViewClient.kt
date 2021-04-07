package com.maliotis.newsapp.fragments

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient



class ArticleWebViewClient(val context: Context): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            view?.loadUrl(it.url.toString())
        }
        return true
    }
}