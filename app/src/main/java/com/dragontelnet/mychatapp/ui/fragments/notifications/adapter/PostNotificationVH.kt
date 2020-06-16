package com.dragontelnet.mychatapp.ui.fragments.notifications.adapter

import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.facebook.drawee.view.SimpleDraweeView

class PostNotificationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindNotifDetails(holder: PostNotificationVH, notification: PostNotification) {
        holder.notifDateTime.text = "${notification.dateTime?.date} ${notification.dateTime?.time}"

        if (notification.imgUrl?.isNotBlank()!!) {
            holder.notifPic.visibility = View.VISIBLE
            holder.notifPic.setImageURI(notification.imgUrl)
        } else {
            holder.notifPic.visibility = View.GONE
        }

        if (PostNotification.COMMENT_TYPE == notification.type) {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> commented on your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> commented on your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }
        } else {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> liked your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> liked your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }
        }

    }

    fun bindNotifUserDetails(holder: PostNotificationVH, notification: PostNotification) {
        if (notification.notifOwnerProfilePic != "") {
            holder.notifOwnerPic.setImageURI(notification.notifOwnerProfilePic)
        } else {
            if (notification.notifOwnerGender == "male") {
                holder.notifOwnerPic.setActualImageResource(R.drawable.user_male_placeholder)
            } else {
                holder.notifOwnerPic.setActualImageResource(R.drawable.user_female_placeholder)

            }
        }
    }

    val notifPic: SimpleDraweeView = itemView.findViewById(R.id.notification_pic)
    val notifOwnerPic: SimpleDraweeView = itemView.findViewById(R.id.notification_owner_profile_pic)
    val notifCaption: TextView = itemView.findViewById(R.id.notification_caption)
    val notifDateTime: TextView = itemView.findViewById(R.id.notif_date_time_tv)
}