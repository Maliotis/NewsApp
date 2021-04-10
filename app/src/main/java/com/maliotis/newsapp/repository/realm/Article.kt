package com.maliotis.newsapp.repository.realm

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Article: RealmModel {

    @PrimaryKey
    var id: String? = null

    var author: String? = null
    var title: String? = null
    var description: String? = null
    var url: String? = null
    var urlToImage: String? = null
    var publishedAt: String? = null
    var content: String? = null
    var hidden: Boolean? = null
    var pinned: Boolean? = null
}