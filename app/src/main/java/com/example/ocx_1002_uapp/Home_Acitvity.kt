package com.example.ocx_1002_uapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.ocx_1002_uapp.Fragments.HistoryFragment
import com.example.ocx_1002_uapp.Fragments.HomeFragment
import com.example.ocx_1002_uapp.Fragments.UserFragment
import com.example.ocx_1002_uapp.Services.WebSocketService

import com.example.ocx_1002_uapp.databinding.ActivityHomeAcitvityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Home_Acitvity : AppCompatActivity() {
    lateinit var binding: ActivityHomeAcitvityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAcitvityBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        changeFragment(HomeFragment())

    // Observe incoming messages
//        viewModel.messages.observe(this, Observer { message ->
//            Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
//        })
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.btm_nav_home -> changeFragment(HomeFragment())
                R.id.btm_nav_history -> changeFragment(HistoryFragment())
                R.id.btm_nav_User -> changeFragment(UserFragment())
                else -> {
                    true
                }
            }
        }
    }
    override fun onStart() {
        changeFragment(HomeFragment())
        super.onStart()

    }
    private fun changeFragment(fragment :Fragment): Boolean {
        val fragmentManager  = supportFragmentManager
        try {
            fragmentManager.beginTransaction()
                .replace(binding.fragmentContainerView.id,fragment)
                .commit()
            return true
        } catch (e: Exception) {
           return false
        }
    }
}