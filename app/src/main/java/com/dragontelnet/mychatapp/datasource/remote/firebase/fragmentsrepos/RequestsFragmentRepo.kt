package com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.request.RequestsRepoOperationsHandler
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.allUsersCollection
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.myFriendRequestsListRef
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class RequestsFragmentRepo : RequestsRepoOperationsHandler() {
    private lateinit var reqListListener: ListenerRegistration
    private lateinit var liveFriendReqSeenListener: ListenerRegistration
    private val reqListenerList: MutableList<ListenerRegistration> = mutableListOf()
    private val listenerRegistrationList: MutableList<ListenerRegistration> = mutableListOf()
    private val reqLiveEvent = MutableLiveData<List<FriendRequest>>()


    fun getFriend(friendUid: String): SingleLiveEvent<User> {
        //here sender is I,that is my uid is senderUid
        val friendLiveEvent = SingleLiveEvent<User>()
        allUsersCollection.document(friendUid)
                .get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        user?.let { friendLiveEvent.value = it }
                    }
                }
        return friendLiveEvent
    }

    fun setSeenToAllReqListener() {
        //listening to requests to add seen update
        liveFriendReqSeenListener = myFriendRequestsListRef
                .addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    snapshot?.isEmpty?.let { isEmpty ->
                        if (!isEmpty) {
                            val friendRequestList = snapshot.toObjects(FriendRequest::class.java)
                            for ((sentByUid, receiverUid, type, status) in friendRequestList) {
                                if (type == FirestoreKeys.RECEIVED && sentByUid != getCurrentUser()?.uid && status == FirestoreKeys.DELIVERED) {
                                    val map = HashMap<String, Any>()
                                    map[FirestoreKeys.STATUS] = FirestoreKeys.SEEN
                                    myFriendRequestsListRef.document(sentByUid!!)
                                            .update(map)
                                }
                            }
                        }
                    }
                }
        listenerRegistrationList.add(liveFriendReqSeenListener)
    }

    fun removeDbListeners() {
        reqListenerList.forEach { it.remove() }
        listenerRegistrationList.forEach { it.remove() }
        reqListenerList.clear()
        listenerRegistrationList.clear()
    }

    private val TAG = "RequestsFragmentRepo"
    fun getRequestsListLive(): MutableLiveData<List<FriendRequest>> {
        val query: Query = myFriendRequestsListRef
        reqListListener = query.addSnapshotListener { qs, _ ->
            if (qs != null && !qs.isEmpty) {
                val reqList = qs.toObjects(FriendRequest::class.java)
                if (reqList.isNotEmpty()) {
                    reqList.forEach { friendRequest ->
                        allUsersCollection.document(friendRequest.sentByUid!!)
                                .get()
                                .addOnSuccessListener { ds ->
                                    val user = ds.toObject(User::class.java)
                                    if (user?.uid == friendRequest.sentByUid) {
                                        friendRequest.user = user
                                        reqList.remove(friendRequest)
                                        reqList.add(friendRequest)
                                        reqLiveEvent.value = reqList
                                    }
                                }
                    }
                } else {
                    reqLiveEvent.value = emptyList()
                }
            } else {
                reqLiveEvent.value = emptyList()
            }
        }
        reqListenerList.add(reqListListener)
        return reqLiveEvent
    }
}