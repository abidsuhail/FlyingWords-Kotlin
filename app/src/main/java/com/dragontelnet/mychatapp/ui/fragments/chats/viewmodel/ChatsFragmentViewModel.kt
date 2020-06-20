package com.dragontelnet.mychatapp.ui.fragments.chats.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.ChatFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User

class ChatsFragmentViewModel : ViewModel() {
    private val sortedChatsListEvent = MediatorLiveData<List<Chat>>()
    private val repo: ChatFragmentRepo = ChatFragmentRepo()

    fun getUser(receiverUid: String): LiveData<User?> = repo.getUser(receiverUid)
    fun removeAllListeners() {
        sortedChatsListEvent.removeSource(repo.getLastChatListLive())
        repo.removeDbListeners()
    }

    fun getLastChatListLive(): LiveData<List<Chat>> {
        sortedChatsListEvent.addSource(repo.getLastChatListLive()) { list ->
            val sortedChatList = list.sortedWith(compareByDescending {
                if (it.timeStamp != null) {
                    it.timeStamp?.time
                } else {
                    it.deviceTimeStamp
                }

            })
            sortedChatsListEvent.value = sortedChatList
        }
        return sortedChatsListEvent
    }

}