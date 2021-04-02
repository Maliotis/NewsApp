package com.maliotis.newsapp.repository.retrofit

import com.maliotis.newsapp.model.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface NewsService {

    @GET("{version}/{type}")
    fun listNews(
        @Path("version") version: String,
        @Path("type") type: String,
        @QueryMap options: Map<String, String>): Call<News>
}