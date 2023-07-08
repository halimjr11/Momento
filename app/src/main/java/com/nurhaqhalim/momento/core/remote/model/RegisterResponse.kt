package com.nurhaqhalim.momento.core.remote.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RegisterResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("message")
    val message: String
) : Parcelable