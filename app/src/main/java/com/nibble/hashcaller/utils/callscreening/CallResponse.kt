package com.nibble.hashcaller.utils.callscreening

import android.telecom.CallScreeningService
import java.io.Serializable

data class CallResponse(
     val builder: CallScreeningService.CallResponse.Builder): Serializable{

}
