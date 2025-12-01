package com.example.ocx_1002_uapp.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ocx_1002_uapp.api.Entities.RegisterUserEntity
import com.example.ocx_1002_uapp.api.Entities.entityAdd
import com.example.ocx_1002_uapp.api.Services.UserServices
import com.example.project_b_security_gardapp.api.Entities.RequestsResultEntity
import com.example.project_b_security_gardapp.api.Entities.User
import com.example.project_b_security_gardapp.api.Entities.userLoginEntity
import com.example.project_b_security_gardapp.api.Responses.UserLoginResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

class UserRepository(private val userServices: UserServices) {
    private val loginData = MutableLiveData<UserLoginResponse>()
    private val userData = MutableLiveData<User>()

    val UserDataLiveData :LiveData<User>
        get() = userData
    val loginLiveData: LiveData<UserLoginResponse>
        get() = loginData

    suspend fun login(body: userLoginEntity): Response<UserLoginResponse> {
        // Make the API call
        val result = userServices.login(body)

        // Check if the call was successful and the body is not null
//        if(result.body() != null){
//            // Post the received data to the LiveData object
//            loginData.postValue(result.body())
//        }
        return result
    }
    suspend fun GetUserBytoken(token :String): Response<User> {
        val result = userServices.getDetailsByToken("Bearer $token")
//        if(result.body() != null){
//            // Post the received data to the LiveData object
//            userData.postValue(result.body())
//        }
        return result
    }
    suspend fun getAllGuestRequests(token: String): Response<List<RequestsResultEntity>> {
        return userServices.getAllGuestRequests(token)
    }

    suspend fun SignUpUser(data:RegisterUserEntity):Response<UserLoginResponse>{
        return userServices.SignUp(data)
    }

    suspend fun getRequestById(id:String,token: String): Response<RequestsResultEntity> {
        return userServices.getRequestById(token,id.toInt())

    }
    suspend fun updateRequestStatus(id:String,status:String,token: String):Response<RequestsResultEntity>{
        return userServices.UpdateStatus(token = token,id=id.toInt(),status = status)
    }
    suspend fun getImage(photo: String,token: String):Response<ResponseBody>{
        return userServices.getPhoto(fileName = photo, token = token)
    }

    suspend fun getAddBanner(token:String): Response<entityAdd> {
        return userServices.getAddBanner("Bearer ".plus(token))
    }
    suspend fun getImageForBanner(token: String,fileName:String):Response<ResponseBody>{
        return userServices.getImageForBanner(token = "Bearer ".plus(token),fileName = fileName)

    }


}