package com.dragontelnet.mychatapp.utils

import android.widget.TextView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.facebook.drawee.view.SimpleDraweeView

object UserProfileDetailsSetter {

    fun setImage(sdv: SimpleDraweeView, photoLink: String, gender: String) {
        if (photoLink == "") {
            if (gender == "male") {
                sdv.setActualImageResource(R.drawable.user_male_placeholder)
            } else {
                sdv.setActualImageResource(R.drawable.user_female_placeholder)
            }
        } else {
            sdv.setImageURI(photoLink)
        }
    }

    fun setImage(sdv: SimpleDraweeView?, user: User?) {
        user?.let {
            if (it.profilePic == "") {
                //no profile pic
                if (it.gender == "male") {
                    sdv?.setActualImageResource(R.drawable.user_male_placeholder)
                } else {
                    sdv?.setActualImageResource(R.drawable.user_female_placeholder)
                }
            } else {

                //have profile pic
                sdv?.setImageURI(it.profilePic)
            }
        }
    }

    fun setAllUserDetails(user: User?, nameTv: TextView? = null, genderTv: TextView? = null, sdv: SimpleDraweeView? = null, userLastSeenDateTimeTv: TextView? = null, cityTv: TextView? = null, bioTv: TextView? = null, userNameTv: TextView? = null) {
        setImage(sdv, user)

        user?.let { userNn ->
            nameTv?.text = userNn.name
            genderTv?.text = userNn.gender
            userLastSeenDateTimeTv?.text = userNn.date + " " + userNn.time
            cityTv?.text = userNn.city
            bioTv?.text = userNn.bio
            userNameTv?.text = "@" + userNn.username
        }

    }
}