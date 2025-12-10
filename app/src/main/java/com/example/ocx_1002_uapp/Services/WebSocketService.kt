package com.example.ocx_1002_uapp.Services

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.workers.ServiceCheckerWorker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import java.util.concurrent.TimeUnit

class WebSocketService : Service() {

    private lateinit var stompClient: StompClient
    private val compositeDisposable = CompositeDisposable()
    private var ownerId: String? = null
    private var isStompConnected = false
    private val serverUrl = "wss://gateguard.cloud/ws/websocket"
    private val handler = Handler(Looper.getMainLooper())
    private var reconnectAttempts = 0
    private var wakeLock: PowerManager.WakeLock? = null

    // network callback
    private var connectivityCallback: ConnectivityCallback? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        startAsForeground()
        acquireWakeLock()

        // load owner id
        val prefs = getSharedPreferences(Keywords.MYPREFS.toString(), Context.MODE_PRIVATE)
        ownerId = prefs.getString(Keywords.OwnerId.toString(), null)
        if (ownerId.isNullOrEmpty()) {
            Log.e(TAG, "OwnerId not found — stopping service")
            stopSelf()
            return
        }

        // init network listener
        registerNetworkCallback()

        // init STOMP
        initStompClient()
        connectStomp()

        // schedule WorkManager periodic check (defensive)
        scheduleServiceChecker()
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:ws_wake_lock")
            // Acquire for 10 minutes first; we will renew on reconnect as needed
            if (!wakeLock!!.isHeld) wakeLock!!.acquire(10 * 60 * 1000L)
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock error: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            Log.d(TAG, "WakeLock released")
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock release error: ${e.message}")
        }
    }

    private fun startAsForeground() {
        val channelId = "websocket_channel_v1"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WebSocket Service", NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(false)
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SRS Security")
            .setContentText("Always On. Always Here 24/7")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("CheckResult")
    private fun initStompClient() {
        // Use OkHttp client for stable connections and setting ping intervals
        val okHttpClient = OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS) // helps keep socket alive
            .build()

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)
        // if library allows injecting OkHttp client, use that; otherwise trust default
    }

    @SuppressLint("CheckResult")
    private fun connectStomp() {
        if (isStompConnected) {
            Log.d("STOMP", "Already connected")
            return
        }

        try {
            stompClient.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                            Log.d("STOMP", "Connected")
                            isStompConnected = true
                            reconnectAttempts = 0
                            subscribeToTopic()
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                            Log.w("STOMP", "Closed")
                            isStompConnected = false
                            scheduleReconnect()
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                            Log.e("STOMP", "Error", lifecycleEvent.exception)
                            isStompConnected = false
                            scheduleReconnect()
                        }
                        else -> {}
                    }
                }
            stompClient.connect()
        } catch (e: Exception) {
            Log.e("STOMP", "connectStomp failed: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        reconnectAttempts++
        val backoff = calculateBackoffMillis(reconnectAttempts)
        Log.d("STOMP", "Scheduling reconnect in ${backoff}ms (attempt $reconnectAttempts)")
        handler.removeCallbacks(reconnectRunnable)
        handler.postDelayed(reconnectRunnable, backoff)
    }

    private val reconnectRunnable = Runnable {
        try {
            if (!isStompConnected) {
                Log.d("STOMP", "Reconnect runnable firing")
                // disconnect previous client safely
                try {
                    if (::stompClient.isInitialized) stompClient.disconnect()
                } catch (_: Exception) {}
                initStompClient()
                connectStomp()
            }
        } catch (e: Exception) {
            Log.e("STOMP", "reconnectRunnable error: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun calculateBackoffMillis(attempts: Int): Long {
        // exponential backoff with cap
        val base = 2000L // 2s
        val cap = 60_000L // 60s
        var backoff = base * (1L shl (attempts.coerceAtMost(6))) // 2s,4s,8s...
        if (backoff > cap) backoff = cap
        return backoff
    }

    @SuppressLint("CheckResult")
    private fun subscribeToTopic() {
        try {
            if (ownerId.isNullOrEmpty()) {
                Log.w(TAG, "OwnerId missing; skipping topic subscribe")
                return
            }

            // Unsubscribe old subscriptions
            compositeDisposable.clear()

            val topic = "/topic/owner/$ownerId"
            val disp = stompClient.topic(topic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message ->
                    handleIncomingMessage(message.payload)
                }, { error ->
                    Log.e("STOMP", "Topic subscription error", error)
                })
            compositeDisposable.add(disp)
            Log.d("STOMP", "Subscribed to $topic")
        } catch (e: Exception) {
            Log.e("STOMP", "subscribeToTopic failed: ${e.message}")
        }
    }

    private fun handleIncomingMessage(payload: String) {
        Log.d("STOMP", "Received: $payload")
        try {
            val json = org.json.JSONObject(payload)
            val guestName = json.optString("guestName")
            val id = json.optString("id")
            val flatNo = json.optString("flatNumber")
            val status = json.optString("status")
            NotificationHelper.showNotification(this, id.hashCode(), "New Visitor: $guestName", "Flat: $flatNo | Status: $status")
        } catch (e: Exception) {
            Log.e("STOMP", "Failed to parse payload: ${e.message}")
        }
    }
    private fun registerNetworkCallback(){
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            connectivityCallback = ConnectivityCallback { connected ->
                if (connected) {
                    Log.d(TAG, "Network regained -> reconnecting STOMP")
                    scheduleReconnect()
                } else {
                    Log.w(TAG, "Network lost")
                }
            }
            val request = android.net.NetworkRequest.Builder().build()
            cm.registerNetworkCallback(request, connectivityCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "registerNetworkCallback error: ${e.message}")
        }
    }

    private fun unregisterNetworkCallback() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            connectivityCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ensure service continues
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        handler.removeCallbacks(reconnectRunnable)
        compositeDisposable.dispose()
        try {
            if (::stompClient.isInitialized) stompClient.disconnect()
        } catch (_: Exception) {}
        unregisterNetworkCallback()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun scheduleServiceChecker() {
        // schedule WorkManager periodic job to check service every 15 minutes
        try {
            val workRequest = PeriodicWorkRequestBuilder<ServiceCheckerWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "ServiceCheckerWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "scheduleServiceChecker error: ${e.message}")
        }
    }

    // Helper network callback class
    private class ConnectivityCallback(private val onConnectivityChanged: (Boolean) -> Unit) : android.net.ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            onConnectivityChanged(true)
        }
        override fun onLost(network: android.net.Network) {
            onConnectivityChanged(false)
        }
    }
}
