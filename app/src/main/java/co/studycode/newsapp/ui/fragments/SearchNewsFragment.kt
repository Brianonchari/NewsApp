package co.studycode.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.studycode.newsapp.R
import co.studycode.newsapp.adapters.NewsAdapter
import co.studycode.newsapp.ui.NewsActivity
import co.studycode.newsapp.ui.NewsViewModel
import co.studycode.newsapp.utils.Constants
import co.studycode.newsapp.utils.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import co.studycode.newsapp.utils.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment :Fragment(R.layout.fragment_search_news){
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter:NewsAdapter
    var job:Job? = null

    val TAG = "SearchNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as NewsActivity).viewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articlesFragment,
                bundle
            )
        }

        etSearch.addTextChangedListener {editable->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()){
                      viewModel.searchNews(editable.toString())                   }
                }
            }
        }
        viewModel.searchNews.observe(viewLifecycleOwner, Observer {response ->
            when(response){
                is Resource.Success ->{
                    hideProgressBar()
                    response.data.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse?.articles?.toList())
                        val totalPagesResult = newsResponse!!.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPagesResult
                        if(isLastPage){
                            rvBreakingNews.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error ->{
                    hideProgressBar()
                    response.message?.let { message->
                        Log.e(TAG,"An error occured $message")
                        Toast.makeText(context,"Error!! $message", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading->{
                    showProgressBar()
                }
            }

        })
    }

    //Add Pagination
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCounts = layoutManager.childCount
            val totalItemCounts = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItemPosition = firstVisibleItemPos + visibleItemCounts>= totalItemCounts
            val isNotAtBegginingPosition = firstVisibleItemPos>=0
            val isTotalItemsMoreThanVisible = totalItemCounts>= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItemPosition && isNotAtBegginingPosition && isTotalItemsMoreThanVisible
                    && isScrolling
            if(shouldPaginate){
                viewModel.searchNews(etSearch.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }
    }
    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(){
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView(){
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}