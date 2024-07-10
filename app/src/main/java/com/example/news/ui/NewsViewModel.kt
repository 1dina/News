package com.example.news.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.example.news.NewsApplication
import com.example.news.models.Article
import com.example.news.models.NewsResponse
import com.example.news.repo.NewsRepository
import com.example.news.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    val app: Application, val repository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingPages = 1
    var breakingNewsResponse: NewsResponse? = null
    val searchingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchingPage = 1
    var searchingNewsResponse: NewsResponse? = null


    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeGetBreakingNews(countryCode)

    }

    fun getSearchingNews(searchWord: String) = viewModelScope.launch {
      safeGetSearchingNews(searchWord)

    }

    fun upsertArticle(article: Article) = viewModelScope.launch {
        repository.upsert(article)
    }

    fun getAllArticles() = repository.getAllArticles()
    fun deleteArticle(article: Article) = viewModelScope.launch {
        repository.deleteArticle(article)

    }

    suspend fun safeGetBreakingNews(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (isThereInternetConnection()) {
                val response = repository.getBreakingNews(countryCode, breakingPages)
                breakingNews.postValue(checkTheRequestForBreaking(response))
            } else {
                breakingNews.postValue(Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }

        }
    }
    suspend fun safeGetSearchingNews (searchWord : String){
        searchingNews.postValue(Resource.Loading())
        try {
            if(isThereInternetConnection()){
                val response= repository.getSearchNews(searchWord,searchingPage)
                searchingNews.postValue(checkTheRequestForSearching(response))
            } else {
                searchingNews.postValue(Resource.Error("No Internet Connection"))
            }

        }catch (t : Throwable){
            when (t) {
                is IOException -> searchingNews.postValue(Resource.Error("Network Failure"))
                else -> searchingNews.postValue(Resource.Error("Conversion Error"))
            }

        }


    }



    fun checkTheRequestForBreaking(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful)
            response.body()?.let { result ->
                breakingPages++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = result
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(newArticles)

                }
                return Resource.Success(breakingNewsResponse ?: result)
            }
        return Resource.Error(response.message())


    }

    fun checkTheRequestForSearching(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful)
            response.body()?.let { result ->
                searchingPage++
                if (searchingNewsResponse == null) {
                    searchingNewsResponse = result
                } else {
                    val oldArticles = searchingNewsResponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(newArticles)

                }
                return Resource.Success(searchingNewsResponse ?: result)
            }
        return Resource.Error(response.message())

    }

    fun isThereInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> {
                return false
            }
        }
    }
}