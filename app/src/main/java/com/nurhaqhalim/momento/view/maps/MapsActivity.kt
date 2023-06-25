package com.nurhaqhalim.momento.view.maps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.core.Result
import com.nurhaqhalim.momento.databinding.ActivityMapsBinding
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.viewmodel.MoViewModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var boundsBuilder: LatLngBounds.Builder
    private lateinit var userData: UserData
    private lateinit var mapsBinding: ActivityMapsBinding
    private val storyList: ArrayList<StoryModel> = arrayListOf()
    private val viewModel: MoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)
        supportActionBar?.title = resources.getString(R.string.maps_title_text)
        userData = StorageHelper.getUserData(this)
        boundsBuilder = LatLngBounds.builder()
        initView()
        fetchData()
        initLiveData()
        initListener()
    }

    private fun initListener() {
        mapsBinding.reloadButton.setOnClickListener {
            fetchData()
        }
    }

    private fun initLiveData() {
        viewModel.getStoryResponse().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        showLoading()
                    }

                    is Result.Success -> {
                        hideLoading()
                        storyList.addAll(result.data)
                    }

                    else -> {
                        hideLoading()
                        showError()
                    }
                }
            }
        }
    }

    private fun showError() {
        GlobalConstants.show(
            mapsBinding.errorState,
            mapsBinding.loadingProgress,
            mapsBinding.mapStory
        )
    }

    private fun fetchData() {
        viewModel.fetchStories(
            resources.getString(R.string.token_text).replace("%token%", userData.token),
            1,
            10,
            1
        )
    }

    private fun initView() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_story) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(maps: GoogleMap) {
        googleMap = maps
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
            initMapsView(googleMap)

        }
    }

    private fun initMapsView(googleMap: GoogleMap) {
        storyList.forEach { story ->
            val latLng = LatLng(story.lat, story.lon)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description)
                    .icon(drawableToBitmap(R.drawable.ic_maps_point))
            )
            boundsBuilder.include(latLng)
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Snackbar.make(mapsBinding.root, "Parsing style failed", Snackbar.LENGTH_SHORT)
                    .show()
            }
        } catch (exception: Resources.NotFoundException) {
            Snackbar.make(
                mapsBinding.root,
                "Can't find style. Error: $exception",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun drawableToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val resourceDrawable = ResourcesCompat.getDrawable(resources, id, null)
            ?: return BitmapDescriptorFactory.defaultMarker()
        val bitmap = Bitmap.createBitmap(
            resourceDrawable.intrinsicWidth,
            resourceDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        resourceDrawable.setBounds(0, 0, canvas.width, canvas.height)
        resourceDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    private fun showLoading() {
        GlobalConstants.show(mapsBinding.loadingProgress, mapsBinding.mapStory)
    }

    private fun hideLoading() {
        GlobalConstants.hideView(mapsBinding.loadingProgress, mapsBinding.mapStory)
    }
}