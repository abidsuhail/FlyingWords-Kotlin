package com.dragontelnet.mychatapp.datasource.remote.firebase.modules.request

import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

//functionality is same as CancelFriendRequest
open class DeclineRequestRepo : UnFriendRepo() {
    fun declineRequest(receivedUid: String?): SingleLiveEvent<Boolean> {
        val declineEvent = SingleLiveEvent<Boolean>()
        val delFromUserUid = MyFirestoreDbRefs.getFriendRequestsBuilderRef(receivedUid, getCurrentUser()!!.uid)
        val delFromMyUid = MyFirestoreDbRefs.getFriendRequestsBuilderRef(getCurrentUser()!!.uid, receivedUid)

        MyFirestoreDbRefs.rootRef.runBatch {
            it.delete(delFromMyUid)
            it.delete(delFromUserUid)
        }.addOnCompleteListener {
            declineEvent.value = it.isSuccessful
        }
        return declineEvent
    }
}