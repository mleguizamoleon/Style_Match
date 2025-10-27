package com.example.stylematch.ui.analysis

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.stylematch.databinding.FragmentFaceAnalysisBinding
import com.example.stylematch.ml.FaceAnalyzer
import com.example.stylematch.ml.HairstyleRecommender
import com.example.stylematch.util.FaceDetectionUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// --- MEJORA: Habilitar Hilt en el Fragment.
@AndroidEntryPoint
class FaceAnalysisFragment : Fragment() {

    private var _binding: FragmentFaceAnalysisBinding? = null
    private val binding get() = _binding!!

    // Hilt se encarga de proveer el ViewModel correcto aquí.
    private val viewModel: FaceAnalysisViewModel by viewModels()
    private val navArgs: FaceAnalysisFragmentArgs by navArgs()

    private lateinit var cameraExecutor: ExecutorService
    private var imageUri: Uri? = null
    private var isFrontCamera = true
    private var imageCapture: ImageCapture? = null

    private val currentFavoriteStates = mutableMapOf<String, Boolean>()
    private var currentFaceAnalysisResult: FaceAnalyzer.FaceAnalysis? = null
    private val recommendationViewsMap = mutableMapOf<String, RecommendationItemView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUriString = navArgs.imageUri
        imageUri = imageUriString?.let { Uri.parse(it) }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()

        if (imageUri == null) { // Modo Cámara
            setupCameraUI()
            startCamera()
        } else { // Modo Galería
            binding.cameraGroup.visibility = View.GONE
            analyzeGalleryImage()
        }
    }

    private fun setupObservers() {
        viewModel.analysisState.observe(viewLifecycleOwner) { state ->
            // --- MEJORA: Gestión de estados de UI centralizada ---
            // Ocultar todas las vistas principales por defecto
            binding.progressBar.visibility = View.GONE
            binding.cameraGroup.visibility = View.GONE
            binding.recommendationsContainer.visibility = View.GONE
            binding.errorStateContainer.visibility = View.GONE

            when (state) {
                is AnalysisState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is AnalysisState.Success -> {
                    binding.recommendationsContainer.visibility = View.VISIBLE
                    currentFaceAnalysisResult = state.analysisResult
                    updateUIWithAnalysisResults(state.analysisResult, state.recommendations)
                }
                is AnalysisState.Error -> {
                    binding.errorStateContainer.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = state.errorMessage
                    binding.btnRetry.setOnClickListener {
                        // Navegar hacia atrás para permitir al usuario reintentar.
                        findNavController().navigateUp()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteToggledEvent.collect { (hairstyleName, newFavoriteState) ->
                currentFavoriteStates[hairstyleName] = newFavoriteState
                recommendationViewsMap[hairstyleName]?.updateFavoriteButton(newFavoriteState)
            }
        }
    }

    private fun updateUIWithAnalysisResults(
        faceAnalysis: FaceAnalyzer.FaceAnalysis,
        recommendations: List<HairstyleRecommender.HairstyleRecommendation>
    ) {
        if (!isAdded) return

        binding.segmentationWarningText.visibility = if (faceAnalysis.segmentationWarning != null) {
            binding.segmentationWarningText.text = faceAnalysis.segmentationWarning
            View.VISIBLE
        } else {
            View.GONE
        }

        val genderText = when (faceAnalysis.gender) {
            FaceAnalyzer.Gender.MALE -> "Masculino"
            FaceAnalyzer.Gender.FEMALE -> "Femenino"
            else -> "No determinado"
        }
        binding.genderText.text = "Género detectado: $genderText"
        binding.faceShapeText.text = "Forma del rostro: ${faceAnalysis.faceShape.name.lowercase().replaceFirstChar { it.titlecase() }}"
        binding.jawShapeText.text = "Forma de la mandíbula: ${faceAnalysis.proportions.jawShape.name.lowercase().replaceFirstChar { it.titlecase() }}"
        binding.symmetryText.text = "Simetría: ${(faceAnalysis.symmetryScore * 100).toInt()}%"

        val thirdsFeaturesText = faceAnalysis.features.mapNotNull {
            when(it) {
                FaceAnalyzer.FaceFeature.LONG_UPPER_THIRD -> "Superior Largo"
                FaceAnalyzer.FaceFeature.SHORT_UPPER_THIRD -> "Superior Corto"
                FaceAnalyzer.FaceFeature.LONG_MIDDLE_THIRD -> "Medio Largo"
                FaceAnalyzer.FaceFeature.SHORT_MIDDLE_THIRD -> "Medio Corto"
                FaceAnalyzer.FaceFeature.LONG_LOWER_THIRD -> "Inferior Largo"
                FaceAnalyzer.FaceFeature.SHORT_LOWER_THIRD -> "Inferior Corto"
                else -> null
            }
        }.ifEmpty { listOf("Equilibrado") }
        binding.facialThirdsText.text = "Proporciones: ${thirdsFeaturesText.joinToString(", ")}"

        val otherFeaturesString = faceAnalysis.features
            .filter { it.name.startsWith("PROMINENT") || it.name.startsWith("WIDE") || it.name.startsWith("NARROW") || it.name.startsWith("BALANCED") }
            .joinToString(", ") { it.name.lowercase().replace('_', ' ') }
        binding.featuresText.text = if (otherFeaturesString.isNotEmpty()) "Otras Características: ${otherFeaturesString.replaceFirstChar { it.uppercase() }}" else ""
        binding.featuresText.visibility = if (binding.featuresText.text.isNotEmpty()) View.VISIBLE else View.GONE

        binding.recommendationsList.removeAllViews()
        currentFavoriteStates.clear()
        recommendationViewsMap.clear()

        if (recommendations.isEmpty()) {
            val noRecTextView = android.widget.TextView(requireContext()).apply {
                text = "No se encontraron recomendaciones para los rasgos detectados."
                setTextAppearance(android.R.style.TextAppearance_Medium)
            }
            binding.recommendationsList.addView(noRecTextView)
        } else {
            recommendations.forEach { recommendation ->
                val itemView = RecommendationItemView(requireContext())
                recommendationViewsMap[recommendation.hairstyleName] = itemView
                lifecycleScope.launch {
                    viewModel.isHairstyleFavorite(recommendation.hairstyleName).collectLatest { isFavorite ->
                        if (isAdded && currentFavoriteStates[recommendation.hairstyleName] == null) {
                            currentFavoriteStates[recommendation.hairstyleName] = isFavorite
                            itemView.updateFavoriteButton(isFavorite)
                        }
                    }
                }
                itemView.setRecommendation(
                    hairstyleName = recommendation.hairstyleName,
                    confidence = recommendation.confidence,
                    reasons = recommendation.reasons,
                    description = viewModel.getHairstyleDescription(recommendation.hairstyleName),
                    isFavorite = currentFavoriteStates[recommendation.hairstyleName] ?: false,
                    onFavoriteClicked = { hairstyleNameClicked ->
                        currentFaceAnalysisResult?.let { currentAnalysis ->
                            val currentIsFavorite = currentFavoriteStates[hairstyleNameClicked] ?: false
                            viewModel.toggleFavoriteStatus(recommendation, currentAnalysis, currentIsFavorite)
                        } ?: Log.e("FaceAnalysisFragment", "currentFaceAnalysisResult is null")
                    },
                    imageUrl = recommendation.imageUrl
                )
                binding.recommendationsList.addView(itemView)
            }
        }
    }

    private fun setupCameraUI() {
        binding.cameraGroup.visibility = View.VISIBLE
        binding.captureButton.visibility = View.VISIBLE
        binding.switchCameraButton.setOnClickListener { isFrontCamera = !isFrontCamera; startCamera() }
        binding.captureButton.setOnClickListener { takePhotoAndAnalyze() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            if (_binding == null) return@addListener
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setTargetRotation(binding.cameraPreviewView.display.rotation).build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val analyzer = LiveFaceAnalyzer(binding.faceOverlayView) { _, _ -> /* Live analysis for overlay only */ }
            analyzer.isLivePreviewFrontCamera = isFrontCamera
            imageAnalyzer.setAnalyzer(cameraExecutor, analyzer)
            val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch (exc: Exception) {
                if(isAdded) viewModel.reportCameraInitializationError()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhotoAndAnalyze() {
        val imageCapture = imageCapture ?: return
        viewModel.setLoadingState()
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap().let {
                        val matrix = android.graphics.Matrix().apply {
                            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                            if (isFrontCamera) {
                                postScale(-1f, 1f, it.width / 2f, it.height / 2f)
                            }
                        }
                        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                    }
                    imageProxy.close()
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val faceDetector = FaceDetection.getClient(FaceDetectionUtil.getGalleryDetectorOptions())
                    faceDetector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                viewModel.analyzeFaceAndRecommend(faces.first(), bitmap)
                            } else {
                                viewModel.reportGalleryImageError("No se detectó ningún rostro.")
                            }
                        }
                        .addOnFailureListener { e ->
                            viewModel.reportGalleryImageError("Error en la detección de rostros: ${e.message}")
                        }
                }
                override fun onError(exception: ImageCaptureException) {
                    viewModel.reportGalleryImageError("Error en la captura de la foto: ${exception.message}")
                }
            }
        )
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun analyzeGalleryImage() {
        imageUri?.let { uri ->
            lifecycleScope.launch {
                if (!isAdded) return@launch
                viewModel.setLoadingState()
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
                    } else {
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    }
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val image = InputImage.fromBitmap(mutableBitmap, 0)
                    val faceDetector = FaceDetection.getClient(FaceDetectionUtil.getGalleryDetectorOptions())
                    val faces: List<Face> = faceDetector.process(image).await()
                    if (faces.isNotEmpty()) {
                        viewModel.analyzeFaceAndRecommend(faces.first(), mutableBitmap)
                    } else {
                        viewModel.reportGalleryImageError("No se detectó ningún rostro en la imagen.")
                    }
                } catch (e: Exception) {
                    viewModel.reportGalleryImageError("Error al procesar la imagen: ${e.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdownNow()
        _binding = null
    }
}