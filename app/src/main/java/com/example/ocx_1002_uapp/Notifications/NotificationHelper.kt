package com.example.ocx_1002_uapp.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ocx_1002_uapp.MainActivity
import com.example.ocx_1002_uapp.R

object NotificationHelper {
    private const val CHANNEL_ID = "visitor_alerts"

    fun showNotification(context: Context, id: Int, title: String, text: String) {
        // Correct Raw Sound URI
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.doorbell_223669}")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Create Channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Visitor Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for visitors"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, attributes)   // Sound applied here
            }

            nm.createNotificationChannel(channel)
        }

        // 👉 Intent to open app when notification clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build Notification (NO setSound here; channel already controls it)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.guests_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
//            .setSound(soundUri,AudioManager.STREAM_ALARM)
//            .setSound(soundUri, AudioManager.STREAM_ALARM)
//            .setSound(soundUri, AudioManager.STREAM_ALARM)
            .build()


        playLongRingtone(context)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(id,notification)
        }
    }
    fun playLongRingtone(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.doorbell_223669)
        mediaPlayer.isLooping = false   // true if you want looping
        mediaPlayer.start()
        var playCount = 1    // currently playing 1st time
        mediaPlayer.setOnCompletionListener {
            if (playCount < 3) {
                playCount++
                mediaPlayer.start()   // play again
            } else {
                mediaPlayer.release() // stop completely after 3rd time
            }
        }

    }
}
