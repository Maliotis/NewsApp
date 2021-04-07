package com.maliotis.newsapp.fragments

import com.maliotis.newsapp.repository.realm.Article

interface ArticleClickListener {
    fun itemClicked(position: Int, article: Article)
}