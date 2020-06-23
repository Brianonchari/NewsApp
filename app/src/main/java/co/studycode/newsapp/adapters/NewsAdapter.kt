package co.studycode.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.studycode.newsapp.R
import co.studycode.newsapp.models.Article
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_article_preview.view.*

class NewsAdapter :RecyclerView.Adapter<NewsAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View ):RecyclerView.ViewHolder(itemView)
    private val differCallback= object :DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url ==newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem==newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article_preview,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
      return differ.currentList.size
    }
    private var onItemClickListener:((Article)->Unit)? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val article = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(article.urlToImage).into(ivArticleImage)
            tvSource.text = article.source?.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt
            author.text    = article.author
            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }


    fun setOnItemClickListener(listener:(Article) -> Unit){
        onItemClickListener = listener
    }
}