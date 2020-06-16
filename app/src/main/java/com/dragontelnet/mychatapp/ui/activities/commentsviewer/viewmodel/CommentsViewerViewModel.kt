package com.dragontelnet.mychatapp.ui.activities.commentsviewer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.CommentsViewerRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.google.firebase.firestore.Query

class CommentsViewerViewModel : ViewModel() {
    private val repo: CommentsViewerRepo = CommentsViewerRepo()
    fun getUser(uid: String?): LiveData<User> {
        return repo.getUser(uid!!)
    }

    fun sendComment(post: Post?, content: String?): LiveData<Post?> {
        return repo.sendComment(post!!, content!!)
    }

    fun getCommentsEmptiness(query: Query): LiveData<Boolean> = repo.getCommentsEmptiness(query)

}