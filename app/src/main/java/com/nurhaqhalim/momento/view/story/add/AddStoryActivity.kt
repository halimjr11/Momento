package com.nurhaqhalim.momento.view.story.add

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.components.MoDialog
import com.nurhaqhalim.momento.core.Result
import com.nurhaqhalim.momento.databinding.ActivityAddStoryBinding
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.view.home.MainActivity
import com.nurhaqhalim.momento.viewmodel.MoVMFactory
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


class AddStoryActivity : AppCompatActivity() {
    private lateinit var addStoryBinding: ActivityAddStoryBinding
    private lateinit var fileRequestBody: RequestBody
    private lateinit var locationTracker: LocationTracker
    private lateinit var pickPhoto: ActivityResultLauncher<Intent>
    private lateinit var takePhoto: ActivityResultLauncher<Intent>
    private lateinit var userData: UserData
    private var fileName: String = ""
    private val viewModel: MoViewModel by viewModels {
        MoVMFactory(this)
    }
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val requestPermissionCode: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addStoryBinding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(addStoryBinding.root)
        supportActionBar?.title = resources.getString(R.string.add_story_title_text)
        getLocations()
        userData = StorageHelper.getUserData(this)
        takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uriString = it.data?.getStringExtra(GlobalConstants.cameraTag)
                val uri: Uri = Uri.parse(uriString)
                val file = getFileFromContentUri(this, uri)
                if (file != null) {
                    retrieveFile(file)
                }
            }
        }

        pickPhoto =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri: Uri = result.data?.data as Uri
                    val file = getFileFromContentUri(this, uri)
                    if (file != null) {
                        retrieveFile(file)
                    }
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
        locationTracker = LocationTracker(
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
                addStoryBinding.locationText.text =
                    GlobalConstants.getAddress(this@AddStoryActivity, latitude, longitude)
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
        var locationChoice = false
        with(addStoryBinding) {
            buttonCamera.setOnClickListener {
                actionCamera()
            }

            buttonGallery.setOnClickListener {
                if (checkMediaPermissions()) actionGallery() else checkGalleryPermission()
            }

            locationState.setOnCheckedChangeListener { _, isChecked ->
                locationChoice = isChecked
            }

            buttonAdd.setOnClickListener {
                if (fileName.isEmpty()) {
                    Snackbar.make(
                        addStoryBinding.root,
                        "Tidak ada gambar terpilih!",
                        Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(resources.getColor(R.color.fab_bg, null)).show()
                    return@setOnClickListener
                }

                val description = edAddDescription.text.toString().trim()

                if (description.isEmpty()) {
                    edAddDescription.error = resources.getString(R.string.validation_required_text)
                    Snackbar.make(
                        addStoryBinding.root,
                        "Description is required",
                        Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(resources.getColor(R.color.fab_bg, null)).show()
                    return@setOnClickListener
                }

                val descriptions = description.toRequestBody("text/plain".toMediaType())
                val latitudes =
                    latitude.toFloat().toString().toRequestBody("text/plain".toMediaType())
                val longitudes =
                    longitude.toFloat().toString().toRequestBody("text/plain".toMediaType())

                val filePart = MultipartBody.Part.createFormData("photo", fileName, fileRequestBody)


                viewModel.fetchAddStoryUser(
                    resources.getString(R.string.token_text).replace("%token%", userData.token),
                    filePart,
                    descriptions,
                    if (locationChoice) latitudes else null,
                    if (locationChoice) longitudes else null
                )
            }
        }
    }

    private fun checkMediaPermissions(): Boolean {
        var allPermissionsGranted = true

        for (permission in if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) mediaPermissions else mediaPermission) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                allPermissionsGranted = false
                break
            }
        }
        return allPermissionsGranted
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

    private fun retrieveFile(file: File) {
        addStoryBinding.ivPreview.load(file)
        val image = File(file.absolutePath)
        fileName = image.name
        fileRequestBody = image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sdk33MediaPermission()
        } else {
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
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sdk33MediaPermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                if (p0?.grantedPermissionResponses != null) {
                    actionGallery()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                TODO("Not yet implemented")
            }


        }).onSameThread().check()
    }

    private fun actionGallery() {
        val intentGallery = Intent()
        intentGallery.action = Intent.ACTION_GET_CONTENT
        intentGallery.type = "image/*"

        val chooseImage =
            Intent.createChooser(intentGallery, resources.getString(R.string.button_gallery_text))
        pickPhoto.launch(chooseImage)
    }

    private fun actionCamera() {
        val cameraXIntent = Intent(this@AddStoryActivity, CameraActivity::class.java)
        takePhoto.launch(cameraXIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopListening()
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val mediaPermissions = arrayListOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        val mediaPermission = arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}