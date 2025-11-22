package com.example.ocx_1002_uapp.Services

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.Notifications.NotificationHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import java.net.URI

class WebSocketService : Service() {

    private var webSocketClient: WebSocketClient? = null
    private lateinit var stompClient: StompClient
    private val compositeDisposable = CompositeDisposable()
    private var ownerId: String? = null
//    private lateinit var ownerChangeReceiver: BroadcastReceiver
    private var isStompConnected = false // ✅ Prevent duplicate subscriptions

    // ✅ Your WebSocket URL (for AVD)
    private val serverUrl = "ws://10.0.2.2/ws/websocket"

    @SuppressLint("CheckResult", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        startAsForeground()

        // ✅ Load OwnerId first
        val sharedPreferences = getSharedPreferences(Keywords.MYPREFS.toString(), MODE_PRIVATE)
        ownerId = sharedPreferences.getString(Keywords.OwnerId.toString(), null)

        if (ownerId.isNullOrEmpty()) {
            Log.e(TAG, "❌ OwnerId not found — stopping service")
            stopSelf()
            return
        }

        Log.d(TAG, "✅ onCreate: OwnerId = $ownerId")

        // ✅ Connect WebSocket + STOMP once
        connectWebSocket()
        connectStomp()

//        // ✅ Register receiver for Owner ID change
//        ownerChangeReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                val newOwnerId = intent?.getStringExtra("NEW_OWNER_ID")
//                if (!newOwnerId.isNullOrEmpty() && newOwnerId != ownerId) {
//                    Log.d("WebSocketService", "🔄 OwnerId changed: $ownerId → $newOwnerId")
//                    ownerId = newOwnerId
//                    reconnectConnections()
//                }
//            }
//        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            registerReceiver(ownerChangeReceiver, IntentFilter("OWNER_ID_CHANGED"), Context.RECEIVER_NOT_EXPORTED)
//        } else {
//            registerReceiver(ownerChangeReceiver, IntentFilter("OWNER_ID_CHANGED"))
//        }
    }

    /** ✅ Foreground Notification Setup */
    private fun startAsForeground() {
        val channelId = "websocket_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "WebSocket Service",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gate Security")
            .setContentText("Listening for visitor updates...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    /** ✅ Simple WebSocket client connection */
    private fun connectWebSocket() {
        val uri = URI(serverUrl)
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "✅ Connected to WebSocket server")
                send("SUBSCRIBE /topic/owner/$ownerId")
            }

            override fun onMessage(message: String?) {
                message?.let {
                    Log.d(TAG, "📩 WebSocket Message: $it")
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.w(TAG, "⚠️ WebSocket Closed: $reason — reconnecting in 5s")
                reconnectLater()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "❌ WebSocket Error: ${ex?.message}")
                reconnectLater()
            }
        }
        webSocketClient?.connect()
    }

    /** ✅ STOMP connection with single active subscription */
    @SuppressLint("CheckResult")
    private fun connectStomp() {
        if (isStompConnected) {
            Log.d("STOMP", "⚠️ Already connected — skipping re-init")
            return
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)

        stompClient.lifecycle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                when (event.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        Log.d("STOMP", "✅ Connected")
                        isStompConnected = true
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        Log.e("STOMP", "❌ Error", event.exception)
                        isStompConnected = false
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        Log.d("STOMP", "⚠️ Closed")
                        isStompConnected = false
                    }
                    else -> {}
                }
            }

        stompClient.connect()

        compositeDisposable.add(
            stompClient.topic("/topic/owner/$ownerId")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    handleIncomingMessage(topicMessage.payload)
                }, { error ->
                    Log.e("STOMP", "Error in subscription", error)
                })
        )
    }

    /** ✅ Handles incoming messages cleanly */
    private fun handleIncomingMessage(payload: String) {
        Log.d("STOMP", "📩 Received: $payload")

        try {
            val jsonObject = JSONObject(payload)

            val guestName = jsonObject.optString("guestName")
            val id = jsonObject.optString("id")
            val flatNo = jsonObject.optString("flatNumber")
            val status = jsonObject.optString("status")

            Log.d("STOMP", "Guest: $guestName, Flat: $flatNo, Status: $status")

            NotificationHelper.showNotification(
                this,
                id,
                "New Visitor: $guestName",
                "Flat: $flatNo | Status: $status"
            )
        } catch (e: Exception) {
            Log.e("STOMP", "❌ Failed to parse message: ${e.message}")
        }
    }

    /** ✅ Clean Reconnect */
    private fun reconnectConnections() {
        try {
            Log.d(TAG, "♻️ Reconnecting all connections with new OwnerId: $ownerId")
            webSocketClient?.close()
            compositeDisposable.clear()
            if (::stompClient.isInitialized) stompClient.disconnect()
            isStompConnected = false
            connectWebSocket()
            connectStomp()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during reconnection: ${e.message}")
        }
    }

    private fun reconnectLater() {
        Thread {
            Thread.sleep(5000)
            connectWebSocket()
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ✅ Removed duplicate connectStomp() here
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "🛑 Service destroyed")
        webSocketClient?.close()
        compositeDisposable.dispose()
        if (::stompClient.isInitialized) stompClient.disconnect()
//        unregisterReceiver(ownerChangeReceiver)
        isStompConnected = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
