package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class DateTime(var date: String?, var time: String?, var timeStamp: Long) : Serializable {

    constructor() : this("", "", 0)

}