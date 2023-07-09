package com.nurhaqhalim.momento.core.remote.services

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.nurhaqhalim.momento.utils.GlobalConstants
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiServices {
    fun getInstance(context: Context): Retrofit {
        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context))
            .maxContentLength(250000L)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(false)
            .build()
        val client = OkHttpClient.Builder()
            .addInterceptor(chuckerInterceptor)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .protocols(listOf( Protocol.HTTP_1_1))
            .build()
        return Retrofit.Builder()
            .baseUrl(GlobalConstants.apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}