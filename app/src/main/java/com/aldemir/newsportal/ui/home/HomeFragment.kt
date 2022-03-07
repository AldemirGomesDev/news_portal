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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.FragmentHomeBinding
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.ui.detail.DetailActivity
import com.aldemir.newsportal.util.Constants
import com.aldemir.newsportal.util.Status
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
    private val mNews: ArrayList<New> = arrayListOf()
    private var mNewsCarousel: List<New> = arrayListOf()
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

        homeViewModel.getTotalNews(Constants.NEW)
        homeViewModel.getTotalNews(Constants.NEW_HIGH_LIGHT)
        homeViewModel.getLastPage()
        homeViewModel.getTotalPages()
        homeViewModel.getNewsDatabase()
        homeViewModel.getNewsHighLight(true)

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
                        homeViewModel.getLastPage()
                        homeViewModel.getTotalPages()
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
                homeViewModel.getLastPage()
                homeViewModel.getTotalPages()
                homeViewModel.getNewsDatabase()
                homeViewModel.getNewsHighLight(true)
            }
        }
    }

    private fun renderList(list: List<New>) {
        list.sortedByDescending { it.published_at }
        mNews.addAll(list)
        homeAdapterNews.submitList(list)
    }

    private fun renderListHorizontal(list: List<New>) {
        mNewsCarousel = list
        adapterNewsHighlights.addData(list)
    }

    private fun showNews() {
        binding.loadingNews.visibility = View.GONE
        binding.recyclerViewNews.visibility = View.VISIBLE
        binding.recyclerViewNewsHighlights.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.loadingNews.visibility = View.VISIBLE
        binding.recyclerViewNews.visibility = View.GONE
        binding.recyclerViewNewsHighlights.visibility = View.GONE
    }

    private fun observers() {
        homeViewModel.newsHighlights.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    showNews()
                    if (it.data!!.size >= mTotalNewsHighlights) {
                        homeViewModel.getNewsHighLight(true)
                    }
                }
                Status.LOADING -> {
                    binding.loadingNews.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    showNews()
                    Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        homeViewModel.news.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    showNews()
                    homeViewModel.getLastPage()
                    homeViewModel.getTotalPages()
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                    showNews()
                }
            }
        })

        homeViewModel.totalNewsHighLight.observe(viewLifecycleOwner, Observer { totalNews ->
            mTotalNewsHighlights = totalNews
        })

        homeViewModel.totalNews.observe(viewLifecycleOwner, Observer { totalNews ->
            mTotalNews = totalNews
        })

        homeViewModel.lastPage.observe(viewLifecycleOwner, Observer { lastPage ->
            mLastPage = lastPage
        })

        homeViewModel.totalPages.observe(viewLifecycleOwner, Observer { totalPages ->
            mTotalPages = totalPages
            if (mLastPage <= mTotalPages) {
                homeViewModel.getAllNews(mLastPage, mPerPage, mTotalNews)
            }
        })
        homeViewModel.newsDatabase.observe(viewLifecycleOwner, Observer { news ->
            homeViewModel.getTotalNews(Constants.NEW)
            showNews()
            renderList(news)
        })

        homeViewModel.newsHighLight.observe(viewLifecycleOwner, Observer { news ->
            homeViewModel.getTotalNews(Constants.NEW_HIGH_LIGHT)
            showNews()
            renderListHorizontal(news)
            homeViewModel.getAllNewsHighlights(mTotalNewsHighlights)
        })
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

    override fun onClickNew(position: Int, aView: View) {
        val intent = DetailActivity.newIntent(mContext, mNews[position].url!!)
        startActivity(intent)
    }

    override fun onClickFavorite(position: Int, aView: View) {
        mNews[position].is_favorite = !mNews[position].is_favorite
        if (mNews[position].is_favorite) {
            homeViewModel.addNewsFavorite(mNews[position])
        } else {
            homeViewModel.removeNewsFavorite(mNews[position])
        }
        homeAdapterNews.notifyItemChanged(position)
    }

    override fun onClickShared(position: Int, aView: View) {
        val shareTask = mNews[position].url!!
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

    override fun onClickCarousel(position: Int, aView: View) {
        val intent = DetailActivity.newIntent(mContext, mNews[position].url!!)
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