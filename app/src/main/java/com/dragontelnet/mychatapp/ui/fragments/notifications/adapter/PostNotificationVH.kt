package com.dragontelnet.mychatapp.ui.fragments.notifications.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.facebook.drawee.view.SimpleDraweeView

class PostNotificationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notifPic: SimpleDraweeView = itemView.findViewById(R.id.notification_pic)
    val notifOwnerPic: SimpleDraweeView = itemView.findViewById(R.id.notification_owner_profile_pic)
    val notifCaption: TextView = itemView.findViewById(R.id.notification_caption)
    val notifDateTime: TextView = itemView.findViewById(R.id.notif_date_time_tv)
}