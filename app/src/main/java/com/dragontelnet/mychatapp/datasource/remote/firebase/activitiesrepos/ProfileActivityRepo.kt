package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.request.RequestsRepoOperationsHandler
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.RECEIVED
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.SENT
import com.dragontelnet.mychatapp.utils.MyConstants.ProfileFriendRequestCodes
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getFriendRequestsBuilderRef
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getUidFriendsCollection
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

class ProfileActivityRepo : RequestsRepoOperationsHandler() {
    fun initProfileButtonsVisibility(receiverUid: String): SingleLiveEvent<Int> {
        val initButtonEvent: SingleLiveEvent<Int> = SingleLiveEvent()
        //later listen for realtime
        getFriendRequestsBuilderRef(getCurrentUser()?.uid, receiverUid)
                .get().addOnSuccessListener { ds ->
                    if (ds.exists()) {
                        val request: FriendRequest? = ds.toObject(FriendRequest::class.java)
                        request?.let {
                            if (receiverUid != getCurrentUser()?.uid) {
                                when (it.type) {
                                    SENT -> initButtonEvent.value = ProfileFriendRequestCodes.SENT
                                    RECEIVED -> initButtonEvent.value = ProfileFriendRequestCodes.RECEIVED
                                    else -> initButtonEvent.value = ProfileFriendRequestCodes.UNKNOWN_REQUEST
                                }
                            } else {
                                initButtonEvent.value = ProfileFriendRequestCodes.SAME_USER
                            }
                        }
                    } else {
                        checkInMyFriendListCollection(receiverUid, initButtonEvent)
                    }

                }
        return initButtonEvent
    }

    private fun checkInMyFriendListCollection(receiverUid: String, initButtonEvent: SingleLiveEvent<Int>) {
        getUidFriendsCollection(getCurrentUser()?.uid)
                .document(receiverUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        //both are friends
                        initButtonEvent.value = ProfileFriendRequestCodes.FRIENDS
                    } else {
                        //both are not friends
                        if (getCurrentUser()?.uid != receiverUid) {
                            //not interaction with user,req not sent
                            //so show send req btn
                            initButtonEvent.value = ProfileFriendRequestCodes.NO_REQUEST_EXISTS
                        } else {
                            //same user a/c
                            //hide all buttons
                            initButtonEvent.value = ProfileFriendRequestCodes.SAME_USER
                        }
                    }
                }
    }

    fun getFriendsCount(userUid: String): SingleLiveEvent<Int> {
        val friendCountEvent = SingleLiveEvent<Int>()
        getUidFriendsCollection(userUid).get().addOnSuccessListener {
            friendCountEvent.value = it.size()
        }.addOnFailureListener {
            friendCountEvent.value = 0
        }
        return friendCountEvent
    }

    fun isPostsAvailable(profileUser: User): SingleLiveEvent<Boolean> {
        val postAvailableEvent = SingleLiveEvent<Boolean>()
        MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(profileUser.uid!!).get()
                .addOnSuccessListener {
                    postAvailableEvent.value = it.documents.isEmpty()
                }
        return postAvailableEvent
    }
}