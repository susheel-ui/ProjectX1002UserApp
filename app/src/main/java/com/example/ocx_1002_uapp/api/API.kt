package com.example.ocx_1002_uapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object API {
    val getInstance by lazy {
        Retrofit.Builder().baseUrl("https://gateguard.cloud/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}