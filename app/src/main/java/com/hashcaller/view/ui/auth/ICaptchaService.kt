package com.hashcaller.view.ui.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface ICaptchaService {
    companion object{
        public const val BASE_URL: String = "http://192.168.43.34:8000/"
//        public const val BASE_URL: String = "https://hashcalllerapi001.herokuapp.com/"
    }

    @POST("/")
    suspend fun sendToken(
        @Body()
        response: Data): APIResponse

}
