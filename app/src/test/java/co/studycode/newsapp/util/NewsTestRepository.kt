package co.studycode.newsapp.util

import co.studycode.newsapp.db.AppDatabase
import co.studycode.newsapp.models.NewsResponse
import co.studycode.newsapp.repositories.NewsRepository

class NewsTestRepository(private val response:ArrayList<NewsResponse>, db: AppDatabase): NewsRepository(db) {

}