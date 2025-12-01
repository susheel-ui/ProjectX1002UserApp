package com.example.ocx_1002_uapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.ActivityMainBinding
import com.example.project_b_security_gardapp.api.Entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Handler().postDelayed({
            val sharedPreferences: SharedPreferences = getSharedPreferences(Keywords.MYPREFS.toString(), MODE_PRIVATE)
            val token = sharedPreferences.getString(Keywords.USERTOKEN.toString(), null)

            if (token == null) {
                startActivity(Intent(this, LoginActivity::class.java))  // we will redirect to LoginActivity
                finish()
            } else {
                val service = API.getInstance.create(UserServices::class.java)
                val repo  = UserRepository(service)
                var response: Response<User>? = null
                val job = CoroutineScope(Dispatchers.IO).launch {
                    response =  repo.GetUserBytoken(token.toString())
                }
                job.invokeOnCompletion {
                    if(response?.code() == 200){
                        startActivity(Intent(this, Home_Acitvity::class.java))
                        finish()
                    }else{
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
//
            }

        },2000)
    }
}