package com.nurhaqhalim.momento.view.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.databinding.ActivityMainBinding
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.MarginItemDecoration
import com.nurhaqhalim.momento.utils.MoIdlingResource
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.utils.StorageHelper.set
import com.nurhaqhalim.momento.view.adapter.MoLoadingStateAdapter
import com.nurhaqhalim.momento.view.adapter.MoPagingAdapter
import com.nurhaqhalim.momento.view.auth.LoginActivity
import com.nurhaqhalim.momento.view.maps.MapsActivity
import com.nurhaqhalim.momento.view.settings.SettingActivity
import com.nurhaqhalim.momento.view.story.add.AddStoryActivity
import com.nurhaqhalim.momento.view.story.detail.DetailActivity
import com.nurhaqhalim.momento.viewmodel.MoVMFactory
import com.nurhaqhalim.momento.viewmodel.MoViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var storyAdapter: MoPagingAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var userData: UserData
    private lateinit var locationManager: LocationManager
    private val viewModel: MoViewModel by viewModels {
        MoVMFactory(this)
    }
    private val minDistanceForUpdates: Float = 10f
    private val minTimesBetweenUpdates: Long = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        checkLocationPermission()
        userData = StorageHelper.getUserData(this)
        supportActionBar?.title = resources.getText(R.string.app_name)
        storyAdapter = MoPagingAdapter()
        concatAdapter = ConcatAdapter(storyAdapter, MoLoadingStateAdapter { storyAdapter.retry() })
        fetchData()
        initView()
        initListener()
    }

    private fun initView() {
        with(mainBinding) {
            showLoading()
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = concatAdapter
                hasFixedSize()
                addItemDecoration(MarginItemDecoration(40, 15))
            }
            fabAddStory.setOnClickListener {
                getLocation()
                Intent(this@MainActivity, AddStoryActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    private fun initListener() {
        storyAdapter
            .setOnItemClickListener(object : MoPagingAdapter.OnClickListener {
                override fun onItemClicked(data: StoryModel) {
                    Intent(this@MainActivity, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.STORY_DATA, data)
                        startActivity(this)
                    }
                }
            })
        mainBinding.reloadButton.setOnClickListener { fetchData() }
    }

    private fun checkLocationPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (it.areAllPermissionsGranted()) {
                            getLocation()
                        } else {
                            checkLocationPermission()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                StorageHelper.saveLocation(this@MainActivity, location)
                locationManager.removeUpdates(this)
            }
        }

        try {
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)

            if (provider != null) {
                locationManager.requestLocationUpdates(
                    provider,
                    minTimesBetweenUpdates,
                    minDistanceForUpdates,
                    locationListener
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun fetchData() {
        viewModel.fetchPagingList().observe(this) {
            hideLoading()
            storyAdapter.submitData(lifecycle, it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_maps -> {
                Intent(this, MapsActivity::class.java).apply {
                    startActivity(this)
                }
            }

            R.id.action_setting -> {
                Intent(this, SettingActivity::class.java).apply {
                    startActivity(this)
                }
            }

            R.id.action_logout -> {
                MoIdlingResource.increment()
                StorageHelper.resetUserData(this@MainActivity)
                val storageHelper = StorageHelper.customStorage(this, GlobalConstants.storageName)
                storageHelper[GlobalConstants.keyLogin] = false
                MoIdlingResource.decrement()
                Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(this)
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showLoading() {
        GlobalConstants.show(mainBinding.loadingProgress, mainBinding.recyclerView)
    }

    private fun hideLoading() {
        GlobalConstants.hideView(mainBinding.loadingProgress, mainBinding.recyclerView)
    }
}