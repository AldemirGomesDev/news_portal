package com.aldemir.newsportal.data.repository

import com.aldemir.newsportal.api.ApiHelper
import com.aldemir.newsportal.api.models.RequestLogin
import com.aldemir.newsportal.api.models.RequestRegister
import com.aldemir.newsportal.data.database.UserDao
import com.aldemir.newsportal.models.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val userDao: UserDao
) {
    suspend fun sinIn(requestLogin: RequestLogin) = apiHelper.sinIn(requestLogin = requestLogin)

    suspend fun sinUp(requestRegister: RequestRegister) = apiHelper.sinUp(requestRegister = requestRegister)

    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUserLogged(email: String, isLogged: Boolean): User {
        return userDao.getUserLogged(email, isLogged)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserEmail(email)
    }

}