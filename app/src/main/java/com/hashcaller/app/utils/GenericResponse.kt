package com.hashcaller.app.utils

class GenericResponse<T>(
    private val message:String,
    private val data:T) {
}