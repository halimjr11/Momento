package com.nurhaqhalim.momento.view.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.components.MoDialog
import com.nurhaqhalim.momento.core.model.LoginRequest
import com.nurhaqhalim.momento.databinding.ActivityLoginBinding
import com.nurhaqhalim.momento.model.UserData
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.utils.StorageHelper
import com.nurhaqhalim.momento.view.home.MainActivity
import com.nurhaqhalim.momento.viewmodel.MoViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var authBinding: ActivityLoginBinding
    private val viewModel: MoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(authBinding.root)
        with(authBinding) {
            loginImage.load(R.drawable.momento)
            loginTitle.text = resources.getText(R.string.app_name)
            loginDescription.text = resources.getText(R.string.login_page_desc)
            btnLogin.text = resources.getText(R.string.login)
            val ss = SpannableString(resources.getText(R.string.register_text_on_login))
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Intent(this@LoginActivity, RegisterActivity::class.java).apply {
                        startActivity(this)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            ss.setSpan(clickableSpan, 19, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            loginRegister.apply {
                text = ss
                movementMethod = LinkMovementMethod.getInstance()
            }
            edLoginPassword.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // Not used
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // Not used
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val isTextEmpty = s.isNullOrEmpty()
                        btnLogin.isEnabled = !isTextEmpty
                    }
                })
                setOnEditorActionListener { _, i, _ ->
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        val viewFocus = currentFocus
                        inputManager.hideSoftInputFromWindow(viewFocus?.windowToken, 0)
                    }
                    true
                }
            }

            btnLogin.setOnClickListener {
                showLoading()
                val email = edLoginEmail.text.toString().trim()
                val password = edLoginPassword.text.toString().trim()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    val loginRequest = LoginRequest(email, password)
                    viewModel.fetchLogin(loginRequest).observe(this@LoginActivity) {
                        hideLoading()
                        if (!it.error) {
                            val userData = UserData(
                                it.loginResult.name, it.loginResult.token, it.loginResult.userId
                            )
                            StorageHelper.saveUserLogin(this@LoginActivity, userData)
                            showSuccessDialog()
                        } else {
                            showErrorDialog()
                        }
                    }
                    viewModel.errorLogin.observe(this@LoginActivity) {
                        if (it) {
                            hideLoading()
                            showErrorDialog()
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.login_success_text),
            GlobalConstants.successAnimation
        )
        dialog.apply {
            show(fragmentTransaction, GlobalConstants.successTag)
            Handler(mainLooper).postDelayed({
                dismiss()
                Intent(this@LoginActivity, MainActivity::class.java).apply {
                    startActivity(this)
                }
                finish()
            }, 1500L)
        }
    }

    private fun showErrorDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.login_failed_text),
            GlobalConstants.failedAnimation
        )
        dialog.apply {
            show(fragmentTransaction, GlobalConstants.failedTag)
            Handler(mainLooper).postDelayed({
                dismiss()
                resetForm()
            }, 1500L)
        }
    }

    private fun resetForm() {
        with(authBinding) {
            edLoginEmail.text = null
            edLoginPassword.text = null
        }
    }

    private fun showLoading() {
        authBinding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        authBinding.loadingProgress.visibility = View.GONE
    }
}