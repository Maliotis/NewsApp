package com.maliotis.newsapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.maliotis.newsapp.repository.realm.Article
import com.maliotis.newsapp.viewModels.NewsViewModel

class NewsFragment: Fragment() {

    val TAG = NewsFragment::class.java.simpleName

    private val newsViewModel: NewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.news_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel.getArticles().observe(viewLifecycleOwner, Observer<List<Article>> { item ->
            Log.d(TAG, "onViewCreated: articleObserver -- item = $item")
        })

    }
}