package com.nurhaqhalim.momento.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.nurhaqhalim.momento.databinding.SettingDropdownLayoutBinding

class MoArrayAdapter(
    context: Context,
    private val items: List<String>,
    private val icon: List<Int>? = null
) :
    ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SettingDropdownLayoutBinding

        val itemView: View
        if (convertView == null) {
            binding =
                SettingDropdownLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            itemView = binding.root
            itemView.tag = binding
        } else {
            binding = convertView.tag as SettingDropdownLayoutBinding
            itemView = convertView
        }

        val item = items[position]
        binding.tvItemDropdown.text = item
        if (icon != null) {
            val icon = icon[position]
            val drawable = context.getDrawable(icon)
            binding.tvItemDropdown.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                null,
                null,
                null
            )
        }

        return itemView
    }
}