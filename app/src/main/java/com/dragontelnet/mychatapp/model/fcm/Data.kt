package com.dragontelnet.mychatapp.model.fcm

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(@SerializedName("title")
                @Expose
                var title: String? = null,

                @SerializedName("content")
                @Expose
                var content: String? = null,

                @SerializedName("profileImg")
                @Expose
                var profileImg: String? = null)