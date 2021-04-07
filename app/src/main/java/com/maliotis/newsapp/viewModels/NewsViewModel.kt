package com.maliotis.newsapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maliotis.newsapp.enums.ApiStatus
import com.maliotis.newsapp.repository.NewsRepository
import com.maliotis.newsapp.repository.realm.Article
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm

class NewsViewModel: ViewModel() {
    val TAG = NewsViewModel::class.java.simpleName

    val realm = Realm.getDefaultInstance()

    // Communication between fragment - Activity
    private val mutableSelectedItem = MutableLiveData<Article>()
    val selectedItem: LiveData<Article> get() = mutableSelectedItem
    fun selectItem(item: Article) {
        mutableSelectedItem.value = item
    }

    // Add article to be loaded in webView
    private val mutableDetailArticle = MutableLiveData<Article>()
    val detailArticle: LiveData<Article> get() = mutableDetailArticle
    fun addDetailArticle(article: Article) {
        mutableDetailArticle.postValue(article)
    }

    // get api status when fetching news
    val newsApiSucceeded: Subject<ApiStatus> = PublishSubject.create()

    private val articleResults: RealmResults<Article> by lazy {
        realm.where(Article::class.java).notEqualTo("hidden", true).findAllAsync()
    }

    // convenient var to add only 1 listener
    private var isSubscribed = false

    private val newsRepository: NewsRepository by lazy {
        NewsRepository()
    }

    // articles
    private val articles: MutableLiveData<List<Article>> by lazy {
        MutableLiveData<List<Article>>().also { loadArticles() }
    }
    fun getArticles(): LiveData<List<Article>> {
        return articles
    }

    // load the first page of news
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
                Log.d(TAG, "subscribeToArticles: changeSet = ${changeSet.toString()}")
                articles.postValue(t.sort("publishedAt", Sort.DESCENDING).toList())

//                if (t.isNotEmpty() && changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
//                    articles.postValue(t.sort("publishedAt", Sort.DESCENDING).toList())
//                }
//
//                if (changeSet.state == OrderedCollectionChangeSet.State.UPDATE &&  changeSet.insertionRanges.isNotEmpty()) {
//                    var tempArticles = mutableListOf<Article>()
//                    changeSet.insertionRanges.forEach {
//                        val startIndex = it.startIndex
//                        val length = startIndex + it.length
//                        for (i in startIndex until length) {
//                            t[i]?.let { article ->
//                                tempArticles.add(article)
//                            }
//                        }
//                    }
//                    Log.d(TAG, "subscribeToArticles: new articles inserted.size = ${tempArticles.size}")
//                    Log.d(TAG, "subscribeToArticles: new articles inserted = $tempArticles")
//                    tempArticles.sortWith { article1, article2 ->
//                        article1?.publishedAt?.compareTo(article2?.publishedAt ?: "") ?: 0
//                    }
//                    articles.postValue(tempArticles.asReversed())
//                } else if (changeSet.state == OrderedCollectionChangeSet.State.UPDATE &&  changeSet.changeRanges.isNotEmpty()) {
//                    Log.d(TAG, "subscribeToArticles: ")
//                    var tempArticles = mutableListOf<Article>()
//                    changeSet.changeRanges.forEach {
//                        val startIndex = it.startIndex
//                        val length = startIndex + it.length
//                        for (i in startIndex until length) {
//                            t[i]?.let { article ->
//                                if (article.hidden == true)
//                                    tempArticles.add(article)
//                            }
//                        }
//                    }
//
//                }

            }
        }
    }

    fun hideArticle(articleId: String?, hide: Boolean = true) {
        if (articleId == null) return
        realm.executeTransactionAsync {
            val article: Article? = it.where(Article::class.java).equalTo("id", articleId).findFirst()
            article?.hidden = hide
        }
    }

    fun getOlderNews() {
        val articlesSize = articles.value?.size ?: 0
        val pageSize = articlesSize / 20 + 1
        Log.d(TAG, "getOlderNews: pageSize = $pageSize")
        newsRepository.getOlderNews(newsApiSucceeded, pageSize)

    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: onCleared called")
        articleResults.removeAllChangeListeners()
        newsRepository.disposables.clear()
        isSubscribed = false
        if (!realm.isClosed) realm.close()
    }
}