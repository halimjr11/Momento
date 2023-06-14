package com.nurhaqhalim.momento.view.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nurhaqhalim.momento.databinding.ItemLoadingBinding
import com.nurhaqhalim.momento.databinding.ItemStoryBinding
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.GlobalConstants


class MoStoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var onItemClickListener: onClickListener
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    fun setOnItemClickListener(onClickListener: onClickListener) {
        this.onItemClickListener = onClickListener
    }

    private var list = listOf<StoryModel?>()

    fun setData(data: List<StoryModel?>) {
        list = data
    }

    inner class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(private val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                ViewHolder(
                    ItemStoryBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            else -> {
                LoadingViewHolder(
                    ItemLoadingBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }


    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    private fun showLoadingView(holder: LoadingViewHolder, position: Int) {

    }

    private fun populateItemRows(holder: ViewHolder, position: Int) {
        val story = list[position]
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
                        onItemClickListener.onItemClicked(story)
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

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is ViewHolder) {
            holder.binding.root.clearAnimation()
        }
    }

    interface onClickListener {
        fun onItemClicked(data: StoryModel)
    }

}