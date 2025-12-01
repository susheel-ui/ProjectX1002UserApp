package com.example.ocx_1002_uapp.Fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ocx_1002_uapp.Adapters.VisitorListViewAdapter
import com.example.ocx_1002_uapp.Keywords
import com.example.ocx_1002_uapp.R
import com.example.ocx_1002_uapp.Services_Contact_Activity
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModel
import com.example.ocx_1002_uapp.ViewModels.Fragments.UserFragment.UserViewModelFactory
import com.example.ocx_1002_uapp.ViewModels.RequestsViewModel
import com.example.ocx_1002_uapp.api.API
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.ocx_1002_uapp.api.repo.UserRepository
import com.example.ocx_1002_uapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: VisitorListViewAdapter
    private lateinit var requestsViewModel: RequestsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var token:String

    val imagespath = ArrayList<String>()
    val imagesFile = ArrayList<Bitmap>()
    var imagesLoadedCount = 0   // 🔥 added counter

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

        // 🔥 Fetch banner image paths
        CoroutineScope(Dispatchers.IO).launch {
            val result = repo.getAddBanner(token)
            if(result.isSuccessful && result.code() == 200){
                val data = result.body()
                if(data?.enable == true){
                    if(data != null){
                        val images = data.images
                        for (img in images){
                            imagespath.add(img.name)
                        }
                    }
                }else{
                    activity?.runOnUiThread{
                        binding.viewFlipper.visibility = View.GONE
                    }
                }
            }
        }.invokeOnCompletion {
            activity?.runOnUiThread {
                for (picx in imagespath){
                    getImagesFromDb(repo, token, picx)
                }
            }
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

        requestsViewModel.recentThreeRequstsLiveData.observe(viewLifecycleOwner) { visitors ->
            if (visitors.isNotEmpty()) {
                adapter.updateData(visitors)
            } else {
                Toast.makeText(requireContext(), "No visitors found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnComplain.setOnClickListener{
            activity?.startActivity(Intent(requireContext(),Services_Contact_Activity::class.java))
        }
        binding.btnServices.setOnClickListener{
            activity?.startActivity(Intent(requireContext(),Services_Contact_Activity::class.java))
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requestsViewModel.getGuestRequests(token)
    }

    // 🔥 FIXED IMAGE LOADING FUNCTION
    private fun getImagesFromDb(repo:UserRepository, token:String, path:String){

        CoroutineScope(Dispatchers.IO).launch {
            val result = repo.getImageForBanner(token = token, fileName = path)

            if(result.isSuccessful && result.code() == 200){
                val bytes = result.body()?.bytes()
                if (bytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imagesFile.add(bitmap)
                }
            }

            // 🔥 Count each loaded image
            imagesLoadedCount++

            // 🔥 When all images loaded → Setup Flipper
            if (imagesLoadedCount == imagespath.size) {
                activity?.runOnUiThread {
                    setupFlipper()
                }
            }
        }
    }

    // 🔥 ADD IMAGES TO FLIPPER AFTER ALL LOADED
    private fun setupFlipper() {
        binding.viewFlipper.removeAllViews()

        for (img in imagesFile) {
            val imageView = ImageView(requireContext())
            imageView.setImageBitmap(img)
            binding.viewFlipper.addView(imageView)
        }

        binding.viewFlipper.setInAnimation(requireContext(), R.anim.slide_in_right)
        binding.viewFlipper.setOutAnimation(requireContext(), R.anim.slide_out_left)
        binding.viewFlipper.startFlipping()
    }

    companion object {
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
