package com.hashcaller.network

import androidx.annotation.Keep

/**
 * Base class for all response, that is passed to ui components
 * @property result response from server (data received from server)
 * @param isEverytingWentWell 1 -> everything went well
 *                  0 -> Something wrong happend
 */
@Keep
class NetworkResponseBase<E>(val result: E?, val isEverytingWentWell:Int ) {

companion object{
    const val EVERYTHING_WENT_WELL = 1
    const val SOMETHING_WRONG_HAPPEND = 0
}
}