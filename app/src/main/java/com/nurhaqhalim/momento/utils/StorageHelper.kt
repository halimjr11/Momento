package com.nurhaqhalim.momento.utils

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.nurhaqhalim.momento.model.UserData

object StorageHelper {

    fun defaultStorage(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun customStorage(context: Context, name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    fun saveUserLogin(context : Context, userData: UserData) {
        val localStorage = customStorage(context, GlobalConstants.storageName)
        val json = Gson().toJson(userData)
        localStorage[GlobalConstants.keyLogin] = true
        localStorage[GlobalConstants.keyUser] = json
    }

    fun getUserData(context: Context): UserData {
        val localStorage = customStorage(context, GlobalConstants.storageName)
        return Gson().fromJson(localStorage[GlobalConstants.keyUser, ""], UserData::class.java)
    }

    fun saveLocation(context: Context, location: Location){
        val localStorage = customStorage(context, GlobalConstants.storageName)
        val json = Gson().toJson(localStorage)
        localStorage[GlobalConstants.keyLocation] = json
    }

    fun getLocation(context: Context): Location {
        val localStorage = customStorage(context, GlobalConstants.storageName)
        return Gson().fromJson(localStorage[GlobalConstants.keyLocation, ""], Location::class.java)
    }

    fun resetUserData(context: Context){
        removeData(context, GlobalConstants.keyUser)
    }

    private fun removeData(context: Context, key: String) {
        val localStorage = customStorage(context, GlobalConstants.storageName)
        localStorage.edit().apply {
            remove(key)
            apply()
        }
    }

}