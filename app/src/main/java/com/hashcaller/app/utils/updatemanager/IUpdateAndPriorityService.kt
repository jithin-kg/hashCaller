package com.hashcaller.app.utils.updatemanager

import androidx.annotation.Keep
import com.hashcaller.app.utils.GenericResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Keep
interface IUpdateAndPriorityService {
    companion object {
        public const val BASE_URL: String = "https://iexcrfljeazsamekapi.hashcaller.com/"
    }
    @POST("system/getPriority")
    suspend fun getPriorityByUpdateVersionCode(
        @Header("Authorization")
        token:String,
        @Body body: GetPriorityDTO
    ) : Response<GenericResponse<GetPriorityDTO.Response?>>

}