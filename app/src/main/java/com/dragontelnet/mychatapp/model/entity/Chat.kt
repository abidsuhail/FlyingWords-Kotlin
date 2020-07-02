package com.dragontelnet.mychatapp.model.entity

import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.DELIVERED
import com.dragontelnet.mychatapp.utils.datetime.CurrentDateAndTime
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class Chat(var receiverUidUser: User?,
                var byUid: String?,
                var receiverUid: String?,
                var content: String?,
                var deviceDate: String?,
                var deviceTime: String?,
                var status: String?,
                var isTyping: String?,
                var msgKey: String?,
                var deviceTimeStamp: Long?,
                var storyPhotoLink: String?,
                @ServerTimestamp
                var timeStamp: Date?) : Serializable {

    constructor() : this(null,
            "",
            "",
            "",
            CurrentDateAndTime.currentDate,
            CurrentDateAndTime.currentTime,
            DELIVERED,
            "nottyping",
            "",
            CurrentDateAndTime.timeStamp,
            "",
            null)

    override fun hashCode(): Int {
        return receiverUid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Chat) {
            return other.receiverUid == receiverUid
        }
        return false
    }
}