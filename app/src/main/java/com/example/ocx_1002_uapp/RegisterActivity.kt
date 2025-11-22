package com.example.ocx_1002_uapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Entities.RegisterUserEntity
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Provider.Service

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.buttonSignUp.setOnClickListener {
                validateInput()
        }

        binding.loginPageText.setOnClickListener {
            finish()
        }
    }
    private fun validateInput() {
        binding.buttonSignUp.isClickable = false
        val name = binding.EtName.text.toString().trim()
        val password = binding.EtPassword.text.toString().trim()
        val confirmPassword = binding.EtConfirmPassword.text.toString().trim()
        val mobile = binding.EtMobile.text.toString().trim()
        val flatNo = binding.EtFlatNumber.text.toString().trim()
        val society = binding.EtSocietyName.text.toString().trim()

        when {
            name.isEmpty() -> {
                binding.EtName.error = "Please enter your name"
                binding.EtName.requestFocus()
            }
            password.isEmpty() -> {
                binding.EtPassword.error = "Please enter a password"
                binding.EtPassword.requestFocus()
            }
            password.length < 6 -> {
                binding.EtPassword.error = "Password must be at least 6 characters"
                binding.EtPassword.requestFocus()
            }
            confirmPassword.isEmpty() -> {
                binding.EtConfirmPassword.error = "Please confirm your password"
                binding.EtConfirmPassword.requestFocus()
            }
            password != confirmPassword -> {
                binding.EtConfirmPassword.error = "Passwords do not match"
                binding.EtConfirmPassword.requestFocus()
            }
            mobile.isEmpty() -> {
                binding.EtMobile.error = "Please enter mobile number"
                binding.EtMobile.requestFocus()
            }
            mobile.length != 10 -> {
                binding.EtMobile.error = "Mobile number must be 10 digits"
                binding.EtMobile.requestFocus()
            }
            flatNo.isEmpty() -> {
                binding.EtFlatNumber.error = "Please enter your flat number"
                binding.EtFlatNumber.requestFocus()
            }
            society.isEmpty() -> {
                binding.EtSocietyName.error = "Please enter society name"
                binding.EtSocietyName.requestFocus()
            }
            else -> {
                val user  = RegisterUserEntity(flatNo,name,password,"+91".plus(mobile),society)
                val service= API.getInstance.create(UserServices::class.java)
                val repo = UserRepository(service)

                CoroutineScope(Dispatchers.Main).launch {
                    val result = repo.SignUpUser(user)
                    Log.d(TAG, "Api hit Result : ${result.code()}")

                    if(result.isSuccessful && result.code() == 200){
                        Toast.makeText(applicationContext, "✅ Registration Successful!", Toast.LENGTH_SHORT).show()
                        finish()
                    }else if(result.code() == 500 ){
                        Toast.makeText(applicationContext, "❌ Internal Server Error!", Toast.LENGTH_SHORT).show()
                        binding.buttonSignUp.isClickable = true
                    }
                    else{
                        Toast.makeText(applicationContext, "❌ Registration Failed!", Toast.LENGTH_SHORT).show()
                        binding.buttonSignUp.isClickable = true
                    }
                }

                // You can now proceed with API call or next screen
            }
        }
    }
}