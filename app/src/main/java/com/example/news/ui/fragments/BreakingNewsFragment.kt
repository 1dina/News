package com.example.news.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentBreakingNewsBinding
import com.example.news.databinding.FragmentBreakingNewsBinding.*
import com.example.news.ui.MainActivity
import com.example.news.ui.NewsViewModel
import com.example.news.util.Constant.Companion.QUERY_PAGE_SIZE
import com.example.news.util.Resource

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var binding: FragmentBreakingNewsBinding
    lateinit var adapterNews: NewsAdapter
    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    val TAG = "BreakingNewsError"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        prepareRecycleView()
        adapterNews.setOnItemSelected {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        adapterNews.differ.submitList(it.articles.toList())
                        val totalPages = it.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingPages == totalPages
                        if (isLastPage) binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                    }
                }

                else -> {
                    hideProgressBar()
                    response.message?.let { Toast.makeText(activity,
                        "An error occurred :$it",Toast.LENGTH_LONG).show()}
                }
            }

        })
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            // to check if the user scrolling
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) isScrolling =
                true

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
            val countVisibleItems = layoutManager.childCount
            val totalItems = layoutManager.itemCount
            val isAtLastItem = firstVisibleItem + countVisibleItems >= totalItems
            val isNotLoadingAndIsNotLastPage = !isLastPage && !isLoading
            val isNotAtBeginning = firstVisibleItem >= 0
            val isTotalMoreThanVisible = totalItems >= QUERY_PAGE_SIZE
            val shouldPaginate = isAtLastItem && isTotalMoreThanVisible && isScrolling
                    && isNotAtBeginning && isNotLoadingAndIsNotLastPage
            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
                isScrolling = false
            }


        }
    }

    fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true

    }

    fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false

    }

    fun prepareRecycleView() {
        adapterNews = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = adapterNews
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }


    }
}