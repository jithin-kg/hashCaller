package com.nibble.hashcaller.network.user

import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface IuserService {
    companion object{
        public const val BASE_URL: String = "http://192.168.43.84:3000/"
    }

    @POST("user/signup")
    suspend fun signup(
        @Body userInfo :UserInfoDTO,
        @Header ("Authorization") token:String
    ):Response<NetWorkResponse>
}