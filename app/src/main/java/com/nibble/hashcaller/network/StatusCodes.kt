package com.nibble.hashcaller.network

 class StatusCodes {
     companion object{
         // used while searching, if no item found -> then server returns 204 as status code
         const val NO_CONTENT = 204
         const val STATUS_OK = 200
         const val STATUS_SEARHING_IN_PROGRESS = 900
     }
}