package com.example.project_b_security_gardapp.api.Responses

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(
    @SerializedName("token"       ) var token       : String? = null,
    @SerializedName("role"        ) var role        : String? = null,
    @SerializedName("fullName"    ) var fullName    : String? = null,
    @SerializedName("phoneNumber" ) var phoneNumber : String? = null,
    @SerializedName("userId"      ) var userId      : Int?    = null,
    @SerializedName("flatNumber"  ) var flatNumber  : String? = null,
    @SerializedName("message"     ) var message     : String? = null

)
