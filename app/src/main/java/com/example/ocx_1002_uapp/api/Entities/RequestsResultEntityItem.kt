package com.example.project_b_security_gardapp.api.Entities

data class RequestsResultEntityItem(
    val createdAt: String,
    val description: String,
    val guardId: Int,
    val guestName: String,
    val id: Int,
    val ownerId: Int,
    val phoneNumber: String,
    val photo1: String,
    val photo2: String,
    val status: String,
    val updatedAt:String
)