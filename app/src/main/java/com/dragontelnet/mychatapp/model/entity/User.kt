package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class User(var name: String?,
                var username: String?,
                var gender: String?,
                var phone: String?,
                var profilePic: String?,
                var deviceToken: String?,
                var uid: String?,
                var status: String?,
                var date: String?,
                var time: String?,
                var bio: String?,
                var city: String?) : Serializable {

    //for firebase
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "")

    companion object {
        const val USER_BOOK_KEY = "user_book"
    }

    override fun hashCode(): Int {
        return phone.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return other.uid == uid && other.phone == phone
        }
        return false
    }
}