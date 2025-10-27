package com.example.stylematch.ui.hairstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stylematch.databinding.FragmentHairstylesBinding
import dagger.hilt.android.AndroidEntryPoint

// --- MEJORA: Habilitar Hilt en este Fragment.
@AndroidEntryPoint
class HairstylesFragment : Fragment() {

    private var _binding: FragmentHairstylesBinding? = null
    private val binding get() = _binding!!

    // Hilt se encargará de proveer el ViewModel correcto aquí gracias a la anotación de la clase.
    private val hairstyleViewModel: HairstyleViewModel by viewModels()

    private lateinit var hairstyleAdapter: HairstyleAdapter
    private var isInitialLoad = true
    private val hairstyleQueries = listOf(
        "trendy haircut", "stylish hair", "modern hairstyle", "elegant hairstyle",
        "short hair fashion", "long hair ideas", "men's haircut", "women's haircut",
        "creative hairstyle", "runway hair"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHairstylesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        // Carga los peinados iniciales solo la primera vez que se crea la vista.
        if (isInitialLoad) {
            fetchInitialHairstyles()
            isInitialLoad = false
        }
    }

    private fun fetchInitialHairstyles() {
        val randomQuery = hairstyleQueries.random()
        hairstyleViewModel.fetchHairstyles(randomQuery)
    }

    private fun setupRecyclerView() {
        hairstyleAdapter = HairstyleAdapter()
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.hairstylesRecyclerView.adapter = hairstyleAdapter
        binding.hairstylesRecyclerView.layoutManager = layoutManager

        // Listener para la paginación (scroll infinito).
        binding.hairstylesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Si no se está cargando ya y el usuario llega cerca del final de la lista, carga más.
                if (!hairstyleViewModel.isFetching) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        hairstyleViewModel.fetchHairstyles(hairstyleViewModel.currentQuery)
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        hairstyleViewModel.hairstyleUIState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HairstyleUIState.InitialLoading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    binding.hairstylesRecyclerView.visibility = View.GONE
                }
                is HairstyleUIState.Success -> {
                    // Muestra la barra de progreso solo si está cargando más items, no en la carga inicial.
                    binding.progressBar.visibility = if (state.isLoadingMore) View.VISIBLE else View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.hairstylesRecyclerView.visibility = View.VISIBLE
                    hairstyleAdapter.submitList(state.photos.toList()) // Usa toList() para crear una nueva lista para el DiffUtil

                    if (state.photos.isEmpty()) {
                        binding.tvError.text = "No se encontraron peinados."
                        binding.tvError.visibility = View.VISIBLE
                    }
                }
                is HairstyleUIState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Si ya hay items cargados, muestra el error como un Toast. Si no, en la pantalla.
                    if(hairstyleAdapter.currentList.isEmpty()){
                        binding.hairstylesRecyclerView.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = state.message
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}