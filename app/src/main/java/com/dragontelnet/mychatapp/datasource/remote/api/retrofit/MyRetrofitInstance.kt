package com.dragontelnet.mychatapp.datasource.remote.api.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyRetrofitInstance private constructor() {

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"
        private var instance: FcmMessagingService? = null
        fun getRetrofitInstance(): FcmMessagingService {
            return instance ?: run {
                Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().create(FcmMessagingService::class.java)
            }
        }
    }
}