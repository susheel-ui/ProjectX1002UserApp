package com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ocx_1002_uapp.api.repo.UserRepository

/**
 * A factory class for creating instances of UserViewModel with a constructor parameter.
 * This is the standard way to provide parameters to a ViewModel.
 */
class UserViewModelFactory(private val repo: UserRepository, private val token: String) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `Class`.
     *
     * @param modelClass a `Class` whose instance is requested
     * @return a newly created ViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is the same as our UserViewModel class
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            // If it is, create an instance of UserViewModel, passing the userId.
            // The unchecked cast is safe because we've just checked the class.
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repo,token) as T
        }
        // If it's not our ViewModel, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}