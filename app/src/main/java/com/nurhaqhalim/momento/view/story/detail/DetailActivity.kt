package com.nurhaqhalim.momento.view.story.detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.databinding.ActivityDetailBinding
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.viewmodel.MoViewModel

class DetailActivity : AppCompatActivity() {
    private lateinit var detailBinding: ActivityDetailBinding
    private lateinit var userData: UserData
    private var storyData: StoryModel? = null
    private val viewModel: MoViewModel by viewModels()

    companion object {
        const val STORY_DATA = "story_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)
        userData = StorageHelper.getUserData(this)
        storyData = intent?.getParcelableExtra(STORY_DATA)
        fetchData()
        detailBinding.apply {
            reloadButton.setOnClickListener { fetchData() }
        }
    }

    private fun fetchData() {
        viewModel.fetchGetDetail(
            resources.getString(R.string.token_text).replace("%token%", userData.token),
            storyData?.id ?: ""
        ).observe(this) {
            with(detailBinding) {
                GlobalConstants.show(detailBinding.ivDetailPhoto, detailBinding.errorState)
                GlobalConstants.show(
                    detailBinding.storyDescriptionContainer, detailBinding.errorState
                )
                val story = it.story
                ivDetailPhoto.load(story.photoUrl)
                tvDetailName.text = story.name
                tvDetailDate.text = GlobalConstants.formatDate(story.createdAt)
                tvDetailDescription.text = story.description
                tvDetailLocation.text =
                    GlobalConstants.getAddress(this@DetailActivity, story.lat, story.lon)
            }
        }

        viewModel.errorDetail.observe(this) {
            GlobalConstants.show(detailBinding.errorState, detailBinding.ivDetailPhoto)
            GlobalConstants.show(detailBinding.errorState, detailBinding.storyDescriptionContainer)
        }
    }

}