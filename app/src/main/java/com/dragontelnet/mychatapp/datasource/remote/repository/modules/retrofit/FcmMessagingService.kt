package com.dragontelnet.mychatapp.datasource.remote.repository.modules.retrofit

import android.content.Context
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.getCurrentOfflineUserFromBook
import com.dragontelnet.mychatapp.datasource.remote.api.retrofit.MyRetrofitInstance
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.model.fcm.Data
import com.dragontelnet.mychatapp.model.fcm.MyResponse
import com.dragontelnet.mychatapp.model.fcm.NotificationPOJO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class FcmMessagingService {
    fun sendNotification(user: User, chatMsg: String?, context: Context?) {

        //sending post request NotificationPOJO
        val data = Data()
        val notificationPOJO = NotificationPOJO()

        with(data)
        {
            content = chatMsg
            getCurrentOfflineUserFromBook?.let {
                title = it.name
            } ?: run { title = "UNKNOWN USER!!!" }
        }
        notificationPOJO.data = data
        notificationPOJO.to = user.deviceToken //getting latest device token of receiver user

        MyRetrofitInstance.getRetrofitInstance()
                .sendNotification(notificationPOJO)?.enqueue(object : Callback<MyResponse?> {
                    override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {}

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {}
                })
    }

}