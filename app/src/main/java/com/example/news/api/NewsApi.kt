package com.example.news.api

import com.example.news.models.NewsResponse
import com.example.news.util.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("/v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String = "eg",
        @Query("page") pageNum : Int =1 ,
        @Query("apiKey") apiKey : String = API_KEY) :Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun getSearchNews(
        @Query("q") searchWord: String,
        @Query("page") pageNum : Int =1,
        @Query("apiKey") apiKey : String = API_KEY) : Response<NewsResponse>


}