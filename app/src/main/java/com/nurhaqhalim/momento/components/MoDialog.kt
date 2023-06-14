package com.nurhaqhalim.momento.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.databinding.CustomDialogBinding


class MoDialog : DialogFragment() {
    private lateinit var customDialogBinding: CustomDialogBinding
    private var text = ""
    private var animationName = ""

    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_ANIMATION = "KEY_ANIMATION"

        fun newInstance(title: String, animation: Int): MoDialog {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putInt(KEY_ANIMATION, animation)
            val fragment = MoDialog()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        customDialogBinding = CustomDialogBinding.inflate(inflater)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)
        return customDialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(customDialogBinding)
    }

    private fun setupView(customDialogBinding: CustomDialogBinding) {
        with(customDialogBinding) {
            textDialog.text = arguments?.getString(KEY_TITLE)
            imageDialog.apply {
                setAnimation(arguments?.getInt(KEY_ANIMATION) ?: 0)
                loop(true)
                playAnimation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }
}