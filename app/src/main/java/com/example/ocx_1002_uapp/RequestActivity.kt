package com.example.ocx_1002_uapp

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ocx_1002_uapp.Glild.ImageLoader
import com.example.ocx_1002_uapp.ViewModels.ViewRequestViewModel
import com.example.ocx_1002_uapp.databinding.ActivityRequestBinding

class RequestActivity : AppCompatActivity() {
    lateinit var binding: ActivityRequestBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var requestViewModel: ViewRequestViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra("Id")
        sharedPreferences = getSharedPreferences(Keywords.MYPREFS.toString(), MODE_PRIVATE)
        val token =
            sharedPreferences.getString(Keywords.USERTOKEN.toString(), Keywords.NOTFOUND.toString())
        requestViewModel = ViewRequestViewModel(token.toString())

        if (id != null) {
            requestViewModel.getRequestById(id, token.toString())
            Log.d("Data", "onCreate: $token")
        }
//        Accept btn for accepting
        binding.btnAccept.setOnClickListener {
            requestViewModel.updateStatus(id!!,Keywords.ACCEPTED.toString(),token.toString())
        }
//        Reject btn for rejection
        binding.btnReject.setOnClickListener {
            requestViewModel.updateStatus(id!!,Keywords.REJECTED.toString(),token.toString())
        }

        requestViewModel.photoOne.observe(this){
            binding.ivPhoto1.setImageBitmap(it)
        }
        requestViewModel.photoTwo.observe(this){
            binding.ivPhoto2.setImageBitmap(it)
        }
//        Observe request
        requestViewModel.request.observe(this) { visitor ->
            binding.tvRequestNameValue.text = visitor.guestName
            binding.tvMobileNumberValue.text = visitor.phoneNumber
            binding.tvDescriptionValue.text = visitor.description
            binding.tvSocietyValue.text = visitor.societyName
            binding.chipStatus.text = visitor.status
            binding.tvOwnerNameValue.text = visitor.ownerName

            if(visitor.photo1 != null) {
                Log.d(TAG, "onCreate: ${visitor.photo1.toString()}")
                try {
//                    ImageLoader().loadImageWithAuth(this,visitor.photo1.toString(),token.toString(),binding.ivPhoto1)
                    val url = visitor.photo1.toString()
                    val fileName = url.substringAfterLast("/")
                    Log.d(TAG, "onCreate: file Name $fileName")
                    requestViewModel.getPhoto1(token.toString(),fileName)
                } catch (e: Exception) {
                    Log.d("Error To load image ", "onCreate: exception for not connection with url")
                }
            }else{
                Log.d(TAG, "onCreate: ${visitor.photo1.toString()}")
            }
            if (visitor.photo2 != null){
                Log.d(TAG, "onCreate: ${visitor.photo1.toString()}")
                try {
//                    ImageLoader().loadImageWithAuth(this,visitor.photo1.toString(),token.toString(),binding.ivPhoto1)
                    val url = visitor.photo2.toString()
                    val fileName = url.substringAfterLast("/")
                    Log.d(TAG, "onCreate: file Name $fileName")
                    requestViewModel.getPhoto2(token.toString(),fileName)
                } catch (e: Exception) {
                    Log.d("Error To load image ", "onCreate: exception for not connection with url")
                }
            }
            if (visitor.status.equals(Keywords.ACCEPTED.toString())) {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.chipStatus.chipBackgroundColor = getColorStateList(R.color.green)
            } else if (visitor.status.equals(Keywords.REJECTED.toString())) {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.chipStatus.chipBackgroundColor = getColorStateList(R.color.red)
            }
        }

        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.fragment_loading)
            .setCancelable(false)
            .create()
//        loadingDialog.create().apply {
//            window?.setBackgroundDrawableResource(android.R.color.transparent)
//        }

        requestViewModel.loading.observe(this) {

            if (it) {
                Log.d("Loading Indicator", "onStart: loading $it ")
                loadingDialog.show()
            }else{
                loadingDialog.dismiss()
            }
        }
    }
    override fun onStart() {
        super.onStart()

    }
}