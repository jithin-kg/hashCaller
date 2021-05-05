package com.nibble.hashcaller.network

import com.google.gson.GsonBuilder
import com.nibble.hashcaller.network.contact.IContactsService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


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

            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()

    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(IContactsService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(okHttp)
        .build()

    fun <T> createaService(serviceClass: Class<T>?): T {

//        okHttp.readTimeout(60, TimeUnit.SECONDS)
//        okHttp.connectTimeout(60, TimeUnit.SECONDS)

        return retrofit.create(serviceClass)

    }
}
