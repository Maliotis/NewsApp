package com.maliotis.newsapp.repository.retrofit

import com.maliotis.newsapp.Keys
import io.realm.rx.RxObservableFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitCreator {
    var API_BASE_URL = "https://newsapi.org/"
    var version2 = "v2"
    var typeEverything = "everything"
    var typeTopHeadlines = "top-headlines"
    var page = 1

    var options = mutableMapOf<String, String>("q" to "entertainment",

        "apiKey" to Keys.apiKey())

    var httpClient = OkHttpClient.Builder()

    var builder = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

    var retrofit = builder
        .client(
            httpClient.build()
        )
        .build()

    var newsService: NewsService = retrofit.create(NewsService::class.java)
}