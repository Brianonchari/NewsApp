package co.studycode.newsapp.models

import co.studycode.newsapp.models.Article
import java.io.Serializable

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
):Serializable