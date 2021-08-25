package com.hashcaller.app.view.ui.auth

import com.google.gson.GsonBuilder
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

object RetrofitClientTest {
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
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl("http://192.168.43.34:3000")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(okHttp)
        .build()

    fun <T> createaService(serviceClass: Class<T>?): T {

//        okHttp.readTimeout(60, TimeUnit.SECONDS)
//        okHttp.connectTimeout(60, TimeUnit.SECONDS)

        return retrofit.create(serviceClass)

    }
}
