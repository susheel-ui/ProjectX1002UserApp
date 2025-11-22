package com.example.project_b_security_gardapp.api.Entities

import com.google.gson.annotations.SerializedName

data class userLoginEntity(
    @SerializedName("phoneNumber") var phoneNumber: String? = null,
    @SerializedName("password") var password: String? = null,
    )