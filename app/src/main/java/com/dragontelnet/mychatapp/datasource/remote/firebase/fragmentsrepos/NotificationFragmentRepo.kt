package com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.DELIVERED
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.SEEN
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys.Companion.STATUS
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getNotificationCollectionRef
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationFragmentRepo {
    private val notifsList = mutableListOf<ListenerRegistration>()
    private var notifsListenerRegistration: ListenerRegistration? = null
    fun startNotifSeenListener() {
        val query = getNotificationCollectionRef(getCurrentUser()?.uid)
        val seenMap = hashMapOf<String, Any>(STATUS to SEEN)

        query.get().addOnSuccessListener { qs ->
            val notifObjList = qs.toObjects(PostNotification::class.java)
            notifObjList.forEach { notif ->
                if (notif.status == DELIVERED) query.document(notif?.notifId!!).update(seenMap)
            }
        }
    }

    fun getLiveNotifsList(): MutableLiveData<List<PostNotification>> {
        val notifLiveEvent = MutableLiveData<List<PostNotification>>()
        val notifHashSet = hashSetOf<PostNotification>()
        val query = getNotificationCollectionRef(getCurrentUser()?.uid)
                .orderBy(PostNotification.ORDER_BY_TIMESTAMP, Query.Direction.DESCENDING)
        notifsListenerRegistration = query.addSnapshotListener { qs, _ ->
            if (qs != null && !qs.isEmpty) {
                val dc = qs.documentChanges
                dc.forEach {
                    if (it.type == DocumentChange.Type.ADDED) {
                        val notifObj = it.document.toObject(PostNotification::class.java)
                        MyFirestoreDbRefs.allUsersCollection.document(notifObj.byUid!!)
                                .get()
                                .addOnSuccessListener { ds1 ->
                                    val notifUser = ds1.toObject(User::class.java)
                                    notifObj.notifOwnerName = notifUser?.name
                                    notifObj.notifOwnerProfilePic = notifUser?.profilePic
                                    notifObj.notifOwnerGender = notifUser?.gender
                                    notifObj.notifOwnerUser = notifUser
                                    MyFirestoreDbRefs.allUsersCollection.document(notifObj.postByUid!!)
                                            .get()
                                            .addOnSuccessListener { ds2 ->
                                                val postUser = ds2.toObject(User::class.java)
                                                notifObj.postOwnerName = postUser?.name
                                                notifObj.postOwnerProfilePic = postUser?.profilePic
                                                MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(notifObj.postByUid!!)
                                                        .document(notifObj.postId!!)
                                                        .get()
                                                        .addOnSuccessListener { postDs ->
                                                            val post = postDs.toObject(Post::class.java)
                                                            notifObj.post = post
                                                            notifHashSet.remove(notifObj)
                                                            notifHashSet.add(notifObj)
                                                            notifLiveEvent.value = notifHashSet.toList()
                                                        }
                                            }


                                }
                    }
                }
            } else {
                notifLiveEvent.value = emptyList()
            }
        }
        notifsList.add(notifsListenerRegistration!!)
        return notifLiveEvent
    }

    fun removeAllListeners() {
        notifsList.forEach {
            it.remove()
        }
    }
}