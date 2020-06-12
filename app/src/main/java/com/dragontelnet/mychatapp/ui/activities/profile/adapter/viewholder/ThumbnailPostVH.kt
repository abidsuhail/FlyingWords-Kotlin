package com.dragontelnet.mychatapp.ui.activities.profile.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.facebook.drawee.view.SimpleDraweeView

class ThumbnailPostVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val myPostPic: SimpleDraweeView = itemView.findViewById(R.id.my_posted_pic)
    private val myPostText: TextView = itemView.findViewById(R.id.my_post_text)
    val view = itemView
    fun bindPostStatus(holder: ThumbnailPostVH, post: Post) {
        holder.myPostPic.visibility = View.GONE
        holder.myPostText.visibility = View.VISIBLE
        holder.myPostText.text = post.caption
    }

    fun bindPostPhoto(holder: ThumbnailPostVH, post: Post) {
        holder.myPostText.visibility = View.GONE
        holder.myPostPic.visibility = View.VISIBLE
        holder.myPostPic.setImageURI(post.postPhotoUrl)
    }

}