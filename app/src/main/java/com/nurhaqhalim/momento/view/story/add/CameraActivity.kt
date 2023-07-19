package com.nurhaqhalim.momento.view.story.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.databinding.ActivityCameraBinding
import com.nurhaqhalim.momento.utils.GlobalConstants
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()) {
            Timber.tag(TAG)
                .d(resources.getString(R.string.message_permission_granted))
            startCamera()
        } else {
            requestCameraPermissions()
        }

        initListener()
    }

    private fun initListener() {
        binding.btnCapture.setOnClickListener {
            captureImage()
        }
    }

    private fun captureImage() {
        val capture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            "${
                SimpleDateFormat(
                    GlobalConstants.fileNameFormat,
                    Locale.getDefault()
                ).format(System.currentTimeMillis())
            }.jpg"
        )
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri: Uri = Uri.fromFile(photoFile)
                    val resultIntent = Intent()
                    resultIntent.putExtra(GlobalConstants.cameraTag, savedUri.toString())
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.tag(TAG).d(resources.getString(R.string.failed_save_file))
                }

            })

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull().let { file ->
            File(file, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir.exists()) mediaDir else filesDir
    }

    private fun requestCameraPermissions() {
        Dexter.withContext(this).withPermission(
            Manifest.permission.CAMERA
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                if (response != null) {
                    if (response.permissionName == Manifest.permission.CAMERA) {
                        startCamera()
                    }
                }
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
            }

        }).onSameThread().check()
    }

    private fun allPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { pre ->
                pre.setSurfaceProvider(binding.imagePreview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                Timber.tag(TAG).d(resources.getString(R.string.failed_start_camera))
            }
        }, ContextCompat.getMainExecutor(this))

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CaptureImageActivity"
    }
}
