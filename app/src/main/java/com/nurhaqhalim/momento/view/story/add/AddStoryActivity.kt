package com.nurhaqhalim.momento.view.story.add

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import coil.load
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.components.MoDialog
import com.nurhaqhalim.momento.core.Result
import com.nurhaqhalim.momento.databinding.ActivityAddStoryBinding
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.view.home.MainActivity
import com.nurhaqhalim.momento.viewmodel.MoViewModel
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddStoryActivity : AppCompatActivity() {
    private lateinit var addStoryBinding: ActivityAddStoryBinding
    private lateinit var fileRequestBody: RequestBody
    private lateinit var fileName: String
    private lateinit var pickPhoto: ActivityResultLauncher<String>
    private lateinit var takePhoto: ActivityResultLauncher<Intent>
    private lateinit var userData: UserData
    private var savePermission: Boolean = false
    private val viewModel: MoViewModel by viewModels()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val requestPermissionCode: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addStoryBinding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(addStoryBinding.root)
        getLocations()
        userData = StorageHelper.getUserData(this)
        takePhoto = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val extra = result.data?.extras
                val data = extra?.get("data") as Bitmap

                if (savePermission) {
                    saveBitmapToFile(this, data)?.let {
                        retrieveFile(it)
                    }
                }
            }
        }

        pickPhoto = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            val file = it?.let { uri -> getFileFromContentUri(this, uri) }
            if (file != null) {
                retrieveFile(file)
            }
        }
        initListener()
        initLiveData()
    }

    private fun initLiveData() {
        viewModel.getAddStoryResponse().observe(this@AddStoryActivity) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        // TODO : add loading handler
                    }

                    is Result.Success -> {
                        showSuccessDialog()
                    }

                    else -> {
                        showErrorDialog()
                    }
                }
            }
        }
    }

    private fun getLocations() {
        val minTimeBetweenUpdates = 1000L
        val minDistanceBetweenUpdates = 100f
        val shouldUseGPS = true
        val shouldUseNetwork = true
        val shouldUsePassive = true
        val locationTracker = LocationTracker(
            minTimeBetweenUpdates,
            minDistanceBetweenUpdates,
            shouldUseGPS,
            shouldUseNetwork,
            shouldUsePassive
        )
        locationTracker.addListener(object : LocationTracker.Listener {

            override fun onLocationFound(location: Location) {
                latitude = location.latitude
                longitude = location.longitude
                locationTracker.stopListening()
            }

            override fun onProviderError(providerError: ProviderError) {
            }

        })

        if (checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestPermissionCode
            )
        }
        locationTracker.startListening(this)
    }

    private fun initListener() {
        with(addStoryBinding) {
            buttonCamera.setOnClickListener {
                saveFilePermission()
                checkCameraPermission()
            }

            buttonGallery.setOnClickListener {
                checkGalleryPermission()
            }

            buttonAdd.setOnClickListener {
                val description = edAddDescription.text.toString().trim()

                val descriptions = description.toRequestBody("text/plain".toMediaType())
                val latitudes =
                    latitude.toFloat().toString().toRequestBody("text/plain".toMediaType())
                val longitudes =
                    longitude.toFloat().toString().toRequestBody("text/plain".toMediaType())

                val filePart = MultipartBody.Part.createFormData("photo", fileName, fileRequestBody)

                Toast.makeText(this@AddStoryActivity, "udah ke klik pak!", Toast.LENGTH_SHORT)
                    .show()

                viewModel.fetchAddStoryUser(
                    resources.getString(R.string.token_text).replace("%token%", userData.token),
                    filePart,
                    descriptions,
                    latitudes,
                    longitudes
                )
            }
        }
    }

    private fun showSuccessDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.success_add_story_text),
            GlobalConstants.successAnimation
        )
        dialog.apply {
            show(fragmentTransaction, GlobalConstants.successTag)
            Handler(mainLooper).postDelayed({
                dismiss()
                Intent(this@AddStoryActivity, MainActivity::class.java).apply {
                    startActivity(this)
                }
            }, 1500L)
        }
    }

    private fun showErrorDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.failed_add_story_text),
            GlobalConstants.failedAnimation
        )
        dialog.apply {
            show(fragmentTransaction, GlobalConstants.failedTag)
            Handler(mainLooper).postDelayed({
                dismiss()
            }, 1500L)
        }
    }

    private fun getFileFromContentUri(context: Context, uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val file = createTemporaryFile(context)
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
                return file
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun createTemporaryFile(context: Context): File {
        val fileName = "temp_file"
        val directory = context.cacheDir
        return File.createTempFile(fileName, null, directory)
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.png"

        val directory =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "YourFolderName")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val imageFile = File(directory, fileName)

        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            imageFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveFilePermission() {
        Dexter.withContext(this).withPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                if (response != null) {
                    if (response.permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        savePermission = true
                    }
                }
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response != null) {
                    if (response.permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        savePermission = false
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
            }

        }).onSameThread().check()
    }

    private fun retrieveFile(file: File) {
        addStoryBinding.ivPreview.load(file)
        val image = File(file.absolutePath)
        fileName = image.name
        fileRequestBody = image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    private fun checkCameraPermission() {
        Dexter.withContext(this).withPermission(
            Manifest.permission.CAMERA
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                if (response != null) {
                    if (response.permissionName == Manifest.permission.CAMERA) {
                        actionCamera()
                    }
                }
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
            }

        }).onSameThread().check()
    }

    private fun checkGalleryPermission() {
        Dexter.withContext(this).withPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                if (response != null) {
                    if (response.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) actionGallery()
                }
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
            }

        }).onSameThread().check()
    }

    private fun actionGallery() {
        pickPhoto.launch("image/*")
    }

    private fun actionCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            takePhoto.launch(this)
        }
    }
}