package com.aldemir.newsportal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aldemir.newsportal.models.New
import com.aldemir.newsportal.models.User
import com.aldemir.newsportal.util.DateTypeConverter

@Database(entities = [User::class, New::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class ConfigDataBase : RoomDatabase() {

    abstract fun  userDao(): UserDao
    abstract fun newDao(): NewDao
}