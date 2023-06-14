package com.nurhaqhalim.momento.core.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LoginResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("loginResult")
    val loginResult: LoginResult,
    @SerializedName("message")
    val message: String
) : Parcelable {
    @Keep
    @Parcelize
    data class LoginResult(
        @SerializedName("name")
        val name: String,
        @SerializedName("token")
        val token: String,
        @SerializedName("userId")
        val userId: String
    ) : Parcelable
}