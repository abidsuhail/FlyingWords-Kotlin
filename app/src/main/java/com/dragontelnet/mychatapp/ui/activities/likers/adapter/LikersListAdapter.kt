package com.dragontelnet.mychatapp.ui.activities.likers.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.likers.view.LikersActivity
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.ui.fragments.search.adapter.viewholder.UserVH

class LikersListAdapter(private val activity: LikersActivity,
                        private var mList: List<User>) : RecyclerView.Adapter<UserVH>() {
    fun updateList(list: List<User>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)
        return UserVH(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val likerUser: User = mList[position]
        holder.bindUserDetails(holder, likerUser)

        holder.itemView.setOnClickListener {
            val i = Intent(activity, ProfileActivity::class.java)
            i.putExtra("user", likerUser)
            activity.startActivity(i)

        }
    }
}