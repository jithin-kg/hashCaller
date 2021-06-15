package com.nibble.hashcaller.network.user

import com.nibble.hashcaller.view.ui.profile.RequestUserInfoDTO
import com.nibble.hashcaller.view.ui.profile.RequestUserinfoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface IuserService {
    companion object{

//        public const val BASE_URL: String = "http://192.168.43.34:8000/"
         const val BASE_URL: String = "https://real-caller-api-2-jzlji.ondigitalocean.app/"  //-> worked
//        public const val BASE_URL: String = "https://real-caller-api-2-jzlji.ondigitalocean.app/" worker with DO

//        public const val BASE_URL: String = "http://api.hashcaller.com:8000/"
//        public const val BASE_URL: String = "https://hashcalllerapi001.herokuapp.com/"
    }
    @Multipart
    @POST("user/signup")
    suspend fun signup(
        @Part("firstName") firstName:RequestBody,
        @Part("lastName") lastName:RequestBody,
        @Part("hashedNum") hashedNum:RequestBody,
        @Part("phoneNumber") phoneNumber:RequestBody,
        @Part("countryCode") countryCode:RequestBody,
        @Part("countryISO") countryISO:RequestBody,
        @Part image: MultipartBody.Part?,
        @Header ("Authorization") token:String
    ):Response<SingupResponse>

    @Multipart
    @POST("user/updateUserInfo")
    suspend fun updateUserInfo(
        @Part("firstName") firstName:RequestBody,
        @Part("lastName") lastName:RequestBody,
        @Part("hashedNum") hashedNum:RequestBody,
        @Part("phoneNumber") phoneNumber:RequestBody,
        @Part("countryCode") countryCode:RequestBody,
        @Part("countryISO") countryISO:RequestBody,
        @Part image: MultipartBody.Part?,
        @Header ("Authorization") token:String
    ): Response<SingupResponse>
    //retrieves cipher from hashcaller server
    @POST("user/getCipher")
    suspend fun getCipher(
        @Header("Authorization")
        token: String): Response<ResponseCipher>

    @POST("user/getUserInfoForUid")
    suspend fun getUserInfo(
        @Header("Authorization")
        token: String,
        @Body userInfo:GetUserInfoDTO
    ) : Response<SingupResponse>

    @POST("user/getUserInfoByMail")
    suspend fun requestUserInfoInServer(
        @Header("Authorization")
        token: String,
        @Body emailBody: RequestUserInfoDTO
    ): Response<RequestUserinfoResponse>


}