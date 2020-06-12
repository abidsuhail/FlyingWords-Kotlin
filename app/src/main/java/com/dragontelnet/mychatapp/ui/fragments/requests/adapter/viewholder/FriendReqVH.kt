package com.dragontelnet.mychatapp.ui.fragments.requests.adapter.viewholder

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants
import com.facebook.drawee.view.SimpleDraweeView

class FriendReqVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var fname: TextView = itemView.findViewById(R.id.friend_req_fname)
    var gender: TextView = itemView.findViewById(R.id.friend_req_gender)
    var friendConnect: TextView = itemView.findViewById(R.id.friend_req_connect_txt)
    var confirmBtn: Button = itemView.findViewById(R.id.friend_req_btn_confirm)
    var declineBtn: Button = itemView.findViewById(R.id.friend_req_btn_decline)
    var cancelSentRequest: Button = itemView.findViewById(R.id.friend_req_btn_cancel_sent)
    var profilePic: SimpleDraweeView = itemView.findViewById(R.id.friend_req_img)

    fun bindUserDetails(user: User, holder: FriendReqVH) {
        holder.fname.text = user.name
        holder.gender.text = user.gender
        if (user.profilePic == "") {
            if (user.gender == "male") {
                holder.profilePic.setImageResource(R.drawable.user_male_placeholder)
            } else {
                holder.profilePic.setImageResource(R.drawable.user_female_placeholder)
            }
        } else {
            //show real image
            holder.profilePic.setImageURI(user.profilePic)
        }
    }

    fun bindRequestButtons(friendRequest: FriendRequest, holder: FriendReqVH) {
        /*holder.confirmBtn.isEnabled=true
        holder.declineBtn.isEnabled=true
        holder.cancelSentRequest.isEnabled=true*/

        if (friendRequest.type == MyConstants.FirestoreKeys.RECEIVED) {
            holder.confirmBtn.visibility = View.VISIBLE
            holder.declineBtn.visibility = View.VISIBLE
            holder.cancelSentRequest.visibility = View.GONE
        } else {
            holder.confirmBtn.visibility = View.GONE
            holder.declineBtn.visibility = View.GONE
            holder.cancelSentRequest.visibility = View.VISIBLE
        }
    }
}