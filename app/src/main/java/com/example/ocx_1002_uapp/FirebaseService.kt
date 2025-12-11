package com.example.ocx_1002_uapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.ocx_1002_uapp.Services.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService :  FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Token :", "onNewToken: $token ")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            NotificationHelper.showNotification(applicationContext, 0,it.title.toString(), it.body.toString())
        }

    }
    private fun sendTokenToServer(token: String) {
        // TODO: API call to save token on server
    }
}