package com.nurhaqhalim.momento.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nurhaqhalim.momento.di.MoInjection

class MoVMFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoViewModel(MoInjection.retrieveRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}