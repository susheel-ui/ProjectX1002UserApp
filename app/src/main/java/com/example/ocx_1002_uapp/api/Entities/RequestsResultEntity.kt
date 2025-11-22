package com.example.project_b_security_gardapp.api.Entities

data class RequestsResultEntity(
    val createdAt: String,
    val id: Int,
    val guestName: String,
    val phoneNumber: String,
    val description: String,
    val status: String,
    val ownerName: String,
    val guardName: String,
    val societyName: String,
    val photo1: String?,
    val photo2: String?,
    val ownerId: Int,
    val guardId: Int,
    val flatNumber: String,
    val updatedAt: String
)
