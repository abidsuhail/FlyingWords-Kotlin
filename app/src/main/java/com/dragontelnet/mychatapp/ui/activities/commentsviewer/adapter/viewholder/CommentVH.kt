package com.dragontelnet.mychatapp.ui.activities.commentsviewer.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Comment
import com.dragontelnet.mychatapp.model.entity.User
import com.facebook.drawee.view.SimpleDraweeView

class CommentVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val commentOwnerDp: SimpleDraweeView = itemView.findViewById(R.id.comment_owner_dp)
    private val commentOwnerName: TextView = itemView.findViewById(R.id.comment_owner_name)
    private val commentContent: TextView = itemView.findViewById(R.id.comment_content)
    fun bindCommentDetails(holder: CommentVH, user: User, comment: Comment) {
        with(user)
        {
            holder.commentOwnerName.text = name
            holder.commentOwnerDp.setImageURI(profilePic)
        }
        holder.commentContent.text = comment.content

    }

}