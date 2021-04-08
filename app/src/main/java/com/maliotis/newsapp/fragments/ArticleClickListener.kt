package com.maliotis.newsapp.fragments

import com.maliotis.newsapp.repository.realm.Article

/**
 * An interface for communication between the [NewsFragment] and the
 * [com.maliotis.newsapp.fragments.adapters.ArticleAdapter]
 */
interface ArticleClickListener {
    fun itemClicked(position: Int, article: Article)
}