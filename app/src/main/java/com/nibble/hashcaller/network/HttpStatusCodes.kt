package com.nibble.hashcaller.network

 class HttpStatusCodes {
     companion object{
         // used while searching, if no item found -> then server returns 204 as status code
         const val NO_CONTENT = 204
         const val STATUS_OK = 200
         const val STATUS_CREATED = 201
         const val CREATED = 201
         const val STATUS_SEARHING_IN_PROGRESS = 900
         const val FORBIDDEN = 403
         const val SERVER_ERROR = 500
     }
}