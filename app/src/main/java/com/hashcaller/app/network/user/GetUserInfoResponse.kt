package com.hashcaller.app.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetUserInfoResponse( @SerializedName("email")
                                var email: String = "",

                                @SerializedName("firstName")
                                var firstName: String = "",

                                @SerializedName("lastName")
                                var lastName: String,

                                @SerializedName("gender")
                                var gender: String = "",
                            ) {
}