package com.aldemir.newsportal.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.aldemir.newsportal.models.New

@Dao
interface NewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(new: New): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<New>): List<Long>

    @Update
    suspend fun update(new: New): Int

    @Delete
    suspend fun delete(new: New)

    @Query("SELECT * FROM New WHERE id = :id")
    suspend fun get(id: Int): New

    @Query("SELECT * FROM New WHERE highlight = :highlight")
    fun getAll(highlight: Boolean): LiveData<List<New>>

    @Query("SELECT * FROM New WHERE is_favorite = :isFavorite")
    fun getFavorites(isFavorite: Boolean): LiveData<List<New>>

    @Query("SELECT * FROM New WHERE highlight = :highlight")
    suspend fun getHighLight(highlight: Boolean): List<New>

    @Query("SELECT * FROM New WHERE title LIKE :search AND is_favorite =:isFavorite")
    suspend fun getNewsFilterDates(search: String, isFavorite: Boolean): List<New>

}