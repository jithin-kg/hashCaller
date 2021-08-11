package com.hashcaller

data class BasicResponseItem<T>(
    val `data`: T? = null,
    val message: String,
    val statusCode: Int
)