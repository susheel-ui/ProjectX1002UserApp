package com.example.ocx_1002_uapp.ViewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.project_b_security_gardapp.api.Entities.RequestsResultEntity

import kotlinx.coroutines.launch

class RequestsViewModel : ViewModel() {

    private val repo: UserRepository

    // 🔹 LiveData to hold the guest request list
    private val _requestsLiveData = MutableLiveData<List<RequestsResultEntity>>()
    val requestsLiveData: LiveData<List<RequestsResultEntity>> = _requestsLiveData

    private val _recentThreeRequstsLiveData = MutableLiveData<List<RequestsResultEntity>>()
    val recentThreeRequstsLiveData: LiveData<List<RequestsResultEntity>> = _recentThreeRequstsLiveData


    // 🔹 Loading indicator LiveData
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // 🔹 Error message LiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val apiInstance = API.getInstance
        val userService = apiInstance.create(UserServices::class.java)
        repo = UserRepository(userService)
    }

    /**
     * Fetch guest requests list from API and post it to LiveData
     */
    fun getGuestRequests(token: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val response = repo.getAllGuestRequests("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    // ✅ "data" contains the actual list of guest requests
                    _requestsLiveData.postValue(result)
                    if(result.size >= 2)
                        _recentThreeRequstsLiveData.postValue(result.subList(0,2))
                    else if(result.size == 1){
                        _recentThreeRequstsLiveData.postValue(result.subList(0,1))
                    }else{
                        _recentThreeRequstsLiveData.postValue(emptyList())
                    }
                    Log.d("RequestsViewModel", "Fetched ${result.size} guest requests")
                } else {
                    val msg = "Failed: ${response.code()} - ${response.message()}"
                    _errorMessage.postValue(msg)
                    Log.e("RequestsViewModel", msg)
                }

            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Unexpected error")
                Log.e("RequestsViewModel", "Exception: ${e.message}", e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

}
