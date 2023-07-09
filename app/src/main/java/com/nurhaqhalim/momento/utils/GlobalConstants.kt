package com.nurhaqhalim.momento.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.View
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.model.SettingModel
import okio.IOException
import java.text.SimpleDateFormat
import java.util.Locale

object GlobalConstants {
    const val storageName = "momento.db"
    const val tableName = "story"
    const val remoteKeys = "keys"
    const val dbName = "modatabase"
    const val keyLogin = "isLogin"
    const val keyUser = "userData"
    const val keyLocation = "location"
    const val successTag = "successDialog"
    const val failedTag = "failedDialog"
    val successAnimation = R.raw.success
    val failedAnimation = R.raw.failed
    const val apiUrl = "https://story-api.dicoding.dev/v1/"

    fun hideView(view1: View, view2: View) {
        view1.visibility = View.GONE
        view2.visibility = View.VISIBLE
    }

    fun show(view1: View, view2: View) {
        view1.visibility = View.VISIBLE
        view2.visibility = View.GONE
    }

    fun show(view1: View, view2: View, view3: View) {
        view1.visibility = View.VISIBLE
        view2.visibility = View.GONE
        view3.visibility = View.GONE
    }

    fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText = ""

        if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
            return addressText  // Return empty address text if the coordinates are invalid
        }

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            addressText = if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                "${address.featureName} ${address.thoroughfare} ${address.locality} ${address.subAdminArea}"
            } else {
                context.resources.getString(R.string.unknown_location_text)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }

    fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

    fun getThemeModel(): SettingModel {
        return SettingModel(
            arrayListOf(
                R.drawable.ic_theme,
                R.drawable.ic_light_mode,
                R.drawable.ic_dark_mode,
            ), arrayListOf(
                R.string.setting_theme_system,
                R.string.setting_theme_light,
                R.string.setting_theme_dark
            )
        )
    }
}