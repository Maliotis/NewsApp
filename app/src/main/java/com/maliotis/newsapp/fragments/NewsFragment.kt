package com.maliotis.newsapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maliotis.newsapp.Helpers.convertDPToPixels
import com.maliotis.newsapp.R
import com.maliotis.newsapp.fragments.adapters.ArticleAdapter
import com.maliotis.newsapp.fragments.itemDecorators.GridSpacingItemDecoration
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

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        val articleAdapter = ArticleAdapter()
        val gridLayout = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        val spacing = convertDPToPixels(24f, requireContext()).toInt()
        val decorator = GridSpacingItemDecoration(2, spacing, true)
        newsRecyclerView.adapter = articleAdapter
        newsRecyclerView.layoutManager = gridLayout
        newsRecyclerView.addItemDecoration(decorator)
        

        newsViewModel.getArticles().observe(viewLifecycleOwner, Observer<List<Article>> { item ->
            Log.d(TAG, "onViewCreated: articleObserver -- item = $item")
            articleAdapter.listArticles = item.toMutableList()
            articleAdapter.notifyDataSetChanged()
        })

    }
}