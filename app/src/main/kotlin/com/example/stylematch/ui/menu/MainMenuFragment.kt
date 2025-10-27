package com.example.stylematch.ui.menu

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stylematch.R
import com.example.stylematch.databinding.FragmentMainMenuBinding
import dagger.hilt.android.AndroidEntryPoint

// --- MEJORA: Habilitar Hilt en este Fragment.
@AndroidEntryPoint
class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageFromGalleryLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityResults()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLaunchCamera.setOnClickListener {
            checkAndRequestCameraPermission()
        }

        binding.btnLaunchGallery.setOnClickListener {
            checkAndRequestStoragePermission()
        }

        binding.btnShowHairstyleGallery.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_hairstylesFragment)
        }

        binding.btnShowFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_favoritesFragment)
        }
    }

    private fun registerActivityResults() {
        pickImageFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val action = MainMenuFragmentDirections.actionMainMenuFragmentToFaceAnalysisFragmentGallery(it.toString())
                findNavController().navigate(action)
            }
        }

        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                navigateToFaceAnalysisCamera()
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }

        requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permiso de almacenamiento denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                navigateToFaceAnalysisCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), getString(R.string.permission_camera_rationale), Toast.LENGTH_LONG).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Volver a pedir
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun navigateToFaceAnalysisCamera() {
        // Navegar a FaceAnalysisFragment sin URI para modo cámara
        val action = MainMenuFragmentDirections.actionMainMenuFragmentToFaceAnalysisFragmentCamera(null)
        findNavController().navigate(action)
    }

    private fun checkAndRequestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), getString(R.string.permission_storage_rationale), Toast.LENGTH_LONG).show()
                requestStoragePermissionLauncher.launch(permission) // Volver a pedir
            }
            else -> {
                requestStoragePermissionLauncher.launch(permission)
            }
        }
    }

    private fun openGallery() {
        pickImageFromGalleryLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}