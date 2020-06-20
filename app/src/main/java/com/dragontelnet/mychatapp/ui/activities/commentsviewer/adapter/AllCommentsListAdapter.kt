package com.dragontelnet.mychatapp.ui.activities.commentsviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Comment
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.adapter.viewholder.CommentVH
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.view.CommentsViewerActivity
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.viewmodel.CommentsViewerViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AllCommentsListAdapter(options: FirestoreRecyclerOptions<Comment?>, private val mViewModel: CommentsViewerViewModel?, private val activity: CommentsViewerActivity) : FirestoreRecyclerAdapter<Comment, CommentVH>(options) {
    override fun onBindViewHolder(holder: CommentVH, position: Int, comment: Comment) {
        mViewModel?.getUser(comment.commentByUid)?.observe(activity, Observer { user: User -> holder.bindCommentDetails(holder, user, comment) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_layout, parent, false)
        return CommentVH(view)
    }

}