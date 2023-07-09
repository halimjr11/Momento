package com.nurhaqhalim.momento.view.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.databinding.ItemStoryBinding
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.DataMapper
import com.nurhaqhalim.momento.utils.GlobalConstants

class MoPagingAdapter : PagingDataAdapter<StoryEntity, MoPagingAdapter.ViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickListener: OnClickListener
    fun setOnItemClickListener(onClickListener: OnClickListener) {
        this.onItemClickListener = onClickListener
    }

    inner class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        holder.binding.apply {
            if (story != null) {
                itemImage.load(story.photoUrl)
                itemTitle.text = story.name
                itemLocation.text =
                    GlobalConstants.getAddress(this.root.context, story.lat, story.lon)
                itemDescription.text = story.description
                root.apply {
                    playAnimation(this)
                    setOnClickListener {
                        onItemClickListener.onItemClicked(DataMapper.entityToStory(story))
                    }
                }
            }
        }
    }

    private fun playAnimation(view: View) {
        val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.5f, 1.0f).apply {
            duration = 300
        }
        val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.5f, 1.0f).apply {
            duration = 300
        }

        scaleXAnimator.start()
        scaleYAnimator.start()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    interface OnClickListener {
        fun onItemClicked(data: StoryModel)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}