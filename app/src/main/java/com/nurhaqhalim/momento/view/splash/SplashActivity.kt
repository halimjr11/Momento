package com.nurhaqhalim.momento.view.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.nurhaqhalim.momento.databinding.ActivitySplashBinding
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.utils.StorageHelper.get
import com.nurhaqhalim.momento.view.auth.LoginActivity
import com.nurhaqhalim.momento.view.home.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var splashBinding: ActivitySplashBinding
    private lateinit var storageHelper: SharedPreferences
    private val splashTimeOut = 4000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        storageHelper = StorageHelper.customStorage(this, GlobalConstants.storageName)
        val isLogin = storageHelper[GlobalConstants.keyLogin, false]

        Handler(mainLooper).postDelayed({
            when {
                isLogin!! -> {
                    Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(this)
                    }
                }

                else -> {
                    Intent(this, LoginActivity::class.java).apply {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(this)
                    }
                }
            }
            finish()
        }, splashTimeOut)
    }
}