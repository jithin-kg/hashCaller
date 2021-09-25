package com.hashcaller.app.network.search.model

import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import retrofit2.Response
import java.util.*

class DTOMapper {

    companion object {
        fun convertServerResToContactView(res: CallersInfoFromServer) =
             CntctitemForView(
                firstName = res.firstName,
                lastName = res.lastName,
                 nameInPhoneBook = res.nameInPhoneBook,
                 isVerifiedUser = res.isVerifiedUser,
                 thumbnailImgServer = res.thumbnailImg,
                 avatarGoogle = res.avatarGoogle,
                 spammerType = res.spammerType,
                 spammCount = res.spamReportCount,
                 hUid = res.hUid,
                 carrier = res.carrier,
                location = res.city,
                country = res.country,
                statusCode = HttpStatusCodes.STATUS_OK,
                informationReceivedDate = Date()
            )

        fun serverResultToConctView(response: Response<SerachRes>): CntctitemForView {
            val result = response.body()?.cntcts

            return  CntctitemForView(
                firstName = result?.firstName?:"",
                lastName = result?.lastName?:"",
                nameInPhoneBook = result?.nameInPhoneBook?:"",
                isVerifiedUser = result?.isVerifiedUser?:false,
                thumbnailImgServer = result?.thumbnailImg?:"",
                avatarGoogle = result?.avatarGoogle?:"",
                spammCount = result?.spammCount?:0L,
                hUid =result?.hUid?:"",
                carrier = result?.carrier?:"",
                location = result?.location?:"",
                lineType = result?.lineType?:"",
                country = result?.country?:"",
                statusCode = response.code(),
                isInfoFoundInServer = result?.isInfoFoundInDb?: INFO_NOT_FOUND_IN_SERVER,
                informationReceivedDate = Date(),
                clientHashedNum = result?.clientHashedNum?:""
            )
        }

        fun cntctitemForViewTOCallersInfoFromServer(resFromServer: CntctitemForView?, formatedNum:String): CallersInfoFromServer {
            return CallersInfoFromServer(
                contactAddress = formatedNum,
                hashedNum = resFromServer?.clientHashedNum?:"",
                firstName = resFromServer?.firstName?:"",
                lastName = resFromServer?.lastName?:"",
                nameInPhoneBook = resFromServer?.nameInPhoneBook?:"",
                informationReceivedDate =  Date(),
                spamReportCount = resFromServer?.spammCount?:0L,
                city = resFromServer?.location?:"",
                country = resFromServer?.country?:"",
                carrier = resFromServer?.carrier?:"",
                isBlockedByUser = false,
                isUserInfoFoundInServer = resFromServer?.isInfoFoundInServer?:INFO_NOT_FOUND_IN_SERVER,
                spammerType = resFromServer?.spammerType?:0,
                thumbnailImg = resFromServer?.thumbnailImgServer?:"",
                hUid = resFromServer?.hUid?:"",
                bio = resFromServer?.bio?:"",
                email = resFromServer?.email?:"",
                avatarGoogle = resFromServer?.avatarGoogle?:"",
                isVerifiedUser = resFromServer?.isVerifiedUser?:false

            )
        }


    }
}