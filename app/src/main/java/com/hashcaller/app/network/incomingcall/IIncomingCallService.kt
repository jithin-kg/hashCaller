package com.hashcaller.app.network.incomingcall

import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.repository.search.SearchDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface IIncomingCallService {

    companion object {
        const val BASE_URL: String = IuserService.BASE_URL;
    }

    @POST("community/suggestName")
    suspend fun suggestName(
        @Body data: SuggestNameModel,
        @Header("Authorization") token: String
    ): Response<SuggestNameModel.Response>

    @POST("suggestName/upvoteName")
    suspend fun upVote(
        @Body data: SuggestNameModel,
        @Header("Authorization") token: String
    ): Response<SuggestNameModel.Response>

    @POST("suggestName/downvoteName")
    suspend fun downVote(
        @Body data: SuggestNameModel,
        @Header("Authorization") token: String
    ): Response<SuggestNameModel.Response>

}