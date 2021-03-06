package co.studycode.newsapp.service

import co.studycode.newsapp.models.NewsResponse
import co.studycode.newsapp.utils.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/top-headlines")
    suspend fun getBusinessNews(
        @Query("category")
        businessCategory: String="sports",
        @Query("page")
        pageNumber: Int=1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ):Response<NewsResponse>
}