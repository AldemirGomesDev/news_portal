package com.aldemir.newsportal.api

import android.content.Context
import android.content.SharedPreferences
import com.aldemir.newsportal.MyApplication
import com.aldemir.newsportal.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class SessionManager @Inject constructor (
) {

    private var prefs: SharedPreferences = MyApplication.appContext
        .getSharedPreferences(MyApplication.appContext.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val NAME = "name"
        const val LAST_PAGE = "last_page"
        const val TOTAL_PAGES = "total_pages"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserId(id: Long) {
        val editor = prefs.edit()
        editor.putLong(USER_ID, id)
        editor.apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(USER_ID, 0)
    }

    fun saveName(name: String) {
        val editor = prefs.edit()
        editor.putString(NAME, name)
        editor.apply()
    }

    fun getName(): String? {
        return prefs.getString(NAME, null)
    }

    fun saveUserName(userName: String) {
        val editor = prefs.edit()
        editor.putString(USER_NAME, userName)
        editor.apply()
    }

    fun getUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun saveLastPage(lastPage: Int) {
        val editor = prefs.edit()
        editor.putInt(LAST_PAGE, lastPage)
        editor.apply()
    }

    fun getLastPage(): Int {
        return prefs.getInt(LAST_PAGE, 0)
    }

    fun saveTotalPages(totalPages: Int) {
        val editor = prefs.edit()
        editor.putInt(TOTAL_PAGES, totalPages)
        editor.apply()
    }

    fun getTotalPages(): Int {
        return prefs.getInt(TOTAL_PAGES, 0)
    }

    fun saveTotalNews(totalNews: Int, tag: String) {
        val editor = prefs.edit()
        editor.putInt(tag, totalNews)
        editor.apply()
    }

    fun getTotalNews(tag: String): Int {
        return prefs.getInt(tag, 0)
    }
}