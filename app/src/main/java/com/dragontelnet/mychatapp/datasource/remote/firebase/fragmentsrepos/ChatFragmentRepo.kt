package com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.UserDetailsFetcher
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getOlderChatsRefOfUid
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

class ChatFragmentRepo : UserDetailsFetcher() {
    private var friendLastChatListener: ListenerRegistration? = null
    private val friendChatEvent = MutableLiveData<List<Chat>>()
    private val friendsHashSet = hashSetOf<Chat>()
    fun getLastChatListLive(): MutableLiveData<List<Chat>> {

        getCurrentUser()?.uid?.let { uid ->
            friendLastChatListener = getOlderChatsRefOfUid(uid)
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
        }

        return friendChatEvent
    }

    fun removeDbListeners() = friendLastChatListener?.remove()

}