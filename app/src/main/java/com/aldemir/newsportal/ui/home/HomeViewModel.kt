package com.aldemir.newsportal.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemir.newsportal.api.SessionManager
import com.aldemir.newsportal.api.models.ResponseNew
import com.aldemir.newsportal.api.models.ResponseNewHighlights
import com.aldemir.newsportal.data.repository.NewRepository
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.util.Constants
import com.aldemir.newsportal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val newsRepository: NewRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _lastPage = MutableLiveData<Int>()
    val lastPage: LiveData<Int> = _lastPage

    private val _totalPages = MutableLiveData<Int>()
    val totalPages: LiveData<Int> = _totalPages

    private val _totalNews = MutableLiveData<Int>()
    val totalNews: LiveData<Int> = _totalNews

    private val _totalNewsHighLight = MutableLiveData<Int>()
    val totalNewsHighLight: LiveData<Int> = _totalNewsHighLight

    private val _newsDatabase = MutableLiveData<List<New>>()
    var newsDatabase: LiveData<List<New>> = _newsDatabase

    private val _newsHighLight = MutableLiveData<List<New>>()
    var newsHighLight: LiveData<List<New>> = _newsHighLight


    private val _news = MutableLiveData<Resource<List<New>>>((Resource.loading(null)))
    var news: LiveData<Resource<List<New>>> = _news

    private val _newsHighlights = MutableLiveData<Resource<List<New>>>((Resource.loading(null)))
    var newsHighlights: LiveData<Resource<List<New>>> = _newsHighlights

    private val _res = MutableLiveData<Resource<ResponseNewHighlights>>()

    val res : LiveData<Resource<ResponseNewHighlights>>
        get() = _res


    fun getNewsDatabase() {
        try {
            viewModelScope.launch {
                newsDatabase = newsRepository.getNewsDatabase()
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    fun getNewsHighLight(highlight: Boolean) {
        try {
            viewModelScope.launch {
                _newsHighLight.value = newsRepository.getHighLight(highlight)
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    fun getAllNews(lastPage: Int, perPage: Int, countNews: Int) {
        viewModelScope.launch {
            try {
                val responseNew: ResponseNew = newsRepository.getAllNews(lastPage, perPage)
                formatNew(responseNew = responseNew, countNews = countNews, lastPage = lastPage)
            } catch (error: Exception) {
                Log.e(TAG, "ERROR getAllNews  => : ${error}")
                _totalNews.value = 0
            }

        }
    }

    fun getAllNewsHighlights(count: Int) = viewModelScope.launch {
        _newsHighlights.postValue(Resource.loading(null))
        val mNews: ArrayList<New> = arrayListOf()
        try {
            newsRepository.getAllNewsHighlights().let {
                if (it.isSuccessful){
                    val result = it.body()!!.data
                    mNews.addAll(formatNewHighlights(responseNew = result, count = count))
                    _newsHighlights.postValue(Resource.success(mNews))
                    saveTotalNews(result.size, Constants.NEW_HIGH_LIGHT)
                }else{
                    _newsHighlights.postValue(Resource.error(it.errorBody().toString(), null))
                }
                if (mNews.size > count) {
                    insertNews(mNews)
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR => : ${error}")
            _newsHighlights.value = (Resource.error("Sem conexão, tente novamente", null))
        }

    }

    fun addNewsFavorite(new: New) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.updateNew(new)
        }
    }

    fun removeNewsFavorite(new: New) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.updateNew(new)
        }
    }

    private fun saveLastPage(lastPage: Int) {
        sessionManager.saveLastPage(lastPage + 1)
    }

    private fun saveTotalPages(totalPages: Int) {
        sessionManager.saveTotalPages(totalPages)
    }

    private fun saveTotalNews(total: Int, tag: String) {
        sessionManager.saveTotalNews(total, tag)
    }

    fun getLastPage() {
        _lastPage.value = sessionManager.getLastPage()
    }

    fun getTotalPages() {
        _totalPages.value = sessionManager.getTotalPages()
    }

    fun getTotalNews(tag: String) {
        val totalNews = sessionManager.getTotalNews(tag)
        if (tag == "news") {
            _totalNews.value = totalNews
        } else {
            _totalNewsHighLight.value = totalNews
        }
    }

    private fun insertNews(news: ArrayList<New>) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                newsRepository.insertNews(news)
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    private fun getDateFormatted(dateOriginal: String): Date? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return inputFormat.parse(dateOriginal)

    }

    private fun formatNewHighlights(responseNew: List<ResponseNewHighlights.Data>, count: Int): ArrayList<New> {
        val mNews: ArrayList<New> = arrayListOf()
        if (responseNew.size > count) {
            for (new in responseNew) {
                val mNew = New()
                mNew.author = new.author
                mNew.content = new.content
                mNew.description = new.description
                mNew.highlight = new.highlight
                mNew.image_url = new.image_url
                mNew.published_at = getDateFormatted(new.published_at)
                mNew.title = new.title
                mNew.url = new.url
                mNews.add(mNew)
            }
        }
        return mNews
    }

    private fun formatNew(responseNew: ResponseNew, countNews: Int, lastPage: Int,): ArrayList<New> {
        val mNews: ArrayList<New> = arrayListOf()
        if (responseNew.pagination.total_items > countNews) {
            for (new in responseNew.data) {
                val mNew = New()
                mNew.author = new.author
                mNew.content = new.content
                mNew.description = new.description
                mNew.highlight = new.highlight
                mNew.image_url = new.image_url
                mNew.published_at = getDateFormatted(new.published_at)
                mNew.title = new.title
                mNew.url = new.url

                mNews.add(mNew)
            }
            saveTotalPages(responseNew.pagination.total_pages)
            _totalNews.value = responseNew.pagination.total_items
            if (lastPage == responseNew.pagination.total_pages) {
                saveTotalNews(responseNew.pagination.total_items, Constants.NEW)
            } else {
                saveLastPage(responseNew.pagination.current_page)
            }
            insertNews(mNews)
        }

        return mNews
    }

}