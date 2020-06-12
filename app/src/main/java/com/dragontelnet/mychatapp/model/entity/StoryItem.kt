package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class StoryItem(var date: String?,
                     var time: String?,
                     var imageUrl: String?,
                     var timeStamp: Long = 0) : Serializable {

    constructor() : this("", "", "", 0)

    override fun equals(other: Any?): Boolean {
        if (other is Story) {
            return other.timeStamp == timeStamp
        }
        return false
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }
}