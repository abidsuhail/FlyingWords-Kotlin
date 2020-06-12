package com.dragontelnet.mychatapp.utils.firestore

import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import java.util.*

class DeviceTokenMap(var deviceToken: String) {

    fun toMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map[FirestoreKeys.Companion.DEVICE_TOKEN] = deviceToken
        return map
    }

}