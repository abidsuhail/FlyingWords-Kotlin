package com.dragontelnet.mychatapp.ui.fragments.search.adapter.viewholder

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.facebook.drawee.view.SimpleDraweeView
import de.hdodenhof.circleimageview.CircleImageView

class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var fname: TextView = itemView.findViewById(R.id.friend_fname)
    var gender: TextView = itemView.findViewById(R.id.friend_gender)
    var username: TextView = itemView.findViewById(R.id.friend_username)
    var onlineIcon: CircleImageView = itemView.findViewById(R.id.online_icon_friend)
    var offlineIcon: CircleImageView = itemView.findViewById(R.id.offline_icon_friend)
    var profilePic: SimpleDraweeView = itemView.findViewById(R.id.friend_pic)
    var sendReqBtn: Button? = null

    //public CircleImageView profilePic;
    fun bindUserDetails(holder: UserVH, user: User) {
        holder.fname.text = user.name
        holder.gender.text = user.gender
        holder.username.text = "@" + user.username
        if (user.gender == "male" && user.profilePic == "") {
            holder.profilePic.setImageResource(R.drawable.user_male_placeholder)
        } else if (user.gender == "female" && user.profilePic == "") {
            holder.profilePic.setImageResource(R.drawable.user_female_placeholder)
        } else {
            holder.profilePic.setImageURI(user.profilePic)
        }
    }


}