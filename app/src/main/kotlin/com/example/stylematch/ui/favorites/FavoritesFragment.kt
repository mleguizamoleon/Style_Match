package com.example.stylematch.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylematch.databinding.FragmentFavoritesBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// --- MEJORA: Habilitar Hilt en este Fragment.
@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    // Hilt se encargará de proveer el ViewModel correcto aquí gracias a la anotación de la clase.
    private val favoritesViewModel: FavoritesViewModel by viewModels()

    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter { favorite ->
            // Acción al hacer clic en el botón de eliminar.
            favoritesViewModel.deleteFavorite(favorite)

            // Implementar la acción de "DESHACER" en el Snackbar.
            Snackbar.make(binding.root, "${favorite.hairstyleName.replace('_', ' ')} eliminado", Snackbar.LENGTH_LONG)
                .setAction("DESHACER") {
                    // Llamar a la función en el ViewModel para restaurar el favorito.
                    favoritesViewModel.undoDelete()
                }.show()
        }

        binding.rvFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        binding.pbLoadingFavorites.visibility = View.VISIBLE // Mostrar al inicio
        favoritesViewModel.allFavorites.observe(viewLifecycleOwner) { favoritesList ->
            binding.pbLoadingFavorites.visibility = View.GONE

            if (favoritesList.isNullOrEmpty()) {
                binding.rvFavorites.visibility = View.GONE
                binding.tvNoFavorites.visibility = View.VISIBLE
            } else {
                binding.rvFavorites.visibility = View.VISIBLE
                binding.tvNoFavorites.visibility = View.GONE
                favoritesAdapter.submitList(favoritesList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar memory leaks
    }
}