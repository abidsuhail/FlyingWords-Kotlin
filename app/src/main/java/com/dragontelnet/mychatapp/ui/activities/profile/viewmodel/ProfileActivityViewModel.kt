package com.dragontelnet.mychatapp.ui.activities.profile.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.ProfileActivityRepo
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyDaggerInjection
import javax.inject.Inject

class ProfileActivityViewModel : ViewModel() {

    @Inject
    lateinit var repo: ProfileActivityRepo

    fun sendFriendRequest(receiverUid: String): LiveData<Boolean> {
        return repo.sendFriendRequest(receiverUid)
    }

    fun getUser(userUid: String): LiveData<User> {
        return repo.getUser(userUid)
    }

    fun initProfileButtonsVisibility(receiverUid: String): LiveData<Int> {
        return repo.initProfileButtonsVisibility(receiverUid)
    }

    fun acceptRequest(receivedUid: String, context: Context?): LiveData<Boolean> {
        return repo.acceptRequest(receivedUid, context)
    }

    fun declineRequest(receivedUid: String): LiveData<Boolean> {
        return repo.declineRequest(receivedUid)
    }

    fun unFriendRequest(friendUid: String, context: Context): LiveData<Boolean> {
        return repo.unFriend(friendUid, context)
    }

    fun getFriendsCount(userUid: String): LiveData<Int> {
        return repo.getFriendsCount(userUid)
    }

    fun isPostsAvailable(profileUser: User): LiveData<Boolean> = repo.isPostsAvailable(profileUser)

    init {
        MyDaggerInjection.profileRepoComp?.inject(this)
    }
}