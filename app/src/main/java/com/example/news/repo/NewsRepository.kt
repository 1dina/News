package com.example.news.repo

import com.example.news.api.RetrofitInstance
import com.example.news.db.ArticleDatabase
import com.example.news.models.Article


class NewsRepository (  val db: ArticleDatabase){
    suspend fun getBreakingNews(countryCode :String , pageNumber: Int) =
   RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)
    suspend fun getSearchNews (searchWord : String , pageNumber: Int) =
        RetrofitInstance.api.getSearchNews(searchWord,pageNumber)
    suspend fun upsert (article: Article ) =db.articleDao().upsert(article)
    fun getAllArticles ()= db.articleDao().getAllArticles()
    suspend fun deleteArticle(article: Article) =db.articleDao().deleteArticle(article)


}