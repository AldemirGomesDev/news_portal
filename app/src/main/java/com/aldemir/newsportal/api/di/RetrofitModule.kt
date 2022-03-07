package com.aldemir.newsportal.api.di

import com.aldemir.newsportal.BuildConfig
import com.aldemir.newsportal.api.*
import com.aldemir.newsportal.api.models.ResponseLogin
import com.aldemir.newsportal.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideBaseUrl() = Constants.BASE_URL


    @Provides
    fun provideToken(): ResponseLogin {
        val sessionManager = SessionManager()
        val token = sessionManager.fetchAuthToken()
        return if (token != null) {
            ResponseLogin(token = token)
        } else {
            ResponseLogin(token = "")
        }
    }

    @Provides
    fun providesOkhttpInterceptor(responseLogin: ResponseLogin) : Interceptor{
        return  Interceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()
                .addHeader(Constants.AUTHORIZATION, "${Constants.BEARER} ${responseLogin.token}")
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    @Provides
    fun provideOkHttpClient(interceptor: Interceptor) = if (BuildConfig.DEBUG){
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }else{
        OkHttpClient
            .Builder()
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, BASE_URL:String): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper = apiHelper
}