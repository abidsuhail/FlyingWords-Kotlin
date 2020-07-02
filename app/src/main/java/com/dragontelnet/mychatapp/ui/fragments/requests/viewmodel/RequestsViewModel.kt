package com.dragontelnet.mychatapp.ui.fragments.requests.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.RequestsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.utils.MyDaggerInjection.Companion.requestsRepoComp
import javax.inject.Inject

class RequestsViewModel : ViewModel() {
    @Inject
    lateinit var repo: RequestsFragmentRepo

    private val sortedReqList = MediatorLiveData<List<FriendRequest>>()

    init {
        requestsRepoComp?.inject(this)
    }

    fun acceptRequest(receivedUid: String, context: Context): LiveData<Boolean> {
        return repo.acceptRequest(receivedUid, context)
    }

    fun declineRequest(receivedUid: String): LiveData<Boolean> {
        return repo.declineRequest(receivedUid)
    }


    fun startAllReqSeenListener() {
        repo.setSeenToAllReqListener()
    }

    fun removeAllListeners() {
        sortedReqList.removeSource(repo.getRequestsListLive())
        repo.removeDbListeners()
    }

    fun getRequestsListLive(): LiveData<List<FriendRequest>> {
        sortedReqList.removeSource(repo.getRequestsListLive())
        sortedReqList.addSource(repo.getRequestsListLive()) { sortedList ->
            sortedReqList.value = sortedList.sortedWith(compareByDescending {
                if (it.timeStamp != null) {
                    it.timeStamp
                } else {
                    it.type
                }
            })
        }
        return sortedReqList
    }

}