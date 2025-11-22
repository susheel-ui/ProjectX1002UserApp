package com.example.ocx_1002_uapp.Fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ocx_1002_uapp.Adapters.VisitorListViewAdapter
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.R
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModel
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModelFactory
import com.example.ocx_1002_uapp.ViewModels.RequestsViewModel
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.FragmentHomeBinding
import com.example.project_b_security_gardapp.api.Entities.User
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: VisitorListViewAdapter
    private lateinit var requestsViewModel: RequestsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var token:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        requestsViewModel = ViewModelProvider(this)[RequestsViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)

        val api = API.getInstance
        val userServices = api.create(UserServices::class.java)
        val repo = UserRepository(userServices)
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(Keywords.MYPREFS.toString(), MODE_PRIVATE)
        token = sharedPreferences.getString(Keywords.USERTOKEN.toString(), null).toString()
        if (token != null) {
            userViewModel = ViewModelProvider(
                this,
                UserViewModelFactory(repo, token)
            ).get(UserViewModel::class.java)


        }

        try {
            adapter = VisitorListViewAdapter(requireContext(), emptyList())
            binding.RecentGuest.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.RecentGuest.adapter = adapter
        } catch (e: Exception) {
            Log.d(TAG, "onCreate: ${e.message}")
        }

        userViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.societyNameTV.text = it.societyName
                binding.flateNumber.text = it.flatNumber.uppercase()
            }
        }
        // ✅ Observe visitor list
        requestsViewModel.recentThreeRequstsLiveData.observe(viewLifecycleOwner) { visitors ->
            if (visitors.isNotEmpty()) {
                adapter.updateData(visitors)
            } else {
                Toast.makeText(requireContext(), "No visitors found", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.loadingSkeleten.startShimmer()
//        requestsViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) {
//                binding.RecentGuest.visibility = View.VISIBLE
//                binding.loadingSkeleten.shimmerLayout.visibility = View.GONE
//            } else {
//                binding.RecentGuest.visibility = View.GONE
//                binding.loadingSkeleten.shimmerLayout.visibility = View.VISIBLE
//            }
//        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requestsViewModel.getGuestRequests(token)
        // TODO:: Here Recycler View will update

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}