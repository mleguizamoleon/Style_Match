package com.example.stylematch.ui.hairstyle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stylematch.R
import com.example.stylematch.data.model.pexel.PexelsPhoto
import com.example.stylematch.databinding.ItemHairstyleBinding

class HairstyleAdapter : ListAdapter<PexelsPhoto, HairstyleAdapter.HairstyleViewHolder>(HairstyleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HairstyleViewHolder {
        val binding = ItemHairstyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HairstyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HairstyleViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }

    inner class HairstyleViewHolder(private val binding: ItemHairstyleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: PexelsPhoto) {
            binding.tvPhotographer.text = itemView.context.getString(R.string.pexels_photographer_prefix, photo.photographer)
            // Use a smaller image source for thumbnails to save data and load faster
            Glide.with(itemView.context)
                .load(photo.src.medium) // Or photo.src.small / photo.src.portrait
                .placeholder(R.drawable.ic_placeholder_image) // Add a placeholder drawable
                .error(R.drawable.ic_error_image) // Add an error drawable
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivHairstyle)
        }
    }

class HairstyleDiffCallback : DiffUtil.ItemCallback<PexelsPhoto>() {
    override fun areItemsTheSame(oldItem: PexelsPhoto, newItem: PexelsPhoto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PexelsPhoto, newItem: PexelsPhoto): Boolean {
        return oldItem == newItem
    }
}
}
