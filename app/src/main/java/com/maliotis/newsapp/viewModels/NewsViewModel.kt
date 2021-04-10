package com.maliotis.newsapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maliotis.newsapp.RealmUtil.realm
import com.maliotis.newsapp.enums.ApiStatus
import com.maliotis.newsapp.repository.NewsRepository
import com.maliotis.newsapp.repository.realm.Article
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults


class NewsViewModel: ViewModel() {

    /**
     * Communication between fragment - Activity
     */
    private val mutableSelectedItem = MutableLiveData<Article>()
    val selectedItem: LiveData<Article> get() = mutableSelectedItem
    fun selectItem(item: Article) {
        mutableSelectedItem.value = item
    }

    /**
     * Add article to be loaded in webView
     */
    private val mutableDetailArticle = MutableLiveData<Article>()
    val detailArticle: LiveData<Article> get() = mutableDetailArticle
    fun addDetailArticle(article: Article) {
        mutableDetailArticle.postValue(article)
    }

    /**
     * Get api status when fetching news
     */
    val newsApiSucceeded: Subject<ApiStatus> = PublishSubject.create()

    private val articleResults: RealmResults<Article> by lazy {
        realm.where(Article::class.java).equalTo("hidden", false)
            .or().isNull("hidden").findAllAsync()
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
        getNews()
    }

    private fun subscribeToArticles() {
        if (!isSubscribed) {
            isSubscribed = true
            articleResults.addChangeListener { t, changeSet ->
                val tList = t.toList()
                val sortedList = tList.sortedWith( compareBy( { it.pinned }, { it.publishedAt })).asReversed()
                val unManagedList = realm.copyFromRealm(sortedList)
                articles.postValue(unManagedList)
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

    fun pinArticle(articleId: String?, pin: Boolean = true) {
        if (articleId == null) return
        realm.executeTransactionAsync {
            val article: Article? = it.where(Article::class.java).equalTo("id", articleId).findFirst()
            article?.pinned = pin
            //article?.hidden = hide
        }
    }

    fun getNews() {
        newsRepository.getNews(newsApiSucceeded)
    }

    fun getOlderNews() {
        val articlesSize = articles.value?.size ?: 0
        val pageSize = articlesSize / 20 + 1
        newsRepository.getOlderNews(newsApiSucceeded, pageSize)

    }

    override fun onCleared() {
        super.onCleared()
        articleResults.removeAllChangeListeners()
        newsRepository.disposables.clear()
        isSubscribed = false
        if (!realm.isClosed) realm.close()
    }
}