package com.nurhaqhalim.momento.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate


object SettingUtils {
    fun updateTheme(activity: Activity, mode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(mode)
        activity.recreate()
        return true
    }

    fun getTheme(): Int {
        return AppCompatDelegate.getDefaultNightMode()
    }
}