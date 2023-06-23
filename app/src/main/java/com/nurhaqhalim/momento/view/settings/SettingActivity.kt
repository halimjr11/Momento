package com.nurhaqhalim.momento.view.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.databinding.ActivitySettingBinding
import com.nurhaqhalim.momento.utils.DarkMode
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.SettingUtils
import com.nurhaqhalim.momento.view.adapter.MoArrayAdapter

class SettingActivity : AppCompatActivity() {
    private lateinit var settingBinding: ActivitySettingBinding
    private lateinit var themeAdapter: MoArrayAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingBinding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(settingBinding.root)
        with(settingBinding) {
            initThemeSettings()
            languagePicker.apply {
                setOnClickListener {
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intent)
                }
            }
        }
    }

    private fun initThemeSettings() {
        val settingThemeModel = GlobalConstants.getThemeModel()
        val themeTitleList = settingThemeModel.title.map { resources.getString(it) }.toList()
        themeAdapter =
            MoArrayAdapter(this@SettingActivity, themeTitleList, settingThemeModel.icon)
        with(settingBinding) {
            themePicker.apply {
                text = when (SettingUtils.getTheme()) {
                    AppCompatDelegate.MODE_NIGHT_YES -> {
                        Editable.Factory.getInstance()
                            .newEditable(resources.getString(R.string.setting_theme_dark))
                    }

                    AppCompatDelegate.MODE_NIGHT_NO -> {
                        Editable.Factory.getInstance()
                            .newEditable(resources.getString(R.string.setting_theme_light))
                    }

                    else -> {
                        Editable.Factory.getInstance()
                            .newEditable(resources.getString(R.string.setting_theme_system))
                    }
                }
                setOnItemClickListener { adapterView, view, position, id ->
                    when (settingThemeModel.title[position]) {
                        R.string.setting_theme_system -> {
                            SettingUtils.updateTheme(
                                this@SettingActivity,
                                DarkMode.FOLLOW_SYSTEM.value
                            )
                        }

                        R.string.setting_theme_light -> {
                            SettingUtils.updateTheme(this@SettingActivity, DarkMode.OFF.value)
                        }

                        R.string.setting_theme_dark -> {
                            SettingUtils.updateTheme(this@SettingActivity, DarkMode.ON.value)
                        }
                    }
                }
                setAdapter(themeAdapter)
            }
        }
    }

    override fun onResume() {
        initThemeSettings()
        super.onResume()
    }
}