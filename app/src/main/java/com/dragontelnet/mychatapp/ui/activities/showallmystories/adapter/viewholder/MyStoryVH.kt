package com.dragontelnet.mychatapp.ui.activities.showallmystories.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.facebook.drawee.view.SimpleDraweeView

class MyStoryVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var myStoryPhoto: SimpleDraweeView = itemView.findViewById(R.id.my_story_image)
    var viewsCountTv: TextView = itemView.findViewById(R.id.my_story_view_count)
    var dateAndTimeTv: TextView = itemView.findViewById(R.id.my_story_date_time_tv)
    var deleteStoryIv: ImageView = itemView.findViewById(R.id.my_story_delete_story_iv)
    var view: View = itemView
}