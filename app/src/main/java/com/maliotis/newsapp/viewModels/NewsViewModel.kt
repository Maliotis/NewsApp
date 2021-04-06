package com.maliotis.newsapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maliotis.newsapp.enums.ApiStatus
import com.maliotis.newsapp.enums.ItemSelected
import com.maliotis.newsapp.repository.NewsRepository
import com.maliotis.newsapp.repository.realm.Article
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class NewsViewModel: ViewModel() {
    val TAG = NewsViewModel::class.java.simpleName

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val mutableSelectedItem = MutableLiveData<Pair<ItemSelected, Any?>>()
    val selectedItem: LiveData<Pair<ItemSelected, Any?>> get() = mutableSelectedItem

    fun selectItem(item: Pair<ItemSelected, Any?>) {
        mutableSelectedItem.value = item
    }

    private val mutableDetailArticle = MutableLiveData<Article>()
    val detailArticle: LiveData<Article> get() = mutableDetailArticle

    fun addDetailArticle(article: Article) {
        mutableDetailArticle.postValue(article)
    }

    val newsApiSucceeded: Subject<ApiStatus> = PublishSubject.create()

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
        newsRepository.getNews(newsApiSucceeded)
    }

    private fun subscribeToArticles() {
        if (!isSubscribed) {
            isSubscribed = true
            articleResults.addChangeListener { t, changeSet ->
                Log.d(TAG, "subscribeToArticles: t.size = ${t.size}")
                Log.d(TAG, "subscribeToArticles: changeSet.state = ${changeSet.state}")
                if (t.isNotEmpty() && changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    articles.postValue(t.sort("publishedAt", Sort.DESCENDING).toList())
                }

                if (changeSet.state == OrderedCollectionChangeSet.State.UPDATE &&  changeSet.insertionRanges.isNotEmpty()) {
                    var tempArticles = mutableListOf<Article>()
                    changeSet.insertionRanges.forEach {
                        val startIndex = it.startIndex
                        val length = startIndex + it.length
                        for (i in startIndex until length) {
                            t[i]?.let { article ->
                                tempArticles.add(article)
                            }
                        }
                    }
                    Log.d(TAG, "subscribeToArticles: new articles inserted.size = ${tempArticles.size}")
                    Log.d(TAG, "subscribeToArticles: new articles inserted = $tempArticles")
                    tempArticles.sortWith { article1, article2 ->
                        article1?.publishedAt?.compareTo(article2?.publishedAt ?: "") ?: 0
                    }
                    articles.postValue(tempArticles.asReversed())
                }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        articleResults.removeAllChangeListeners()
        newsRepository.disposables.forEach {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        if (!realm.isClosed)
            realm.close()
    }

    fun getOlderNews() {
        newsRepository.getOlderNews(newsApiSucceeded)

    }
}