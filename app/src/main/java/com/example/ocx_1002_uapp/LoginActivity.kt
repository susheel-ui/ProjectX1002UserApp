package com.example.ocx_1002_uapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ocx_1002_uapp.Services.WebSocketService
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.ActivityLoginBinding
import com.example.ocx_1002_uapp.workers.ServiceCheckerWorker
import com.example.project_b_security_gardapp.api.Entities.userLoginEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var repo: UserRepository
    lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        val apiInstance = API.getInstance
        val userService = apiInstance.create(UserServices::class.java)
        try {
            repo = UserRepository(userService)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate LoginActivity repo problem : ${e.message}")
        }

        sharedPreferences = getSharedPreferences(Keywords.MYPREFS.toString(), Context.MODE_PRIVATE)
        // ✅ Ask notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }


// logIn button Action
        binding.buttonLogin.setOnClickListener {
            if (binding.mobileNumber.text.toString()
                    .isNotEmpty() && binding.passwordET.text.toString().isNotEmpty()
            ) {

                val mobile = "+91".plus(binding.mobileNumber.text.toString())
                val password = binding.passwordET.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    val jsonBody = userLoginEntity(mobile, password)
                    val result = repo.login(jsonBody)
                    if (result.isSuccessful && result.code() == 200) {
                        Log.d(TAG, "onCreate: ${result.body()}")
                        val editor = sharedPreferences.edit()
                        editor.putString(
                            Keywords.USERTOKEN.toString(),
                            result.body()!!.token.toString()
                        )
                        editor.putString(
                            Keywords.OwnerId.toString(),
                            result.body()!!.userId.toString()
                        )
                        editor.apply()
                        
                        CoroutineScope(Dispatchers.Main).launch {
                            val intent = Intent(applicationContext, WebSocketService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                            requestBatteryOptimizationPermission()
                            startActivity(Intent(applicationContext, Home_Acitvity::class.java))
                            finish()
                        }
//                        val intent = Intent("OWNER_ID_CHANGED")
//                        intent.putExtra("NEW_OWNER_ID",result.body()!!.userId.toString())
//                        sendBroadcast(intent)
                        // ✅ Start foreground service
//                        val intent = Intent("OWNER_ID_CHANGED")
//                        intent.putExtra("NEW_OWNER_ID", result.body()!!.userId.toString())
//                        sendBroadcast(intent)
                    }
                    else{
                        Log.d(TAG, "onCreate: ${result.code()}")
                        withContext(Dispatchers.Main) {
                            binding.TvMessage.visibility = View.VISIBLE
                            binding.TvMessage.text = "Invalid Mobile Or Password"
                        }
                    }
                }
            } else {
                binding.mobileNumber.error = "Please Enter Email"
                binding.passwordET.error = "Please Enter Password"
            }
        }
//Create Account Action
        binding.textCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.TvForgottenPsw.setOnClickListener {
            startActivity(Intent(this,Services_Contact_Activity::class.java))
        }
    }

    private fun requestBatteryOptimizationPermission() {
        val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val packageNameStr = packageName
        try {
            if (!pm.isIgnoringBatteryOptimizations(packageNameStr)) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageNameStr")
                startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback: open battery settings
            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }

}