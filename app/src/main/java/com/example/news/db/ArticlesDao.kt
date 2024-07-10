package com.example.news.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.news.models.Article

@Dao
interface ArticlesDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long
    @Query("select * from articles")
     fun getAllArticles():LiveData<List<Article>>
     @Delete
     suspend fun deleteArticle (article: Article)
}