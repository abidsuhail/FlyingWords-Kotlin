package com.dragontelnet.mychatapp.ui.fragments.story.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.facebook.drawee.view.SimpleDraweeView

class SeenStoryVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var seenByUid: String? = null
    var profilePic: SimpleDraweeView = itemView.findViewById(R.id.seen_item_profile_pic)
    var fullNameTv: TextView = itemView.findViewById(R.id.seen_item_fname)
    var dateTimeTv: TextView = itemView.findViewById(R.id.seen_item_date_time)
}