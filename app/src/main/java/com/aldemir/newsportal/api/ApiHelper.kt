package com.aldemir.newsportal.api

import com.aldemir.newsportal.api.models.*
import retrofit2.Response

interface ApiHelper {

    suspend fun sinIn(requestLogin: RequestLogin): ResponseLogin

    suspend fun sinUp(requestRegister: RequestRegister): ResponseLogin

    suspend fun getAllNews(
        current_page: String,
        per_page: String,
        published_at: String
    ): ResponseNew

    suspend fun getAllNewsHighlights(): Response<ResponseNewHighlights>
}