package com.example.coindetector

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.lang.reflect.Field

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var javaCameraView: JavaCameraView
    private lateinit var radiusTextView: TextView
    private val radiusHistory = mutableListOf<Double>()
    private val maxHistory = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "No se pudo cargar OpenCV")
        } else {
            Log.d("OpenCV", "OpenCV cargado correctamente")
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Título superior
        val titleTextView = TextView(this).apply {
            text = "Contador de Monedas Colombianas \n By JL"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(16, 32, 16, 16)
        }

        javaCameraView = JavaCameraView(this, -1)
        javaCameraView.visibility = SurfaceView.VISIBLE
        javaCameraView.setCvCameraViewListener(this)

        // Texto informativo (con fuente llamativa)
        radiusTextView = TextView(this).apply {
            textSize = 18f
            setPadding(16, 16, 16, 16)
            typeface = Typeface.MONOSPACE
        }

        layout.addView(
            titleTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        layout.addView(
            javaCameraView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        layout.addView(
            radiusTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(layout)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            javaCameraView.setCameraPermissionGranted()
            javaCameraView.enableView()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            javaCameraView.setCameraPermissionGranted()
            javaCameraView.enableView()
        } else {
            Log.e("Permisos", "Permiso de cámara denegado")
        }
    }

    override fun onResume() {
        super.onResume()
        if (::javaCameraView.isInitialized) {
            javaCameraView.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::javaCameraView.isInitialized) {
            javaCameraView.disableView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::javaCameraView.isInitialized) {
            javaCameraView.disableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        try {
            val cameraField: Field = JavaCameraView::class.java.getDeclaredField("mCamera")
            cameraField.isAccessible = true
            val camera = cameraField.get(javaCameraView) as android.hardware.Camera
            camera.setDisplayOrientation(90)
            val params = camera.parameters
            params.setRotation(90)
            camera.parameters = params
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()

        val gray = Mat()
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(15.0, 15.0), 3.0)

        val circles = Mat()
        Imgproc.HoughCircles(
            gray,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.0,
            50.0,
            100.0,
            45.0,
            10,
            100
        )

        val radii = mutableListOf<Double>()

        if (!circles.empty()) {
            for (i in 0 until circles.cols()) {
                val data = circles.get(0, i)
                if (data != null && data.size >= 3) {
                    val center = Point(data[0], data[1])
                    val radius = data[2]
                    radii.add(radius)

                    Imgproc.circle(rgba, center, radius.toInt(), Scalar(0.0, 255.0, 0.0), 4)
                    Imgproc.circle(rgba, center, 3, Scalar(255.0, 0.0, 0.0), -1)
                }
            }
        }

        val counts = mutableMapOf(50 to 0, 100 to 0, 200 to 0)

        if (radii.isNotEmpty()) {
            val minRadius = radii.minOrNull() ?: 1.0

            radiusHistory.add(minRadius)
            if (radiusHistory.size > maxHistory) {
                radiusHistory.removeAt(0)
            }

            val smoothedBase = radiusHistory.average()

            for (r in radii) {
                val ratio = r / smoothedBase
                val value = when {
                    ratio < 1.08 -> 50
                    ratio < 1.30 -> 100
                    else -> 200
                }
                counts[value] = counts.getOrDefault(value, 0) + 1
            }
        }

        runOnUiThread {
            val total = counts.entries.sumOf { (denom, qty) -> denom * qty }

            radiusTextView.text = buildString {
                append("Monedas detectadas:\n")
                append("50:  ${counts[50]}   → ${counts[50]?.times(50)}\n")
                append("100: ${counts[100]}  → ${counts[100]?.times(100)}\n")
                append("200: ${counts[200]}  → ${counts[200]?.times(200)}\n")
                append("\nTotal: $total pesos")
            }
        }

        return rgba
    }
}
