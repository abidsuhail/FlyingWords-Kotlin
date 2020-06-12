package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class Comment(var date: String?,
                   var time: String?,
                   var commentByUid: String?,
                   var content: String?,
                   var commentId: String?,
                   var timeStamp: Long) : Serializable {

    constructor() : this("", "", "", "", "", 0)

}