package com.aldemir.newsportal.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.FragmentHomeBinding
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.ui.detail.DetailActivity
import com.aldemir.newsportal.util.Constants
import com.aldemir.newsportal.util.Resource
import com.aldemir.newsportal.util.Status
import com.aldemir.newsportal.util.onEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class HomeFragment : Fragment(),
    HomeAdapterNews.ClickListener,
    HomeAdapterNewsHighlights.ClickListener {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var adapterNewsHighlights: HomeAdapterNewsHighlights
    private lateinit var homeAdapterNews: HomeAdapterNews
    private lateinit var mContext: Context
    private lateinit var myFixedRateTimer: Timer
    private var mLastPage: Int = 1
    private var mPerPage: Int = 10
    private var mTotalPages: Int = 1
    private var mTotalNews: Int = 0
    private var mTotalNewsHighlights: Int = 0

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fetchTotalNews()
        fetchNumberOfPages()
        fetchDatabaseNews()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observers()
        setupUi()
    }

    private fun setupUi() {
        homeAdapterNews = HomeAdapterNews()
        homeAdapterNews.submitList(arrayListOf())

        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewNews.layoutManager = layoutManager
        binding.recyclerViewNews.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerViewNews.context,
                (binding.recyclerViewNews.layoutManager as LinearLayoutManager).orientation
            )
        )
        binding.recyclerViewNews.adapter = homeAdapterNews
        endlessScrollingSearch(layoutManager)
        homeAdapterNews.setOnItemClickListener(this)
        homeAdapterNews.setOnItemClickListenerFavorite(this)
        homeAdapterNews.setOnItemClickListenerShared(this)

        //==========recyclerview horizontal=================
        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewNewsHighlights.layoutManager = horizontalLayoutManager
        adapterNewsHighlights =
            HomeAdapterNewsHighlights(
                arrayListOf()
            )
        binding.recyclerViewNewsHighlights.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerViewNewsHighlights.context,
                (binding.recyclerViewNewsHighlights.layoutManager as LinearLayoutManager).orientation
            )
        )
        binding.recyclerViewNewsHighlights.adapter = adapterNewsHighlights
        adapterNewsHighlights.setOnItemClickListener(this)
    }

    private fun endlessScrollingSearch(layoutManager: LinearLayoutManager) {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val debouncePeriod: Long = 400
        var searchJob: Job? = null

        binding.recyclerViewNews.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val currentItems = layoutManager.childCount
                val scrollOutItems = layoutManager.findFirstVisibleItemPosition()
                val totalItems = layoutManager.itemCount

                if (currentItems + scrollOutItems >= totalItems) {
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(debouncePeriod)
                        fetchNumberOfPages()
                        homeViewModel.getAllNews(mLastPage, mPerPage, mTotalNews)
                    }
                }
            }
        })
    }

    private fun updateNews() {
        myFixedRateTimer = fixedRateTimer(
            Constants.FIXED_RATE_TIMER,
            false,
            Constants.INITIAL_DELAY,
            Constants.PERIOD_DELAY
        ) {
            Log.w(TAG, "update news")
            requireActivity().runOnUiThread {
                fetchNumberOfPages()
                fetchDatabaseNews()
            }
        }
    }

    private fun renderList(list: List<New>) {
        list.sortedByDescending { it.is_favorite }
        homeAdapterNews.submitList(list)
    }

    private fun renderListHorizontal(list: List<New>) {
        adapterNewsHighlights.addData(list)
    }

    private fun showNews() {
        binding.loadingNews.visibility = View.GONE
        binding.recyclerViewNews.visibility = View.VISIBLE
        binding.recyclerViewNewsHighlights.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.loadingNews.visibility = View.VISIBLE
    }

    private fun showToastError(errorMessage: String) {
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun fetchNumberOfPages() {
        homeViewModel.getLastPage()
        homeViewModel.getTotalPages()
    }

    private fun fetchDatabaseNews() {
        homeViewModel.getDatabaseNews()
        homeViewModel.getDatabaseNewsHighLight(true)
    }

    private fun fetchTotalNews() {
        homeViewModel.getTotalNews(Constants.NEW)
        homeViewModel.getTotalNews(Constants.NEW_HIGH_LIGHT)
    }

    private fun observers() {
        onEvent(homeViewModel.newsHighlights) { handleNewsHighLights(it) }
        onEvent(homeViewModel.totalNewsHighLight) { mTotalNewsHighlights = it }
        onEvent(homeViewModel.totalNews) { mTotalNews = it }
        onEvent(homeViewModel.lastPage) { mLastPage = it }
        onEvent(homeViewModel.totalPages) { verifyIfLastPage(it) }
        onEvent(homeViewModel.news) { handleNewsApi(it) }
        homeViewModel.newsDatabase.observe(viewLifecycleOwner) { handleNewsDatabase(it) }
        homeViewModel.newsHighLight.observe(viewLifecycleOwner) { handleNewsHighLightDatabase(it) }
    }

    private fun handleNewsHighLights(news: List<New>) {
        if (news.size >= mTotalNewsHighlights) {
            homeViewModel.getDatabaseNewsHighLight(true)
        }
    }

    private fun handleNewsApi(resource: Resource<List<New>>) {
        when(resource.status) {
            Status.SUCCESS -> {
                showNews()
                fetchNumberOfPages()
            }
            Status.LOADING -> {
                showLoading()
            }
            Status.ERROR -> {
                showToastError(resource.message!!)
                showNews()
            }
        }
    }

    private fun handleNewsDatabase(news: List<New>) {
        fetchTotalNews()
        showNews()
        renderList(news)
    }

    private fun handleNewsHighLightDatabase(news: List<New>) {
        homeViewModel.getTotalNews(Constants.NEW_HIGH_LIGHT)
        showNews()
        renderListHorizontal(news)
        homeViewModel.getAllNewsHighlights(mTotalNewsHighlights)
    }

    private fun verifyIfLastPage(totalPages: Int) {
        mTotalPages = totalPages
        if (mLastPage <= mTotalPages) {
            homeViewModel.getAllNews(mLastPage, mPerPage, mTotalNews)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onResume() {
        super.onResume()
        updateNews()
    }

    override fun onStop() {
        super.onStop()
        myFixedRateTimer.cancel()
    }

    override fun onClickNew(new: New, position: Int, aView: View) {
        val intent = DetailActivity.newIntent(mContext, new.url!!)
        startActivity(intent)
    }

    override fun onClickFavorite(new: New, position: Int, aView: View) {
        new.is_favorite = !new.is_favorite
        homeViewModel.updateFavoriteNews(new = new)
        homeAdapterNews.notifyItemChanged(position)
    }

    override fun onClickShared(new: New, position: Int, aView: View) {
        val shareTask = new.url!!
        val dialog =
            AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.dialog_share_title))
                .setMessage(mContext.getString(R.string.dialog_share_message))
                .setPositiveButton(mContext.getString(R.string.alert_button_confirm)) { dialog, _ ->
                    setShareIntent(shareTask(shareTask))
                    dialog.dismiss()
                }
                .setNegativeButton(mContext.getString(R.string.alert_button_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
        dialog.show()
    }

    override fun onClickCarousel(new: New, position: Int, aView: View) {
        val intent = DetailActivity.newIntent(mContext, new.url!!)
        startActivity(intent)
    }

    private fun setShareIntent(shareBody: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = Constants.INTENT_CONTENT_TYPE
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, mContext.getString(R.string.show_new)))
    }

    private fun shareTask(str: String): String {
        return "${Constants.DIALOG_SHARE_MESSAGE}\n" + str + ""
    }
}