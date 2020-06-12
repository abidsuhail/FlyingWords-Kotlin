package com.dragontelnet.mychatapp.model.entity

import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import java.io.Serializable

data class Story(var timeStamp: Long, var byUid: String?, var ownerName: String?, var ownerProfileUrl: String?, var storyItemList: MutableList<StoryItem>?) : Serializable {

    constructor() : this(CurrentDateAndTime.timeStamp, "", "", "", emptyList<StoryItem>().toMutableList())

    override fun equals(other: Any?): Boolean {
        if (other is Story) {
            return other.byUid == byUid
        }
        return false
    }

    override fun hashCode(): Int {
        return byUid.hashCode()
    }
}