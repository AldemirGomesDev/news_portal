package com.aldemir.newsportal.data.database.di

import android.content.Context
import androidx.room.Room
import com.aldemir.newsportal.data.database.ConfigDataBase
import com.aldemir.newsportal.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideConfigDataBase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        ConfigDataBase::class.java,
        Constants.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideUserDao(db: ConfigDataBase) = db.userDao()

    @Singleton
    @Provides
    fun provideNewDao(db: ConfigDataBase) = db.newDao()
}