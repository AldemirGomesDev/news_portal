package com.aldemir.newsportal.data.repository

import androidx.lifecycle.LiveData
import com.aldemir.newsportal.api.ApiHelper
import com.aldemir.newsportal.data.database.NewDao
import com.aldemir.newsportal.models.New
import java.util.*
import javax.inject.Inject

class NewRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val newDao: NewDao
) {
    suspend fun getAllNews(lastPage: Int, perPage: Int) = apiHelper.getAllNews(lastPage.toString(), perPage.toString(), "")

    suspend fun getAllNewsHighlights() = apiHelper.getAllNewsHighlights()

    suspend fun insertNew(new: New): Long {
        return newDao.insert(new)
    }

    suspend fun insertNews(list: List<New>){
        newDao.insertAll(list)
    }

    suspend fun updateNew(new: New) {
        newDao.update(new)
    }

    fun getNewsDatabase(): LiveData<List<New>> {
        return newDao.getAll(false)
    }

    fun getFavorites(isFavorite: Boolean): LiveData<List<New>> {
        return newDao.getFavorites(isFavorite)
    }

    suspend fun getHighLight(highlight: Boolean): List<New> {
        return newDao.getHighLight(highlight)
    }

    suspend fun getNewsFilter(search: String, isFavorite: Boolean): List<New> {
        return newDao.getNewsFilterDates("%" +
                search.uppercase(Locale.getDefault()) +
                "%", isFavorite)
    }
}