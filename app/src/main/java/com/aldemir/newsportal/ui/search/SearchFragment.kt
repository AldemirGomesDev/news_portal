package com.aldemir.newsportal.ui.search

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.FragmentSearchBinding
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.ui.detail.DetailActivity
import com.aldemir.newsportal.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class SearchFragment : Fragment(), SearchAdapter.ClickListener {

    companion object {
        private const val TAG = "SearchFragment"
    }
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var mContext: Context
    private lateinit var adapter: SearchAdapter
    private var mNews: List<New> = arrayListOf()
    private var mSearch: String = ""
    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observers()
        setupRecyclerView()

    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickNew(position: Int, aView: View) {
        val intent = DetailActivity.newIntent(mContext, mNews[position].url!!)
        startActivity(intent)
    }

    override fun onClickFavorite(position: Int, aView: View) {
        mNews[position].is_favorite = !mNews[position].is_favorite
        if (mNews[position].is_favorite) {
            searchViewModel.addNewsFavorite(mNews[position])
        } else {
            searchViewModel.removeNewsFavorite(mNews[position])
        }
        adapter.notifyItemChanged(position)
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

    private fun setupUI() {
        binding.containerSearch.setOnFocusChangeListener{_, _ ->
            hideKeyBoard()
        }

        binding.containerSearch.setOnClickListener {
            hideKeyBoard()
        }

        binding.autoCompleteNews.setEndIconOnClickListener {
            removeFilters()
        }

        binding.edtSearch.setOnFocusChangeListener {_, _ ->
            Log.w(TAG, "onFocusChangeListener")
            binding.edtSearch.isCursorVisible = true
            binding.edtSearch.isFocusableInTouchMode = true
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            val debouncePeriod: Long = 500
            var searchJob: Job? = null

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(search: CharSequence?, start: Int, before: Int, count: Int) {
                mSearch = search.toString()
                if (search!!.isNotEmpty()) {
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(debouncePeriod)
                        mSearch = search.toString()
                        applySearch()
                    }
                }
            }
        })

        binding.checkBoxFavorite.setOnCheckedChangeListener { _, isChecked ->
            isFavorite = isChecked
            applySearch()
        }

    }

    private fun observers() {
        searchViewModel.newsDatabase.observe(viewLifecycleOwner, androidx.lifecycle.Observer { news ->
            if (news.isNotEmpty()){
                renderList(news)
                binding.recyclerViewNewsFilters.visibility = View.VISIBLE
            }
            else {
                binding.recyclerViewNewsFilters.visibility = View.GONE
            }
        })
    }

    private fun hideKeyBoard() {
        binding.edtSearch.clearFocus()
        try {
            val mImm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            mImm.hideSoftInputFromWindow(view?.windowToken, 0)
            mImm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }catch (err: Exception) {
            Log.e(TAG, "hideKeyboard Error: $err")
        }
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

    private fun applySearch() {
        if (mSearch.isNotEmpty()) {
            searchViewModel.getNewsFilter(mSearch, isFavorite)
        }
    }

    private fun removeFilters() {
        mSearch = ""
        isFavorite = false
        binding.edtSearch.setText("")
        binding.checkBoxFavorite.isChecked = false
        renderList(arrayListOf())
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewNewsFilters.layoutManager = layoutManager
        adapter = SearchAdapter()
        binding.recyclerViewNewsFilters.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerViewNewsFilters.context,
                (binding.recyclerViewNewsFilters.layoutManager as LinearLayoutManager).orientation
            )
        )
        binding.recyclerViewNewsFilters.adapter = adapter
        adapter.setOnItemClickListener(this)
        adapter.setOnItemClickListenerFavorite(this)
        adapter.setOnItemClickListenerShared(this)
    }

    private fun renderList(list: List<New>) {
        list.sortedByDescending { it.published_at }
        mNews = list
        adapter.submitList(list)
    }
}