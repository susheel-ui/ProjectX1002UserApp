package com.example.ocx_1002_uapp.api.Entities

data class entityAdd(
    val enable: Boolean,
    val images: List<Image>
)

data class Image(
    val name: String,
    val url: String
)