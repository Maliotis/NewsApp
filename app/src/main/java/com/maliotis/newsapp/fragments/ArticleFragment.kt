package com.maliotis.newsapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.maliotis.newsapp.*
import com.maliotis.newsapp.viewModels.NewsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class ArticleFragment: Fragment() {

    val TAG = ArticleFragment::class.java.simpleName

    val viewModel: NewsViewModel by activityViewModels()
    val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.article_layout, container, false)

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val articleBackButton = view.findViewById<ImageButton>(R.id.articleBackButton)
        val browserButton = view.findViewById<ImageButton>(R.id.articleOpenInBrowserButton)
        val webView  = view.findViewById<WebView>(R.id.articleWebView)
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.webViewClient = ArticleWebViewClient(requireContext())
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.setSupportZoom(true)
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webView.setInitialScale(1)

        articleBackButtonObserver(articleBackButton)


        viewModel.detailArticle.observe(viewLifecycleOwner, {
            it.url?.run {
                webView.loadUrl(this)
                articleOpenInBrowserButtonObserver(browserButton, this)
            }
        })

    }

    private fun articleBackButtonObserver(backButton: ImageButton) {
        val backDisp = backButton.getClickObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                requireActivity().onBackPressed()
            }

        disposables.add(backDisp)
    }

    private fun articleOpenInBrowserButtonObserver(browserButton: ImageButton, url: String) {
        val browserDisp = browserButton.getClickObservable()
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        startActivity(this)
                    }
                }

        disposables.add(browserDisp)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}