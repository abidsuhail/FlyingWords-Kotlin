package com.dragontelnet.mychatapp.ui.activities.friends.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.facebook.drawee.view.SimpleDraweeView
import de.hdodenhof.circleimageview.CircleImageView

class FriendVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var fname: TextView = itemView.findViewById(R.id.friend_fname)
    private var gender: TextView = itemView.findViewById(R.id.friend_gender)
    private var bio: TextView = itemView.findViewById(R.id.friend_bio)
    private var onlineIcon: CircleImageView = itemView.findViewById(R.id.online_icon_friend)
    private var offlineIcon: CircleImageView = itemView.findViewById(R.id.offline_icon_friend)
    private var profilePic: SimpleDraweeView = itemView.findViewById(R.id.friend_pic)

    fun bindFriendDetails(user: User, holder: FriendVH) {

        holder.fname.text = user.name
        holder.gender.text = "@ ${user.username}"
        holder.bio.text = user.bio
        if (user.gender == "male" && user.profilePic == "") {
            holder.profilePic.setImageResource(R.drawable.user_male_placeholder)
        } else if (user.gender == "female" && user.profilePic == "") {
            holder.profilePic.setImageResource(R.drawable.user_female_placeholder)
        } else {
            holder.profilePic.setImageURI(user.profilePic)
        }
        if (user.status == FirestoreKeys.ONLINE || user.status == FirestoreKeys.TYPING_FIELD) {
            //is online
            holder.onlineIcon.visibility = View.VISIBLE
            holder.offlineIcon.visibility = View.GONE
        } else {
            holder.onlineIcon.visibility = View.GONE
            holder.offlineIcon.visibility = View.VISIBLE

        }
    }

}