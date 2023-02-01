package com.aldemir.newsportal.api

import com.aldemir.newsportal.api.models.*
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(
    private val apiService: ApiService
): ApiHelper {
    override suspend fun sinIn(requestLogin: RequestLogin): ResponseLogin {
        return apiService.sinIn(requestLogin)
    }

    override suspend fun sinUp(requestRegister: RequestRegister): ResponseLogin {
        return apiService.sinUp(requestRegister)
    }

    override suspend fun getAllNews(
        current_page: String,
        per_page: String,
        published_at: String
    ): Response<ResponseNew> {
        return apiService.getAllNews(
            current_page = current_page,
            per_page = per_page,
            published_at = published_at
        )
    }

    override suspend fun getAllNewsHighlights(): Response<ResponseNewHighlights> {
        return apiService.getAllNewsHighlights()
    }
}