package com.aldemir.newsportal.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemir.newsportal.data.repository.NewRepository
import com.aldemir.newsportal.models.New
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.sql.SQLException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val newRepository: NewRepository
) : ViewModel() {

    companion object {
        const val TAG = "SearchViewModel"
    }

    private val _newsDatabase = MutableLiveData<List<New>>()
    var newsDatabase: LiveData<List<New>> = _newsDatabase

    fun getNewsFilter(search: String, isFavorite: Boolean) {
        try {
            viewModelScope.launch {
                _newsDatabase.value = newRepository.getNewsFilter(search, isFavorite)
            }
        }catch (error: SQLException) {
            Log.e(TAG, "ERROR ROOM => : ${error}")
        }
    }

    fun addNewsFavorite(new: New) {
        viewModelScope.launch {
            newRepository.updateNew(new)
        }
    }

    fun removeNewsFavorite(new: New) {
        viewModelScope.launch {
            newRepository.updateNew(new)
        }
    }
}