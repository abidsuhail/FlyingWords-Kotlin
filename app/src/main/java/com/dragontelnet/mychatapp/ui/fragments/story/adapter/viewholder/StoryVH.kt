package com.dragontelnet.mychatapp.ui.fragments.story.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.facebook.drawee.view.SimpleDraweeView

class StoryVH constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var fullNameTv: TextView = itemView.findViewById(R.id.full_name_tv)
    var dateTv: TextView = itemView.findViewById(R.id.date_tv)
    var timeTv: TextView = itemView.findViewById(R.id.time_tv)
    var profilePic: SimpleDraweeView = itemView.findViewById(R.id.profile_pic)
    var view: View = itemView
    var constraintLayout: ConstraintLayout = itemView.findViewById(R.id.story_constraint_layout)
    fun hideAllViews() {
        fullNameTv.visibility = View.GONE
        profilePic.visibility = View.GONE
        dateTv.visibility = View.GONE
        timeTv.visibility = View.GONE
        constraintLayout.visibility = View.GONE
        view.visibility = View.GONE
    }

    fun showAllViews() {
        fullNameTv.visibility = View.VISIBLE
        profilePic.visibility = View.VISIBLE
        dateTv.visibility = View.VISIBLE
        timeTv.visibility = View.VISIBLE
        view.visibility = View.VISIBLE
        constraintLayout.visibility = View.VISIBLE
    }

    fun bindStoryToViews(holder: StoryVH, lastStory: StoryItem) {
        holder.profilePic.setImageURI(lastStory.imageUrl)
        holder.dateTv.text = lastStory.date
        holder.timeTv.text = lastStory.time
    }

}