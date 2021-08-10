package com.hashcaller.utils.callscreening

import android.telecom.CallScreeningService
import androidx.annotation.Keep
import java.io.Serializable
@Keep
data class CallResponse(
     val builder: CallScreeningService.CallResponse.Builder): Serializable{

}
