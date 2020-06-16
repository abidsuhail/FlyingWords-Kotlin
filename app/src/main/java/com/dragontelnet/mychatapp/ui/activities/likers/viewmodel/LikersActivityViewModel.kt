package com.dragontelnet.mychatapp.ui.activities.likers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.LikersActivityRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User

class LikersActivityViewModel() : ViewModel() {
    private val repo = LikersActivityRepo()

    fun getLikersUsersList(post: Post): LiveData<List<User>> = repo.getLikersUserList(post)
}