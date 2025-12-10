package com.example.ocx_1002_uapp.ViewModels

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.project_b_security_gardapp.api.Entities.RequestsResultEntity
import kotlinx.coroutines.launch

class ViewRequestViewModel(val requestId: String) : ViewModel() {
    private val repo: UserRepository

    // 🔹 LiveData to hold the guest request list
    private val _request = MutableLiveData<RequestsResultEntity>()
    val request: LiveData<RequestsResultEntity> = _request

    private val _photoOne = MutableLiveData<Bitmap>()
    val photoOne: LiveData<Bitmap> = _photoOne

    private val _photoTwo = MutableLiveData<Bitmap>()
    val photoTwo: LiveData<Bitmap> = _photoTwo

    // 🔹 LiveData for loading state
    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading


    init {
        val apiInstance = API.getInstance
        val userService = apiInstance.create(UserServices::class.java)
        repo = UserRepository(userService)
    }




    fun getRequestById(id: String, token: String) {
        viewModelScope.launch {
            try {
                val result = repo.getRequestById(id, "Bearer $token")
                if (result.isSuccessful && result.code() == 200) {
                    Log.d("Success", "getRequestById: ${result.body()}")
                    _request.postValue(result.body())
                    _loading.postValue(false)
                } else {
                    Log.d("Error", "getRequestById: ${result.code()}")
                }
            } catch (e: Exception) {
                Log.d("Exception", "getRequestById: ${e.toString()} ")
            }
        }
    }

    fun updateStatus(id: String, status: String, token: String) {
        if (status.equals(Keywords.ACCEPTED.toString())) {
            viewModelScope.launch {
                _loading.postValue(true)
                val result = repo.updateRequestStatus(
                    id = id,
                    status = Keywords.ACCEPTED.toString(),
                    token = "Bearer ".plus(token)
                )
                if (result.isSuccessful && result.code() == 200) {
                    _loading.postValue(false)
                    _request.postValue(result.body())
                } else if (result.code() == 500) {
                    _loading.postValue(false)
                    Log.d(TAG, "updateStatus: ${result.code()}")
                }
            }

        } else if (status.equals(Keywords.REJECTED.toString())) {
            viewModelScope.launch {
                _loading.postValue(true)
                val result = repo.updateRequestStatus(
                    id = id,
                    status = Keywords.REJECTED.toString(),
                    token = "Bearer ".plus(token)
                )
                if (result.isSuccessful && result.code() == 200) {
                    _loading.postValue(false)
                    _request.postValue(result.body())
                } else if (result.code() == 500) {
                    _loading.postValue(false)
                    Log.d(TAG, "updateStatus: ${result.code()}")
                }
            }
        } else {
            Log.d(TAG, "updateStatus: Something problem")
        }
    }

    fun getPhoto1(token: String, photo: String) {
        viewModelScope.launch {
            val response = repo.getImage(photo = photo, token = "Bearer ".plus(token))
            if (response.isSuccessful && response.code() == 200) {
                val bytes = response.body()?.bytes()
                if (bytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    _photoOne.postValue(bitmap)
                    // show in ImageView
                }
                Log.d("Success", "getPhoto1: ${response.code()} ")
            } else {
                Log.d("Error", "getPhoto1: ${response.code()}")
            }
        }
    }

    fun getPhoto2(token: String, photo: String) {
        viewModelScope.launch {
            val response = repo.getImage(photo = photo, token = "Bearer ".plus(token))
            if (response.isSuccessful && response.code() == 200) {
                val bytes = response.body()?.bytes()
                if (bytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    _photoTwo.postValue(bitmap)
                    // show in ImageView
                }
                Log.d("Success", "getPhoto2: ${response.code()} ")
            } else {
                Log.d("Error", "getPhoto2: ${response.code()}")
            }
        }
    }

}