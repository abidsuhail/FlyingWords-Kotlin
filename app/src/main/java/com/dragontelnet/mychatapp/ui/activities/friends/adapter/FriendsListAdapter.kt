package com.dragontelnet.mychatapp.ui.activities.friends.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.friends.adapter.viewholder.FriendVH
import com.dragontelnet.mychatapp.ui.activities.friends.view.FriendsActivity
import com.dragontelnet.mychatapp.ui.activities.messaging.view.MessagingActivity
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.utils.auth.CurrentUser

class FriendsListAdapter(private val friendsProgress: ProgressBar,
                         private val activity: FriendsActivity,
                         private var mList: List<User>,
                         private val userUid: String?) : RecyclerView.Adapter<FriendVH>() {

    fun updateFriendList(list: List<User>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: FriendVH, position: Int) {
        friendsProgress.visibility = View.GONE
        val friend: User = mList[position]
        holder.bindFriendDetails(friend, holder)
        settingOnClickListeners(friend, holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_layout, parent, false)
        return FriendVH(view)
    }

    override fun getItemCount(): Int = mList.size

    private fun settingOnClickListeners(user: User, holder: FriendVH) {
        if (userUid == CurrentUser.getCurrentUser()?.uid) {
            holder.itemView.setOnClickListener { startMessagingActivity(user) }
        } else {
            holder.itemView.setOnClickListener { startProfileActivity(user) }
        }
    }

    private fun startProfileActivity(user: User) {

        val i = Intent(activity, ProfileActivity::class.java)
        i.putExtra("user", user)
        activity.startActivity(i)
    }


    private fun startMessagingActivity(user: User) {
        val i = Intent(activity, MessagingActivity::class.java)
        i.putExtra("user", user)
        activity.startActivity(i)
    }

}