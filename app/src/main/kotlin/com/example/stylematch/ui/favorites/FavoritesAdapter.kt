package com.example.stylematch.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stylematch.R
import com.example.stylematch.data.local.entity.FavoriteHairstyleEntity
import com.example.stylematch.databinding.ItemFavoriteHairstyleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavoritesAdapter(
    private val onDeleteClicked: (FavoriteHairstyleEntity) -> Unit
) : ListAdapter<FavoriteHairstyleEntity, FavoritesAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteHairstyleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavoriteViewHolder(
        private val binding: ItemFavoriteHairstyleBinding,
        private val onDeleteClicked: (FavoriteHairstyleEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        fun bind(favorite: FavoriteHairstyleEntity) {
            binding.tvFavHairstyleName.text = favorite.hairstyleName.replace('_', ' ')
            binding.tvFavMainReason.text = "Razón: ${favorite.mainReason}"
            binding.tvFavFaceShapeAtRecommendation.text = "Recomendado para: ${favorite.faceShapeAtRecommendation}"
            binding.tvFavConfidence.text = "Confianza: ${(favorite.confidenceAtRecommendation * 100).toInt()}%"
            binding.tvFavTimestamp.text = "Guardado: ${dateFormat.format(Date(favorite.timestamp))}"

            // Hide description text view as it might be too long for this summary item
            binding.tvFavDescription.text = favorite.description
            binding.tvFavDescription.visibility = ViewGroup.GONE

            binding.btnDeleteFavorite.setOnClickListener {
                onDeleteClicked(favorite)
            }

            // **IMPROVEMENT**: Load the saved hairstyle image using Glide.
            if (!favorite.imageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(favorite.imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image) // Display while loading
                    .error(R.drawable.ic_error_image)         // Display on error
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivFavHairstyleImage)
            } else {
                // Set a default placeholder if no image URL is available
                binding.ivFavHairstyleImage.setImageResource(R.drawable.ic_placeholder_image)
            }
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteHairstyleEntity>() {
        override fun areItemsTheSame(oldItem: FavoriteHairstyleEntity, newItem: FavoriteHairstyleEntity): Boolean {
            return oldItem.hairstyleName == newItem.hairstyleName
        }

        override fun areContentsTheSame(oldItem: FavoriteHairstyleEntity, newItem: FavoriteHairstyleEntity): Boolean {
            return oldItem == newItem
        }
    }
}