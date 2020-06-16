package com.dragontelnet.mychatapp.ui.fragments.notifications.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.NotificationFragmentRepo
import com.dragontelnet.mychatapp.model.entity.PostNotification

class NotificationsFragmentViewModel : ViewModel() {
    val repo = NotificationFragmentRepo()

    fun startNotifSeenListener() = repo.startNotifSeenListener()

    fun getLiveNotifsList(): LiveData<List<PostNotification>> = repo.getLiveNotifsList()
    fun removeAllListeners() = repo.removeAllListeners()
}