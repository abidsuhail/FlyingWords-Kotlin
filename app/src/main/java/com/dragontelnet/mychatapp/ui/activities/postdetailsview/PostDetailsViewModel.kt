package com.dragontelnet.mychatapp.ui.activities.postdetailsview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.PostDetailsActivityRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User

class PostDetailsViewModel : ViewModel() {
    private val repo: PostDetailsActivityRepo = PostDetailsActivityRepo()

    fun sendLikeToPost(post: Post): LiveData<Int> {
        return repo.sendLikeToPost(post)
    }

    fun getUser(uid: String): LiveData<User> {
        return repo.getUser(uid)
    }

    fun getPost(post: Post): LiveData<Post> {
        return repo.getPost(post)
    }

    fun deletePost(post: Post): LiveData<Boolean> = repo.deletePostPhoto(post)

}