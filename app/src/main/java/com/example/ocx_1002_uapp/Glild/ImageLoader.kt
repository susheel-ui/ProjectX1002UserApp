package com.example.ocx_1002_uapp.Glild

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders


class ImageLoader {
    public fun loadImageWithAuth(
        context: Context?,
        imageUrl: String?,
        authToken: String,
        imageView: ImageView?
    ) {
        try {
            val glideUrl = GlideUrl(
                imageUrl, LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()
            )

            Glide.with(context!!)
                .load(glideUrl)
                .into(imageView!!)
            Log.d("Success", "loadImageWithAuth: ok ")
        } catch (e: Exception) {
            Log.d("Error", "loadImageWithAuth error on Loading: ${e.message}")
        }
    }
}