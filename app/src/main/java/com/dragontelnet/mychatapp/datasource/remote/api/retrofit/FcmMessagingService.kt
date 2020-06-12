package com.dragontelnet.mychatapp.datasource.remote.api.retrofit

import com.dragontelnet.mychatapp.model.fcm.MyResponse
import com.dragontelnet.mychatapp.model.fcm.NotificationPOJO
import com.dragontelnet.mychatapp.utils.RemoteKey
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmMessagingService {
    @Headers("authorization:key=${RemoteKey.fcmKey}", "content-type:application/json")
    @POST("fcm/send")
    fun sendNotification(@Body notification: NotificationPOJO?): Call<MyResponse?>?
}