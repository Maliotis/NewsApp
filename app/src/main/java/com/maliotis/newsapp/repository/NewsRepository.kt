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
        val newsCall = newsService.listNews(
            version2,
            typeEverything,
            options
        )

        // delete old articles when fetching new ones
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
                setRealmIDs(news)

                news?.articles?.let {
                    realm.executeTransactionAsync({ r ->
                        if (deleteOldArticles)
                            deleteOutdatedArticles(r)

                        r.insertOrUpdate(it)
                    }, { // onSuccess
                        Log.d(TAG, "onSuccess: realm executed successfully")
                        realm.close()

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

            }, {
                //onComplete
                Log.d(TAG, "subscribeToApi: onComplete called")
            }, {
                // disposing
                Log.d(TAG, "subscribeToApi: disposing")
                //it.dispose()
            })
        disposables.add(sub)
    }

    private fun deleteOutdatedArticles(r: Realm) {
        Log.d(TAG, "deleteOutdatedArticles: delete called")
        val oldArticles = r.where(Article::class.java).sort("publishedAt", Sort.ASCENDING).limit(20).findAll()
        Log.d(TAG, "deleteOutdatedArticles: oldArticles.size = ${oldArticles.size}")
        Log.d(TAG, "deleteOutdatedArticles: oldArticles = $oldArticles")
        //oldArticles.deleteAllFromRealm()

//        if (allArticles.size >= 100) {
//            // then delete at least as much as we are adding in
//            // delete the most outdated
//            val endIndex = news.articles?.size ?: 0
//            val subArticles = allArticles.subList(0, endIndex)
//            subArticles.forEach {
//                it.deleteFromRealm()
//                Log.d(TAG, "deleteOutdatedArticles: item deleted")
//            }
//        }
    }

    private fun setRealmIDs(news: News?) {
        news?.articles?.forEach {
            it.id = it.url.hashCode().toString()
            it.source?.realmId = UUID.randomUUID().toString()
        }
    }

}