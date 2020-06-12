package com.dragontelnet.mychatapp.datasource.remote.repository.fragmentsrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.UserDetailsFetcher
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.TIMESTAMP
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getOlderChatsRefOfUid
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.*

class ChatFragmentRepo : UserDetailsFetcher() {
    private var friendLastChatListener: ListenerRegistration? = null

    fun checkForOlderChatCount(): SingleLiveEvent<Int> {
        val olderChatLiveEvent = SingleLiveEvent<Int>()
        val olderChatRef = getOlderChatsRefOfUid(getCurrentUser()!!.uid)
        olderChatRef.addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (snapshot != null) {
                olderChatLiveEvent.setValue(snapshot.size())
            } else {
                olderChatLiveEvent.setValue(0)
            }
        }
        return olderChatLiveEvent
    }

    fun getLastChatListLive(): MutableLiveData<List<Chat>> {
        val friendChatEvent = MutableLiveData<List<Chat>>()
        val friendsHashSet = hashSetOf<Chat>()
        friendLastChatListener = getOlderChatsRefOfUid(getCurrentUser()?.uid).orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener { t, _ ->
                    val dc = t?.documentChanges
                    if (dc != null && dc.isNotEmpty()) {
                        dc.forEach {
                            when (it.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    //friend added,now it will listen to updates from user collection
                                    val chat = it.document.toObject(Chat::class.java)
                                    MyFirestoreDbRefs.allUsersCollection.document(chat.receiverUid!!).get()
                                            .addOnSuccessListener { ds ->
                                                val user: User = ds.toObject(User::class.java)!!
                                                chat.receiverUidUser = user
                                                friendsHashSet.remove(chat)
                                                friendsHashSet.add(chat)
                                                friendChatEvent.value = friendsHashSet.toList()
                                            }
                                    MyFirestoreDbRefs.allUsersCollection.document(chat.receiverUid!!).get().addOnSuccessListener { ds ->
                                        val user: User = ds.toObject(User::class.java)!!
                                        chat.receiverUidUser = user
                                        friendsHashSet.remove(chat)
                                        friendsHashSet.add(chat)
                                        friendChatEvent.value = friendsHashSet.toList()
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    //friend removed,now remove it from list
                                    val chat = it.document.toObject(Chat::class.java)
                                    friendsHashSet.remove(chat)
                                    friendChatEvent.value = friendsHashSet.toList()

                                }
                            }
                        }
                    } else {
                        friendChatEvent.value = emptyList()
                    }
                }
        return friendChatEvent
    }


    fun removeDbListeners() {

        friendLastChatListener?.remove()
    }

    companion object {
        private const val TAG = "ChatFragmentRepo"
    }
}