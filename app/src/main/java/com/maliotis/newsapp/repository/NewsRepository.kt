package com.maliotis.newsapp.repository

import android.util.Log
import com.maliotis.newsapp.enums.ApiStatus

import com.maliotis.newsapp.RealmApplication
import com.maliotis.newsapp.RealmUtil.realm
import com.maliotis.newsapp.isNetworkConnected
import com.maliotis.newsapp.model.News
import com.maliotis.newsapp.repository.realm.Article
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.newsService
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.options
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.typeEverything
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.version2
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import io.realm.Realm
import io.realm.Sort
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*


class NewsRepository {
    val TAG = NewsRepository::class.java.simpleName
    val disposables = CompositeDisposable()

    fun getNews(subject: Subject<ApiStatus>) {
        options["page"] = "1"
        val newsCall = newsService.listNews(
            version2,
            typeEverything,
            options
        )

        subscribeToApi(newsCall, true, subject)
    }

    fun getOlderNews(subject: Subject<ApiStatus>, pageSize: Int) {
        options["page"] = pageSize.toString()
        val newsCall = newsService.listNews(
            version2,
            typeEverything,
            options
        )

        subscribeToApi(newsCall, false, subject)
    }

    private fun subscribeToApi(
        newsCall: Observable<News>,
        deleteOldArticles: Boolean,
        subject: Subject<ApiStatus>
    ) {
        if (!isNetworkConnected(RealmApplication.staticApplicationContext)) {
            subject.onNext(ApiStatus.NOINTERNET)
            return
        }
        val sub = newsCall
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ news ->
                Log.d(TAG, "onResponse: news ${news?.status}")
                subject.onNext(ApiStatus.SUCCESS)
                setRealmIDsAndAttributes(news)

                news?.articles?.let {
                    realm.executeTransactionAsync({ r ->
                        if (deleteOldArticles)
                            deleteOutdatedArticles(r)

                        val uniqueArticles = uniqueArticles(it, r)
                        r.insertOrUpdate(uniqueArticles)
                    }, { // onSuccess
                        Log.d(TAG, "onSuccess: realm executed successfully")

                    }, { onError ->
                        Log.e(TAG, "onError: realm failed -- ", onError)
                    })

                }
            }, { error ->
                if (error is HttpException) {
                    val errorBody = error.response()?.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    Log.d(TAG, "subscribeToApi: code = ${jObjError.get("code")}")
                    if (jObjError.get("code") == "rateLimited") {
                        subject.onNext(ApiStatus.RATELIMIT)
                    } else if (jObjError.get("code") == "maximumResultsReached") {
                        subject.onNext(ApiStatus.MAXIMUMRESULTS)
                    }
                    else {
                        subject.onNext(ApiStatus.FAILED)
                    }
                }

            })
        disposables.add(sub)
    }

    /**
     * Maintain database small
     * No need to have more than 140 articles stored
     */
    private fun deleteOutdatedArticles(r: Realm) {
        val allArticles = r.where(Article::class.java).findAll()
        if (allArticles.size > 140) {
            // don't delete hidden articles as the user probably doesn't want to see them again
            val oldArticles = r.where(Article::class.java).equalTo("hidden", false)
                    .or().isNull("hidden").sort("publishedAt", Sort.ASCENDING).limit(20).findAll()
            oldArticles.deleteAllFromRealm()
        }
    }

    /**
     * Setting IDs to realm objects.
     * Setting the attributes [Article.pinned] and [Article.hidden] to false as
     * if the item exists it won't be inserted
     * therefore won't affect the user's preferences
     * but if they do get inserted they will have value for comparisons.
     */
    private fun setRealmIDsAndAttributes(news: News?) {
        news?.articles?.forEach {
            it.id = it.url.hashCode().toString()
            it.source?.realmId = UUID.randomUUID().toString()
            it.pinned = false
            it.hidden = false
        }
    }

    /**
     * Returns the unique articles from list [articles] parameter
     */
    private fun uniqueArticles(articles: List<Article>, r: Realm): List<Article> {
        val allArticles = r.where(Article::class.java).findAll()
        val uniqueArticles = mutableListOf<Article>()
        for(article in articles) {
            if (article.id !in allArticles.map { it.id }) uniqueArticles.add(article)
        }

        return uniqueArticles
    }

}