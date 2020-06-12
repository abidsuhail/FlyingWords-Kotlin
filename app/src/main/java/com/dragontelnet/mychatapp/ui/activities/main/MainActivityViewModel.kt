package com.dragontelnet.mychatapp.ui.activities.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.MainActivityRepo
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.DELIVERED
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.RECEIVED
import com.dragontelnet.mychatapp.utils.auth.CurrentUser

class MainActivityViewModel : ViewModel() {
    private val repo: MainActivityRepo = MainActivityRepo()
    private val listenForFriendRequestsLive = MediatorLiveData<Boolean>()
    private val listenForUnreadLastChatLive = MediatorLiveData<Boolean>()
    private val listenForUnreadNotificationsLive = MediatorLiveData<Boolean>()

    fun setUserState(isOnline: String): LiveData<Boolean> = repo.setUserState(isOnline)

    fun getAndSetDeviceTokenToDb(context: Context) = repo.getAndSetDeviceTokenToDb(context)

    fun currentStableUser(): LiveData<User> = repo.currentStableUser

    fun startListenForUnreadRequests(): LiveData<Boolean> {
        //looping through friend req lists to check if Delivered or Seen
        listenForFriendRequestsLive.addSource(repo.liveFriendRequestsList()) { friendRequestList: List<FriendRequest>? ->
            friendRequestList?.let { list ->
                listenForFriendRequestsLive.value = list.any { request ->
                    request.status == DELIVERED && request.type == RECEIVED && request.sentByUid != CurrentUser.getCurrentUser()?.uid
                }
            }
        }
        return listenForFriendRequestsLive
    }

    fun removeAllListeners() = repo.removeAllListeners()

    fun listenLiveForUnreadChats(): LiveData<Boolean> {
        listenForUnreadLastChatLive.addSource(repo.liveAllLastChatsList()) { chatsList: List<Chat?>? ->
            chatsList?.let { list ->
                listenForUnreadLastChatLive.value = list.any { lastChat ->
                    lastChat != null && lastChat.byUid != CurrentUser.getCurrentUser()?.uid && lastChat.status == DELIVERED
                }
            }
        }
        return listenForUnreadLastChatLive

    }

    fun listenForUnreadNotifications(): LiveData<Boolean> {
        listenForUnreadNotificationsLive.addSource(repo.liveNotificationsListener()) { notifList ->
            listenForUnreadNotificationsLive.value = notifList.any { notif ->
                //not seen,its true...then show notif badge
                notif.status == DELIVERED
            }
        }
        return listenForUnreadNotificationsLive
    }


}