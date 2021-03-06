package com.dragontelnet.mychatapp.ui.fragments.requests.adapter.viewholder

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants
import com.dragontelnet.mychatapp.utils.UserProfileDetailsSetter
import com.facebook.drawee.view.SimpleDraweeView

class FriendReqVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var fname: TextView = itemView.findViewById(R.id.friend_req_fname)
    var gender: TextView = itemView.findViewById(R.id.friend_req_gender)
    var friendConnect: TextView = itemView.findViewById(R.id.friend_req_connect_txt)
    var confirmBtn: Button = itemView.findViewById(R.id.friend_req_btn_confirm)
    var declineBtn: Button = itemView.findViewById(R.id.friend_req_btn_decline)
    var cancelSentRequest: Button = itemView.findViewById(R.id.friend_req_btn_cancel_sent)
    var profilePic: SimpleDraweeView = itemView.findViewById(R.id.friend_req_img)

    fun bindUserDetails(user: User?, holder: FriendReqVH) {
        UserProfileDetailsSetter.setAllUserDetails(user = user, nameTv = holder.fname, genderTv = holder.gender, sdv = holder.profilePic)

    }

    fun bindRequestButtons(friendRequest: FriendRequest, holder: FriendReqVH) {
        /*holder.confirmBtn.isEnabled=true
        holder.declineBtn.isEnabled=true
        holder.cancelSentRequest.isEnabled=true*/

        if (friendRequest.type == MyConstants.FirestoreKeys.RECEIVED) {
            holder.confirmBtn.visibility = View.VISIBLE
            holder.declineBtn.visibility = View.VISIBLE
            holder.cancelSentRequest.visibility = View.GONE
            holder.friendConnect.text = "wants to connect with you"
            //holder.friendConnect.visibility=View.VISIBLE
        } else {
            holder.confirmBtn.visibility = View.GONE
            holder.declineBtn.visibility = View.GONE
            holder.cancelSentRequest.visibility = View.VISIBLE
            holder.friendConnect.text = ""
            //holder.friendConnect.visibility=View.GONE

        }
    }
}