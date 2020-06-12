package com.dragontelnet.mychatapp.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object CurrentDateAndTime {
    val currentDate: String
        get() {
            val calendar = Calendar.getInstance()
            val currentDate = SimpleDateFormat("MMM dd,yyyy")
            return currentDate.format(calendar.time)
        }

    /*    fun getServerTimeStamp(timeStamp:FieldValue) : Long?
        {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val date = format.parse(timeStamp.toString())
            return date.time
        }*/
    val currentTime: String
        get() {
            val calendar = Calendar.getInstance()
            val currentDate = SimpleDateFormat("hh:mm a")
            return currentDate.format(calendar.time)
        }

    val timeStamp: Long
        get() {
            val timestamp = Timestamp(System.currentTimeMillis())
            return timestamp.time
        }
}