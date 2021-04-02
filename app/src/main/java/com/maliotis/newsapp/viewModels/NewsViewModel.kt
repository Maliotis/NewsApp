package com.maliotis.newsapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maliotis.newsapp.repository.NewsRepository
import com.maliotis.newsapp.repository.realm.Article
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults

class NewsViewModel: ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val articleResults: RealmResults<Article> by lazy {
        realm.where(Article::class.java).findAllAsync()
    }

    private var isSubscribed = false

    private val newsRepository: NewsRepository by lazy {
        NewsRepository()
    }

    private val articles: MutableLiveData<List<Article>> by lazy {
        MutableLiveData<List<Article>>().also {
            loadArticles()
        }
    }

    fun getArticles(): LiveData<List<Article>> {
        return articles
    }

    private fun loadArticles() {
        subscribeToArticles()
        newsRepository.getNews()
    }

    private fun subscribeToArticles() {
        if (!isSubscribed) {
            isSubscribed = true
            articleResults.addChangeListener { t, changeSet ->
                if (t.isNotEmpty() && changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    articles.postValue(t.toList())
                }
                if (changeSet.insertionRanges.isNotEmpty()) {
                    changeSet.insertionRanges.forEach {
                        val startIndex = it.startIndex
                        val length = it.length
                        var tempArticles = mutableListOf<Article>()
                        for (i in startIndex until length) {
                            t[i]?.let { article ->
                                tempArticles.add(article)
                            }

                        }
                        articles.postValue(tempArticles)
                    }
                }
                // TODO: do the same for additions

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        articleResults.removeAllChangeListeners()
        if (!realm.isClosed)
            realm.close()
    }
}