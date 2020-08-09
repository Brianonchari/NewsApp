package co.studycode.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import co.studycode.newsapp.NewsApp
import co.studycode.newsapp.models.Article
import co.studycode.newsapp.models.NewsResponse
import co.studycode.newsapp.repositories.NewsRepository
import co.studycode.newsapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    val newsRepository: NewsRepository, app: Application
) : AndroidViewModel(app) {
    val TAG = "NewsViewModel"
    val breakinNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val businessNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var topHeadLinesPage = 1
    var searchNewsPage = 1
    var businessNewsPage = 1
    var businessNewsResponse:NewsResponse? = null
    var topHeadLinesResponse: NewsResponse? = null
    var searchNewsResponse: NewsResponse? = null

    init {
        getTopHeadLines("us")
        getBusinessNews("sports")
    }

    fun getTopHeadLines(countryCode: String) = viewModelScope.launch {
        safeTopHeadLinesNewsCall(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    fun getBusinessNews(newsCategory: String) = viewModelScope.launch {
        safeBusinessNewsCall(newsCategory)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                topHeadLinesPage++
                if (topHeadLinesResponse == null) {
                    topHeadLinesResponse = resultResponse
                } else {
                    val oldArticles = topHeadLinesResponse?.articles
                    val newArticles = resultResponse.articles

                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(topHeadLinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleBusinessNews(response: Response<NewsResponse>):Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let {resultResponse->
                businessNewsPage++
                if(businessNewsResponse==null){
                    businessNewsResponse = resultResponse
                }else{
                    val oldArticles = businessNewsResponse?.articles
                    val newArticles = resultResponse.articles

                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(businessNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles

                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    //Room Database Operations
    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    //Fetch Data From Repository
    suspend fun safeTopHeadLinesNewsCall(countryCode: String) {
        breakinNews.postValue(Resource.Loading())
        try {
            if(hasConnection()) {
                val response = newsRepository.getTopHeadLines(countryCode, topHeadLinesPage)
                breakinNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakinNews.postValue(Resource.Error("You have no Internet connection"))
            }
        } catch (t: Throwable) {
            Log.d(TAG, t.message.toString())
            when(t){
                is IOException -> breakinNews.postValue(Resource.Error("Network failure"))
                else -> breakinNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if(hasConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("You have no Internet connection"))
            }
        } catch (t: Throwable) {
            Log.d(TAG, t.message.toString())
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    suspend fun safeBusinessNewsCall(newsCategory: String){
        businessNews.postValue(Resource.Loading())
        try {
            if(hasConnection()){
                val response = newsRepository.getBusinessNews(newsCategory, businessNewsPage)
                businessNews.postValue(handleBusinessNews(response))
            }else{
                businessNews.postValue(Resource.Error("You have no active internet network"))
            }
        }catch (t:Throwable){
            Log.d(TAG, "safeBusinessNewsCall: ${t.message}")
            when(t){
                is IOException -> businessNews.postValue(Resource.Error("Network Failure"))
                else -> businessNews.postValue(Resource.Error("Conversion Error"))
            }
        }

    }

    // Check for Internet Connection
    private fun hasConnection(): Boolean {
        val connectivityManager = getApplication<NewsApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}