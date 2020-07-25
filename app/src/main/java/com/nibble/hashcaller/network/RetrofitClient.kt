package com.nibble.hashcaller.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Jithin KG on 25,July,2020
 */
//class RetrofitClient {
//}

object RetrofitClient {
    //to accept malformed JSON
//    private val gson: Gson = GsonBuilder()
////                        .setLenient()
//                        .create()
    //create logger
    private val logger =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    //create client
    private val okHttp =
        OkHttpClient.Builder().addInterceptor(logger)

    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(IContactsService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(okHttp.build())
        .build()

    fun <T> createaService(serviceClass: Class<T>?): T {
        return retrofit.create(serviceClass)

    }
}
