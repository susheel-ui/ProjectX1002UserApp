package com.example.project_b_security_gardapp.api.Entities

data class User(
    val createdAt: String,
    val enabled: Boolean,
    val fullName: String,
    val flatNumber:String,
    val id: Int,
    val phoneNumber: String,
    val role: String,
    val societyName: String,
    val updatedAt: String
)