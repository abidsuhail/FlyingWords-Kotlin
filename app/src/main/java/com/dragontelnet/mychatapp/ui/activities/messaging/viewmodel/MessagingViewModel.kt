package com.dragontelnet.mychatapp.ui.activities.messaging.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.MessagingActivityRepo
import com.dragontelnet.mychatapp.model.entity.User
import com.google.firebase.firestore.Query

class MessagingViewModel : ViewModel() {
    private val repo = MessagingActivityRepo()

    fun getUser(userUid: String): LiveData<User> = repo.getUser(userUid)
    fun sendMessage(chatMsg: String, uid: String, context: Context, storyReplyLink: String?): LiveData<Boolean> = repo.sendMessage(chatMsg, uid, context, storyReplyLink!!)

    fun notifyOnSentLastMsg(receiverUid: String, context: Context): LiveData<Int> = repo.notifyOnSentLastMsg(receiverUid, context)

    fun getLiveChatUserDetails(receiverUid: String, context: Context): LiveData<User> = repo.getLiveChatUserDetails(receiverUid, context)

    fun addSeenListener(receiverUid: String, context: Context) = repo.addSeenListener(receiverUid, context)

    fun removeSeenListener() = repo.removeSeenListener()

    fun setTypingStatus(receiverUser: User) = repo.setTypingStatus(receiverUser)

    fun stopTypingStatus(receiverUser: User) = repo.stopTypingStatus(receiverUser)

    fun sendNotification(user: User?, chatMsg: String?, context: Context?) = repo.sendNotification(user!!, chatMsg, context)

    fun checkMessagesEmptiness(query: Query): LiveData<Boolean> = repo.checkMessagesEmptiness(query)

    fun getLastChatDocRef(myUid: String, receiverUid: String): LiveData<String> = repo.getLastChatDocRef(myUid, receiverUid)

    fun isChatUserIsFriend(receiverUid: String): LiveData<Boolean> = repo.isChatUserIsFriend(receiverUid)
}