package co.studycode.newsapp.ui.fragments

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import co.studycode.newsapp.R
import co.studycode.newsapp.ui.NewsActivity
import co.studycode.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_article.*


class ArticleFragment :Fragment(R.layout.fragment_article){
    lateinit var viewModel: NewsViewModel
    val args:ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as NewsActivity).viewModel
        val article = args.article

        webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url)
        }
//        val shareIntent: PendingIntent = PendingIntent.getActivity(
//////            context,
//////            0,
//////            Intent.createChooser(
//////                Intent(Intent.ACTION_SEND)
//////                    .setType("text/plain")
//////                    .putExtra(Intent.EXTRA_TEXT, article), "Share Note Reminder"
//////            ),
//////            PendingIntent.FLAG_UPDATE_CURRENT
//////        )



        share_fab.setOnClickListener{
            val shareIntent:Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, article)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "text"))
        }

        fab.setOnClickListener{
            viewModel.saveArticle(article)
            Snackbar.make(view,"Article saved ", Snackbar.LENGTH_SHORT).show()
        }
    }
}