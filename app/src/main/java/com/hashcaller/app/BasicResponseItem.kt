package com.hashcaller.app

data class BasicResponseItem<T>(
    val `data`: T? = null,
    val message: String,
    val statusCode: Int
)