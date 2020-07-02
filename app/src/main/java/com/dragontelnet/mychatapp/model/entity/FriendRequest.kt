package com.dragontelnet.mychatapp.model.entity

data class FriendRequest(var sentByUid: String?, var receiverUid: String?, var type: String?, var status: String?, var user: User?, var timeStamp: Long?) {
    constructor() : this("", "", "", "", null, 0)

    override fun equals(other: Any?): Boolean {
        if (other is FriendRequest) {
            return other.sentByUid == sentByUid
        }
        return false
    }

    override fun hashCode(): Int {
        return sentByUid.hashCode()
    }
}