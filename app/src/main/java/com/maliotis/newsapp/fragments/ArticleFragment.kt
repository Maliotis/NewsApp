package com.maliotis.newsapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        val toolbar = view.findViewById<Toolbar>(R.id.articleToolbar)
        val webView  = view.findViewById<WebView>(R.id.articleWebView)

        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.webViewClient = ArticleWebViewClient(requireContext())
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.setSupportZoom(true)
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webView.setInitialScale(1)

        toolbar.inflateMenu(R.menu.article_web_menu)

        toolBarNavigationObserver(toolbar)

        viewModel.detailArticle.observe(viewLifecycleOwner, {
            it.url?.run {
                webView.loadUrl(this)
                toolBarMenuItemObserver(toolbar, this)
            }
        })

    }

    private fun toolBarNavigationObserver(toolbar: Toolbar) {
        val disp = toolbar.getNavigationOnClickObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                requireActivity().onBackPressed()
            }

        disposables.add(disp)
    }

    private fun toolBarMenuItemObserver(toolbar: Toolbar, url: String) {
        val disp = toolbar.getMenuItemClickObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when(it.itemId) {
                    R.id.articleOpenInBrowserButton -> {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            startActivity(this)
                        }
                    }
                }
            }
        disposables.add(disp)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}