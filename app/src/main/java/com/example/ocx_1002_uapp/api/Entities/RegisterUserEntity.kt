package com.example.ocx_1002_uapp.api.Entities

data class RegisterUserEntity(
    val flatNumber: String,
    val fullName: String,
    val password: String,
    val phoneNumber: String,
    val societyCode: String
)