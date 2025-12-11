package com.example.ocx_1002_uapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object API {
    val Url = "http://192.168.29.160:8080/"
//    val Url = "https://gateguard.cloud/"
    val getInstance by lazy {
        Retrofit.Builder().baseUrl(Url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}