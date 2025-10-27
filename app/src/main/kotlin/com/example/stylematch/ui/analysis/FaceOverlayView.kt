package com.example.stylematch.ui.analysis

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.example.stylematch.R
import kotlin.math.ceil

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val faces = mutableListOf<Face>()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var cameraPreviewWidth: Int = 0
    private var cameraPreviewHeight: Int = 0
    private var isFrontCamera: Boolean = false

    private val boundingBoxPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.purple_200) // Or any color you defined
        style = Paint.Style.STROKE
        strokeWidth = 5.0f
    }

    private val landmarkPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 8.0f
    }

    private val contourPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30.0f
    }

    fun updateFaces(
        faces: List<Face>,
        imageWidth: Int,
        imageHeight: Int,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        isFrontCamera: Boolean
    ) {
        this.faces.clear()
        this.faces.addAll(faces)
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.cameraPreviewWidth = cameraPreviewWidth
        this.cameraPreviewHeight = cameraPreviewHeight
        this.isFrontCamera = isFrontCamera
        invalidate() // Request redraw
    }

    fun clearFaces() {
        faces.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0 || cameraPreviewWidth == 0 || cameraPreviewHeight == 0) {
            return // Dimensions not set yet
        }

        val scaleX = cameraPreviewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = cameraPreviewHeight.toFloat() / imageHeight.toFloat()

        // Determine the common scale factor, usually fitting the smaller dimension
        val scale = kotlin.math.min(scaleX, scaleY)

        // Calculate offsets to center the scaled image within the preview
        val offsetX = (cameraPreviewWidth - ceil(imageWidth * scale)) / 2.0f
        val offsetY = (cameraPreviewHeight - ceil(imageHeight * scale)) / 2.0f

        // Save canvas state
        canvas.save()
        // ML Kit coordinates are usually based on the original image.
        // If using front camera, the X-coordinates need to be mirrored for display on the overlay.
        if (isFrontCamera) {
            canvas.scale(-1f, 1f, cameraPreviewWidth / 2f, cameraPreviewHeight / 2f)
        }

        for (face in faces) {
            val bounds = face.boundingBox
            // Adjust bounding box coordinates
            val MappedBoundingBox = RectF(
                bounds.left * scale + offsetX,
                bounds.top * scale + offsetY,
                bounds.right * scale + offsetX,
                bounds.bottom * scale + offsetY
            )
            canvas.drawRect(MappedBoundingBox, boundingBoxPaint)

            // Draw landmarks
            face.allLandmarks.forEach { landmark ->
                val pointX = landmark.position.x * scale + offsetX
                val pointY = landmark.position.y * scale + offsetY
                canvas.drawCircle(pointX, pointY, 6.0f, landmarkPaint)
            }

            // Draw contours
            face.allContours.forEach { contour ->
                if (contour.points.isNotEmpty()) {
                    val path = Path()
                    val firstPoint = contour.points[0]
                    path.moveTo(firstPoint.x * scale + offsetX, firstPoint.y * scale + offsetY)
                    for (i in 1 until contour.points.size) {
                        val point = contour.points[i]
                        path.lineTo(point.x * scale + offsetX, point.y * scale + offsetY)
                    }
                    // Optionally close specific contours like face oval
                    if (contour.faceContourType == FaceContour.FACE) {
                        // path.close() // Closing the face oval path
                    }
                    canvas.drawPath(path, contourPaint)
                }
            }

            // Example: Display Face ID or other info
            if (face.trackingId != null) {
                canvas.drawText("ID: ${face.trackingId}", MappedBoundingBox.left, MappedBoundingBox.top - 10, textPaint)
            }
        }
        // Restore canvas state
        canvas.restore()
    }
}