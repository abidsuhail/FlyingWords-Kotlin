package com.dragontelnet.mychatapp.utils

import com.dragontelnet.mychatapp.model.entity.DateTime

object DateTimeFactory {
    val currentDateTimeObj: DateTime
        get() = DateTime(CurrentDateAndTime.currentDate, CurrentDateAndTime.currentTime, CurrentDateAndTime.timeStamp)
}