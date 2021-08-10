package com.hashcaller.utils

class GenericResponse<T>(
    private val message:String,
    private val data:T) {
}