package com.dragontelnet.mychatapp.ui.fragments.requests.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.RequestsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyDaggerInjection.Companion.requestsRepoComp
import javax.inject.Inject

class RequestsViewModel : ViewModel() {
    @Inject
    lateinit var repo: RequestsFragmentRepo

    init {
        requestsRepoComp?.inject(this)
    }

    fun acceptRequest(receivedUid: String, context: Context): LiveData<Boolean> {
        return repo.acceptRequest(receivedUid, context)
    }

    fun declineRequest(receivedUid: String): LiveData<Boolean> {
        return repo.declineRequest(receivedUid)
    }


    fun getFriend(friendUid: String): LiveData<User> {
        return repo.getFriend(friendUid)
    }

    fun startAllReqSeenListener() {
        repo.setSeenToAllReqListener()
    }

    fun removeAllListeners() {
        repo.removeDbListeners()
    }

    fun getRequestsListLive(): LiveData<List<FriendRequest>> {
        return repo.getRequestsListLive()

    }

}