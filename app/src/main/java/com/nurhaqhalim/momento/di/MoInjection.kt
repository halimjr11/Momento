package com.nurhaqhalim.momento.di

import android.content.Context
import com.nurhaqhalim.momento.core.MoRepository
import com.nurhaqhalim.momento.core.local.database.MoDatabase
import com.nurhaqhalim.momento.core.remote.services.ApiEndpoint
import com.nurhaqhalim.momento.core.remote.services.ApiServices

object MoInjection {
    fun retrieveRepository(context: Context): MoRepository {
        val database = MoDatabase.getDatabase(context)
        val apiService = ApiServices.getInstance(context).create(ApiEndpoint::class.java)
        return MoRepository(context, apiService, database)
    }
}