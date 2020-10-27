package co.studycode.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.studycode.newsapp.R
import co.studycode.newsapp.adapters.NewsAdapter
import co.studycode.newsapp.ui.NewsActivity
import co.studycode.newsapp.ui.NewsViewModel
import co.studycode.newsapp.utils.Constants.Companion.QUERY_PAGE_SIZE
import co.studycode.newsapp.utils.EndlessScrollListener
import co.studycode.newsapp.utils.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    val TAG = "BreakingNewsFragment"

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articlesFragment,
                bundle
            )
        }
        viewModel.breakinNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse?.articles?.toList())
                        val totalPagesResult = newsResponse!!.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.topHeadLinesPage == totalPagesResult
                        if (isLastPage) {
                            rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occured $message")
                        Toast.makeText(context, "Error!! $message", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
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
        rvBreakingNews.setHasFixedSize(true)
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            val scrollListener = object : EndlessScrollListener() {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    viewModel.getTopHeadLines("us")
                }
            }
            addOnScrollListener(scrollListener)
        }
    }
}