package com.maliotis.newsapp.fragments.newsFragment

import com.maliotis.newsapp.repository.realm.Article

/**
 * An interface for communication between the [NewsFragment] and the
 * [com.maliotis.newsapp.fragments.adapters.NewsAdapter]
 */
interface ArticleClickListener {
    fun itemClicked(position: Int, article: Article)
}