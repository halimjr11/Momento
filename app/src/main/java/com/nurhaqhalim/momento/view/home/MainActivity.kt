package com.nurhaqhalim.momento.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.utils.StorageHelper.set
import com.nurhaqhalim.momento.view.adapter.MoStoryAdapter
import com.nurhaqhalim.momento.view.auth.LoginActivity
import com.nurhaqhalim.momento.view.settings.SettingActivity
import com.nurhaqhalim.momento.view.story.add.AddStoryActivity
import com.nurhaqhalim.momento.view.story.detail.DetailActivity
import com.nurhaqhalim.momento.viewmodel.MoViewModel

@SuppressLint("NotifyDataSetChanged")
class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var storyAdapter: MoStoryAdapter
    private lateinit var userData: UserData
    private lateinit var locationManager: LocationManager
    private val viewModel: MoViewModel by viewModels()
    private val listData = arrayListOf<StoryModel?>()
    private val minDistanceForUpdates: Float = 10f
    private val minTimesBetweenUpdates: Long = 1000
    private var page = 1
    private var currentSize = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        checkLocationPermission()
        userData = StorageHelper.getUserData(this)
        supportActionBar?.title = resources.getText(R.string.app_name)
        storyAdapter = MoStoryAdapter()
        storyAdapter.setData(listData)
        with(mainBinding) {
            showLoading()
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = storyAdapter
                hasFixedSize()
                addItemDecoration(MarginItemDecoration(40, 15))
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                        if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listData.size - 1) {
                            loadMore()
                        }
                    }
                })
            }
            fetchData()
            fabAddStory.setOnClickListener {
                getLocation()
                Intent(this@MainActivity, AddStoryActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
        storyAdapter
            .setOnItemClickListener(object : MoStoryAdapter.onClickListener {
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
        viewModel.fetchStories(
            resources.getString(R.string.token_text).replace("%token%", userData.token),
            page,
            currentSize
        ).observe(this) {
            if (it.isNotEmpty()) {
                GlobalConstants.show(
                    mainBinding.recyclerView,
                    mainBinding.emptyState,
                    mainBinding.errorState
                )
                listData.addAll(it)
                storyAdapter.notifyDataSetChanged()
            } else {
                GlobalConstants.show(
                    mainBinding.emptyState,
                    mainBinding.recyclerView,
                    mainBinding.errorState
                )
            }
            hideLoading()
        }
        viewModel.errorStory.observe(this) {
            GlobalConstants.show(
                mainBinding.errorState,
                mainBinding.recyclerView,
                mainBinding.emptyState
            )
            hideLoading()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_setting -> {
                Intent(this, SettingActivity::class.java).apply {
                    startActivity(this)
                }
            }

            R.id.action_logout -> {
                StorageHelper.resetUserData(this@MainActivity)
                val storageHelper = StorageHelper.customStorage(this, GlobalConstants.storageName)
                storageHelper[GlobalConstants.keyLogin] = false
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

    private fun loadMore() {
        listData.add(null)
        storyAdapter.notifyItemInserted(listData.size - 1)
        Handler(mainLooper).postDelayed({
            page++
            viewModel.fetchStories(
                resources.getString(R.string.token_text).replace("%token%", userData.token),
                page,
                currentSize
            ).observe(this) {
                listData.remove(null)
                val scrollPosition: Int = listData.size
                storyAdapter.notifyItemRemoved(scrollPosition)

                listData.addAll(it)
                storyAdapter.notifyDataSetChanged()
            }
        }, 1000L)
    }

    private fun showLoading() {
        GlobalConstants.show(mainBinding.loadingProgress, mainBinding.recyclerView)
    }

    private fun hideLoading() {
        GlobalConstants.hideView(mainBinding.loadingProgress, mainBinding.recyclerView)
    }
}