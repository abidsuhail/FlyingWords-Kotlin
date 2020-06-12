package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class SeenIds(var seenByUid: String?, var seenByDate: String?, var seenByTime: String?) : Serializable {

    constructor() : this("", "", "")


}