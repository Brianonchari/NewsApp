package co.studycode.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
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
import co.studycode.newsapp.utils.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_breaking_news.paginationProgressBar
import kotlinx.android.synthetic.main.fragment_business_news.*

class BusinessNewsFragment : Fragment(R.layout.fragment_business_news) {
    private val TAG = "BusinessNewsFragment"
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_businessNewsFragment_to_articlesFragment,
                bundle
            )
        }
        viewModel.breakinNews.observe(viewLifecycleOwner, Observer {response ->
            when(response){
                is Resource.Success ->{
                    hideProgressBar()
                    response.data.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse?.articles?.toList())
                        val totalPagesResult = newsResponse!!.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.topHeadLinesPage == totalPagesResult
                        if(isLastPage){
                            rvBusinessNews.setPadding(0,0,0,0)
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


    //Pagination and Scroll Behaviour
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
                viewModel.getTopHeadLines("us")
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

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBusinessNews.setHasFixedSize(true)
        rvBusinessNews.apply {
            adapter = newsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(this@BusinessNewsFragment.scrollListener)
        }
    }

}