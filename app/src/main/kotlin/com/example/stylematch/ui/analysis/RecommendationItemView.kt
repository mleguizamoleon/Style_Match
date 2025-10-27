package com.example.stylematch.ui.analysis

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
// <<<--- IMPORTACIÓN AÑADIDA ---<<<
import com.bumptech.glide.Glide
import com.example.stylematch.R
import com.example.stylematch.databinding.ViewRecommendationItemBinding

class RecommendationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRecommendationItemBinding

    init {
        binding = ViewRecommendationItemBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setRecommendation(
        hairstyleName: String,
        confidence: Float,
        reasons: List<String>,
        description: String,
        isFavorite: Boolean,
        onFavoriteClicked: (hairstyleName: String) -> Unit,
        // <<<--- SE AÑADE EL PARÁMETRO imageUrl ---<<<
        imageUrl: String?
    ) {
        binding.hairstyleNameText.text = hairstyleName.replace('_', ' ')
        binding.confidenceText.text = "${(confidence * 100).toInt()}% de compatibilidad"
        binding.reasonText.text = reasons.joinToString(separator = "\n• ", prefix = "• ")
        binding.descriptionText.text = description
        binding.confidenceProgressBar.progress = (confidence * 100).toInt()

        updateFavoriteButton(isFavorite)

        binding.favoriteButton.setOnClickListener {
            onFavoriteClicked(hairstyleName)
        }

        // <<<--- LÓGICA AÑADIDA PARA CARGAR LA IMAGEN ---<<<
        if (!imageUrl.isNullOrBlank()) {
            binding.hairstyleImageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image) // Muestra esto mientras carga
                .error(R.drawable.ic_error_image)         // Muestra esto si hay un error
                .into(binding.hairstyleImageView)
        } else {
            // Si no hay URL, oculta la vista de la imagen o muestra un placeholder
            binding.hairstyleImageView.visibility = View.GONE
        }
    }

    fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
        }
    }
}