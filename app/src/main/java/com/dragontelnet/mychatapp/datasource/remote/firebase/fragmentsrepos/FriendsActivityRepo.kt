package com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.model.entity.Friend
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getUidFriendsCollection
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

class FriendsActivityRepo {
    private var friendListListener: ListenerRegistration? = null
    private val friendListenersList: MutableList<ListenerRegistration> = mutableListOf()

    fun removeDbListeners() {
        //removing listeners
        friendListenersList.forEach {
            it.remove()
        }
        friendListenersList.clear()

        friendListListener?.remove()
    }

    fun getFriendListLive(): MutableLiveData<List<User>> {
        val friendListEvent = MutableLiveData<List<User>>()
        val friendsHashSet = hashSetOf<User>()
        friendListListener = getUidFriendsCollection(getCurrentUser()?.uid)
                .addSnapshotListener { t, _ ->
                    val dc = t?.documentChanges
                    if (dc != null && dc.isNotEmpty()) {
                        dc.forEach {
                            when (it.type) {
                                DocumentChange.Type.ADDED -> {
                                    //friend added,now it will listen to updates from user collection
                                    val friend = it.document.toObject(Friend::class.java)
                                    friendListListener = MyFirestoreDbRefs.allUsersCollection.document(friend.uid!!).addSnapshotListener { dc, _ ->
                                        val user: User = dc?.toObject(User::class.java)!!
                                        friendsHashSet.remove(user)
                                        friendsHashSet.add(user)
                                        friendListEvent.value = friendsHashSet.toList()
                                    }
                                    friendListenersList.add(friendListListener!!)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    //friend removed,now remove it from list
                                    val friend = it.document.toObject(Friend::class.java)
                                    friendListListener = MyFirestoreDbRefs.allUsersCollection.document(friend.uid!!).addSnapshotListener { dc, _ ->
                                        val user: User = dc?.toObject(User::class.java)!!
                                        friendsHashSet.remove(user)
                                        friendListEvent.value = friendsHashSet.toList()
                                        friendListenersList.add(friendListListener!!)
                                    }
                                }
                            }
                        }
                    } else {
                        friendListEvent.value = emptyList()
                    }
                }
        friendListenersList.add(friendListListener!!)
        return friendListEvent
    }


    companion object {
        private val TAG: String = "FriendsFragmentRepo"
    }
}