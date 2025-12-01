package com.example.ocx_1002_uapp.api.Services

import com.example.ocx_1002_uapp.api.Entities.RegisterUserEntity
import com.example.ocx_1002_uapp.api.Entities.entityAdd
import com.example.project_b_security_gardapp.api.Entities.RequestsResultEntity
import com.example.project_b_security_gardapp.api.Entities.User
import com.example.project_b_security_gardapp.api.Entities.userLoginEntity
import com.example.project_b_security_gardapp.api.Responses.UserLoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserServices {
        @POST("/api/auth/login")
        suspend fun login(@Body loginEntity: userLoginEntity):Response<UserLoginResponse>

        @POST("/api/auth/register/user")
        suspend fun SignUp(@Body signUpEntity: RegisterUserEntity):Response<UserLoginResponse>

        @GET("/api/user/profile")
        suspend fun getDetailsByToken(@Header("Authorization") token: String):Response<User>

        @GET("/api/user/visitor-requests")
        suspend fun getAllGuestRequests(
                @Header("Authorization") token: String
        ): Response<List<RequestsResultEntity>>

        @GET("/api/user/visitor-requests/{id}")
        suspend fun getRequestById(
                @Header("Authorization") token: String,
                @Path("id") id:Int
        ):Response<RequestsResultEntity>

        @POST("/api/user/respond/{id}")
        suspend fun UpdateStatus(
                @Header("Authorization") token: String,
                @Path("id") id:Int,
                @Query("status") status:String
        ):Response<RequestsResultEntity>
        @GET("/api/files/view")
        suspend fun getPhoto(
                @Header("Authorization") token: String,
                @Query("name") fileName:String
        ): Response<ResponseBody>

        @GET("/api/user/feature-status")
        suspend fun getAddBanner(
                @Header("Authorization") token: String
        ):Response<entityAdd>

        @GET("/api/user/feature-status/{imageName}")
        suspend fun getImageForBanner(
                @Header("Authorization") token: String,
                @Path("imageName") fileName:String
        ):Response<ResponseBody>

}