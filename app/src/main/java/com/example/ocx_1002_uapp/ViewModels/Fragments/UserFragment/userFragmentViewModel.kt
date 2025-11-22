package com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.project_b_security_gardapp.api.Entities.User
import kotlinx.coroutines.launch

// A simple data class to represent a Use

/**
 * A parameterized ViewModel that takes a userId as a constructor parameter.
 * It's responsible for fetching and holding the data for a specific user.
 */
class UserViewModel(private val repo:UserRepository, private val token: String) : ViewModel() {

    // Private MutableLiveData that can be modified within the ViewModel
    private val _user = MutableLiveData<User?>()

    // Public LiveData that is exposed to the UI (Fragment) for observation. It's immutable.
    val user: LiveData<User?> = _user

    // LiveData to track the loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    init {
//         The init block is a great place to start loading initial data.
        fetchUserData()
    }

     fun fetchUserData() {
        // Use viewModelScope to launch a coroutine that is automatically
        // cancelled when the ViewModel is cleared.
        viewModelScope.launch {
            _isLoading.value = true
            // Simulate a network or database call
//            delay(1500) // Simulate a 1.5-second delay

            // In a real app, you would fetch this from your repository:
            // val result = userRepository.getUserById(userId)
            val fetchedUser = repo.GetUserBytoken(token)

            _user.value = fetchedUser.body()
            _isLoading.value = false
        }
    }

    /**
     * A public function that the UI can call to refresh the user data.
     */


    override fun onCleared() {
        super.onCleared()
        // This is the place to clean up any resources if needed.
        // Coroutines in viewModelScope are automatically cancelled.
    }
}