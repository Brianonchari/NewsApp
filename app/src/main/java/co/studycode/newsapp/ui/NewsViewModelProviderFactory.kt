package co.studycode.newsapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.studycode.newsapp.NewsApp
import co.studycode.newsapp.repositories.NewsRepository

class NewsViewModelProviderFactory(
    val newsRepository: NewsRepository,
    val app:Application
):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository,app ) as T
    }
}