package com.nurhaqhalim.momento.service

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.core.services.ApiEndpoint
import com.nurhaqhalim.momento.core.services.ApiServices
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.DataMapper
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MoViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private lateinit var data: List<StoryModel>
    private lateinit var apiEndpoint: ApiEndpoint

    override fun onCreate() {
        apiEndpoint = ApiServices.getInstance(context).create(ApiEndpoint::class.java)
        fetchData()
    }

    private fun fetchData() {
        runBlocking(Dispatchers.IO) {
            val userData = StorageHelper.getUserData(context)
            val list = apiEndpoint.getStories(
                context.resources.getString(R.string.token_text)
                    .replace("%token%", userData.token)
            )
            data = DataMapper.listStoryToModel(list.execute().body()?.listStory ?: mutableListOf())
        }
    }

    override fun onDataSetChanged() {
        fetchData()
    }

    override fun onDestroy() {}


    override fun getCount(): Int = data.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = data[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item)
        views.setTextViewText(R.id.widget_item_title, item.name)
        views.setTextViewText(
            R.id.widget_item_location,
            GlobalConstants.getAddress(context, item.lat, item.lon)
        )
        val imageUrl = item.photoUrl

        if (imageUrl.isNotEmpty()) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                views.setImageViewBitmap(R.id.widget_item_image, bitmap)
            } catch (e: Exception) {
                // Handle any exceptions that occur during image loading
            }
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}