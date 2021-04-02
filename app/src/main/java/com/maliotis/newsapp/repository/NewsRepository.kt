package com.maliotis.newsapp.repository

import android.util.Log
import com.maliotis.newsapp.RealmUtil.realm
import com.maliotis.newsapp.model.News
import com.maliotis.newsapp.repository.realm.Article
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.newsService
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.options
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.typeEverything
import com.maliotis.newsapp.repository.retrofit.RetrofitCreator.version2
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class NewsRepository {
    val TAG = NewsRepository::class.java.simpleName
    var articles: MutableList<Article>? = null

    fun getNews() {
        val newsCall = newsService.listNews(
            version2,
            typeEverything,
            options
        )

        newsCall.enqueue(object : Callback<News> {
            override fun onResponse(
                call: Call<News>,
                response: Response<News>
            ) {
                if (response.isSuccessful) {
                    val news = response.body()
                    Log.d(TAG, "onResponse: news ${news?.articles?.get(0)?.content}")
                    setRealmIDs(news)

                    news?.articles?.let {
                        realm.executeTransactionAsync({ r ->
                            deleteOutdatedArticles(r, news)
                            // articles are now managed objects
                            articles = r.copyToRealmOrUpdate(it)
                        }, { // onSuccess
                            Log.d(TAG, "onSuccess: realm executed successfully")

                        }, { onError ->
                            Log.e(TAG, "onError: realm failed -- ", onError)
                        })

                    }

                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.e(TAG, "onFailure: t", t)
            }

        })
    }

    private fun deleteOutdatedArticles(r: Realm, news: News) {
        Log.d(TAG, "deleteOutdatedArticles: delete called")
        var allArticles = r.where(Article::class.java).sort("publishedAt", Sort.ASCENDING).findAll()

        if (allArticles.size >= 100) {
            // then delete at least as much as we are adding in
            // delete the most outdated
            val endIndex = news.articles?.size ?: 0
            val subArticles = allArticles.subList(0, endIndex)
            subArticles.forEach {
                it.deleteFromRealm()
                Log.d(TAG, "deleteOutdatedArticles: item deleted")
            }
        }
    }

    private fun setRealmIDs(news: News?) {
        news?.articles?.forEach {
            it.id = it.url.hashCode().toString()
            it.source?.realmId = UUID.randomUUID().toString()
        }
    }
}