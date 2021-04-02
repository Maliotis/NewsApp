package com.maliotis.newsapp.model

import com.maliotis.newsapp.repository.realm.Article

class News {
    var status: String? = null
    var totalResults: Long? = null
    var articles: List<Article>? = null

}