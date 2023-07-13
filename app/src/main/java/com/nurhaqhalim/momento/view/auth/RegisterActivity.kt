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
import com.nurhaqhalim.momento.core.Result
import com.nurhaqhalim.momento.core.remote.model.RegisterRequest
import com.nurhaqhalim.momento.databinding.ActivityRegisterBinding
import com.nurhaqhalim.momento.utils.GlobalConstants
import com.nurhaqhalim.momento.viewmodel.MoVMFactory
import com.nurhaqhalim.momento.viewmodel.MoViewModel


class RegisterActivity : AppCompatActivity() {
    private lateinit var registerBinding: ActivityRegisterBinding
    private val viewModel: MoViewModel by viewModels {
        MoVMFactory(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)
        supportActionBar?.title = resources.getString(R.string.register_title_text)
        with(registerBinding) {
            registerImage.load(R.drawable.momento)
            registerTitle.text = resources.getText(R.string.app_name)
            registerDescription.text = resources.getText(R.string.register_title_text)
            btnRegister.text = resources.getText(R.string.register)
            val ss = SpannableString(resources.getText(R.string.login_text_on_register))
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                        startActivity(this)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            ss.setSpan(clickableSpan, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            loginRegister.apply {
                text = ss
                movementMethod = LinkMovementMethod.getInstance()
            }
            edRegisterPassword.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // Not used
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // Not used
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val isTextEmpty = s.isNullOrEmpty()
                        btnRegister.isEnabled = !isTextEmpty
                    }
                })
                setOnEditorActionListener { _, i, _ ->
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        val inputManager =
                            this@RegisterActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        val viewFocus = this@RegisterActivity.currentFocus
                        inputManager.hideSoftInputFromWindow(viewFocus?.windowToken, 0)
                    }
                    true
                }
            }

            btnRegister.setOnClickListener {
                showLoading()
                val email = edRegisterEmail.text.toString().trim()
                val name = edRegisterName.text.toString().trim()
                val password = edRegisterPassword.text.toString().trim()
                if (email.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty()) {
                    val registerRequest = RegisterRequest(email, name, password)
                    viewModel.fetchRegister(registerRequest)
                }
            }
        }
        initLiveData()
    }

    private fun initLiveData() {
        viewModel.getRegisterResponse().observe(this@RegisterActivity) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        showLoading()
                    }

                    is Result.Success -> {
                        hideLoading()
                        if (!result.data.error) {
                            showSuccessDialog()
                        } else {
                            showErrorDialog()
                        }
                    }

                    else -> {
                        hideLoading()
                        showErrorDialog()
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.register_success_text),
            GlobalConstants.successAnimation
        )
        dialog.apply {
            show(fragmentTransaction, GlobalConstants.successTag)
            Handler(mainLooper).postDelayed({
                dismiss()
                Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                    startActivity(this)
                }
                finish()
            }, 1500L)
        }
    }

    private fun showErrorDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val dialog = MoDialog.newInstance(
            resources.getString(R.string.register_failed_text),
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
        with(registerBinding) {
            edRegisterEmail.text = null
            edRegisterName.text = null
            edRegisterPassword.text = null
        }
    }

    private fun showLoading() {
        registerBinding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        registerBinding.loadingProgress.visibility = View.GONE
    }
}