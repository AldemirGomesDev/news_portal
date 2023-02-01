package com.aldemir.newsportal.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.aldemir.newsportal.api.SessionManager
import com.aldemir.newsportal.api.models.ResponseNew
import com.aldemir.newsportal.api.models.ResponseNewHighlights
import com.aldemir.newsportal.data.repository.NewRepository
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.util.*
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

    private val _lastPage = MutableLiveData<Event<Int>>()
    val lastPage = _lastPage.asLiveData()

    private val _totalPages = MutableLiveData<Event<Int>>()
    val totalPages = _totalPages.asLiveData()

    private val _totalNews = MutableLiveData<Event<Int>>()
    val totalNews = _totalNews.asLiveData()

    private val _totalNewsHighLight = MutableLiveData<Event<Int>>()
    val totalNewsHighLight = _totalNewsHighLight.asLiveData()

    lateinit var newsDatabase: LiveData<List<New>>

    private val _newsHighLight = MutableLiveData<List<New>>()
    var newsHighLight = _newsHighLight.asLiveData()


    private val _news = MutableLiveData<Event<Resource<List<New>>>>(Event((Resource.loading(null))))
    var news = _news.asLiveData()

    private val _newsHighlights = MutableLiveData<Event<List<New>>>()
    var newsHighlights = _newsHighlights.asLiveData()

    fun getDatabaseNews() {
        try {
            viewModelScope.launch {
                newsDatabase = newsRepository.getNewsDatabase()
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    fun getDatabaseNewsHighLight(highlight: Boolean) {
        try {
            viewModelScope.launch {
                _newsHighLight.value = newsRepository.getDatabaseNewsHighLight(highlight)
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    fun getAllNews(lastPage: Int, perPage: Int, countNews: Int) {
        _news.emit(Resource.loading(null))
        viewModelScope.launch {
            try {
                newsRepository.getAllNews(lastPage, perPage).let {
                    if (it.isSuccessful) {
                        val news = formatNew(
                            responseNew = it.body()!!,
                            countNews = countNews,
                            lastPage = lastPage
                        )
                        _news.emit(Resource.success(news))
                    } else {
                        _news.emit(Resource.error("Nenhum notícia encontrada!", null))
                    }
                }
            } catch (error: Exception) {
                _news.emit((Resource.error("Sem conexão, tente novamente", null)))
                _totalNews.emit(0)
            }

        }
    }

    fun getAllNewsHighlights(count: Int) = viewModelScope.launch {
        val mNews: ArrayList<New> = arrayListOf()
        try {
            newsRepository.getAllNewsHighlights().let {
                if (it.isSuccessful){
                    val result = it.body()!!.data
                    mNews.addAll(formatNewHighlights(responseNew = result, count = count))
                    _newsHighlights.emit(mNews)
                    saveTotalNews(result.size, Constants.NEW_HIGH_LIGHT)
                }
                if (mNews.size > count) {
                    insertNews(mNews)
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "ERROR => : ${error}")
        }

    }

    fun updateFavoriteNews(new: New) {
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
        _lastPage.emit(sessionManager.getLastPage())
    }

    fun getTotalPages() {
        _totalPages.emit(sessionManager.getTotalPages())
    }

    fun getTotalNews(tag: String) {
        val totalNews = sessionManager.getTotalNews(tag)
        if (tag == "news") {
            _totalNews.emit(totalNews)
        } else {
            _totalNewsHighLight.emit(totalNews)
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
            _totalNews.emit(responseNew.pagination.total_items)
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