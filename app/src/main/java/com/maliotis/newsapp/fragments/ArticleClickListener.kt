package com.maliotis.newsapp.fragments

import android.view.View
import com.maliotis.newsapp.repository.realm.Article

interface ArticleClickListener {
    fun itemClicked(position: Int, article: Article, shareViewElements: List<View>)
}