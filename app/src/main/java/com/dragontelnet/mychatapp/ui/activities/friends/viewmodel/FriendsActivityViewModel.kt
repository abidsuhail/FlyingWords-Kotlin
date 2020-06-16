package com.dragontelnet.mychatapp.ui.activities.friends.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.FriendsActivityRepo
import com.dragontelnet.mychatapp.model.entity.User

class FriendsActivityViewModel : ViewModel() {
    private val repo: FriendsActivityRepo = FriendsActivityRepo()
    fun getFriendListLive(): LiveData<List<User>> = repo.getFriendListLive()
    fun removeDbListeners() = repo.removeDbListeners()
}