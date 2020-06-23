package co.studycode.newsapp.repositories

import co.studycode.newsapp.db.AppDatabase
import co.studycode.newsapp.models.Article
import co.studycode.newsapp.service.NewsApiInstance

class NewsRepository(val db:AppDatabase) {

    suspend fun getTopHeadLines(countryCode:String, pageNumber:Int) = NewsApiInstance.api.getBreakingNews(countryCode,pageNumber)
    suspend fun searchNews(searchQuery:String, pageNumber: Int) = NewsApiInstance.api.searchNews(searchQuery,pageNumber)
    suspend fun upsert(article: Article) = db.getArticlesDao().upsert(article)

    fun getSavedNews() = db.getArticlesDao().getArticles()

    suspend fun  deleteArticle(article: Article) = db.getArticlesDao().deleteArticle(article)

}