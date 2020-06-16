package com.dragontelnet.mychatapp.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.FeedsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.google.firebase.firestore.Query

class FeedsFragmentViewModel : ViewModel() {
    private val repo = FeedsFragmentRepo()
    fun sendLikeToPost(post: Post): LiveData<Int> {
        return repo.sendLikeToPost(post)
    }

    fun getUser(userUid: String): LiveData<User> = repo.getUser(userUid)
    fun deletePost(updatedPost: Post): LiveData<Boolean> = repo.deletePostPhoto(updatedPost)
    fun checkFeedsEmptiness(feedsQuery: Query): LiveData<Boolean> = repo.checkFeedsEmptiness(feedsQuery)
    fun removeFeedsCheckerListener() = repo.removeFeedsCheckerListener()

}