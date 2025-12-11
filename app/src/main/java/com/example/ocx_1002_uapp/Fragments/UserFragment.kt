package com.example.ocx_1002_uapp.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ocx_1002_uapp.EditProfileDataActivity
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.LoginActivity
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModel
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModelFactory
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.FragmentUserBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var  fragmentbinding: FragmentUserBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentbinding = FragmentUserBinding.inflate(layoutInflater)
        val api  = API.getInstance
        val userServices = api.create(UserServices::class.java)
        val repo = UserRepository(userServices)
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(Keywords.MYPREFS.toString(), MODE_PRIVATE)
        val token = sharedPreferences.getString(Keywords.USERTOKEN.toString(), null)
        if(token != null)
            userViewModel = ViewModelProvider(this, UserViewModelFactory(repo,token)).get(
                UserViewModel::class.java)
        fragmentbinding.btnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences(Keywords.MYPREFS.toString(), Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // This block will be executed whenever the user data changes.
            if (user != null) {
                fragmentbinding.userName.text = user.fullName
//                fragmentbinding.flateNumber.text = user.societyName
                fragmentbinding.mobileNumber.text = user.phoneNumber
                fragmentbinding.flateNumber.text = user.flatNumber.uppercase()
            } else {
                // Handle the case where the user is null (e.g., not found)
                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show or hide the loading indicator based on the loading state

//            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        fragmentbinding.editProfileIcon.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileDataActivity::class.java)
            startActivity(intent)
        }
        return fragmentbinding.root
        // A simple data class to represent a User

    }
    private fun observeViewModel() {
        // Observe the user LiveData


        // Observe the loading state LiveData

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}