package com.aldemir.newsportal.data.database

import androidx.room.*
import com.aldemir.newsportal.models.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User): Int

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getUserEmail(email: String): User?

    @Query("SELECT * FROM User WHERE email = :email AND isLogged = :isLogged")
    suspend fun getUserLogged(email: String, isLogged: Boolean): User
}