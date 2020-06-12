package com.dragontelnet.mychatapp.datasource.remote.repository.modules.request

import android.content.Context
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

open class UnFriendRepo : SendFriendRequestRepo() {
    fun unFriend(friendUid: String, context: Context): SingleLiveEvent<Boolean> {
        val event = SingleLiveEvent<Boolean>()
        val docRefOfFriendUidInMyRef = MyFirestoreDbRefs.getUidFriendsCollection(getCurrentUser()?.uid)
                .document(friendUid)

        val docRefOfMyUidInFriendRef = MyFirestoreDbRefs.getUidFriendsCollection(friendUid)
                .document(getCurrentUser()!!.uid)

        MyFirestoreDbRefs.rootRef.runBatch {
            it.delete(docRefOfFriendUidInMyRef)
            it.delete(docRefOfMyUidInFriendRef)
        }.addOnCompleteListener {
            event.value = it.isSuccessful
        }

        return event
    }
}