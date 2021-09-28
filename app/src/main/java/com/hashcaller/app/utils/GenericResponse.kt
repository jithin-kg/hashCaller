package com.hashcaller.app.utils

import androidx.annotation.Keep

@Keep
class GenericResponse<T>(
     val message:String,
     val data:T,
    ) {
}