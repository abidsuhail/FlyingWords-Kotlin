package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.UserDetailsFetcher
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.DeviceTokenMap
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import java.util.*
import kotlin.collections.ArrayList


class MainActivityRepo : UserDetailsFetcher() {
    private var liveFriendReqListener: ListenerRegistration? = null
    private var unreadChatListener: ListenerRegistration? = null
    private var unreadNotifListener: ListenerRegistration? = null
    private val reqSeenListenerList: MutableList<ListenerRegistration> = ArrayList()
    private val unreadChatListenerList: MutableList<ListenerRegistration> = ArrayList()
    private val unreadNotifListenerList: MutableList<ListenerRegistration> = ArrayList()

    fun setUserState(status: String): SingleLiveEvent<Boolean> {
        val stateEvent = SingleLiveEvent<Boolean>()
        val stateMap = HashMap<String, Any>()
        stateMap[FirestoreKeys.DATE] = CurrentDateAndTime.currentDate
        stateMap[FirestoreKeys.TIME] = CurrentDateAndTime.currentTime
        stateMap[FirestoreKeys.STATUS] = status

        CurrentUser.getCurrentUser()?.let {
            MyFirestoreDbRefs.allUsersCollection
                    .document(it.uid).set(stateMap, SetOptions.merge()).addOnSuccessListener {
                        stateEvent.value = true
                    }
        }
        return stateEvent
    }

    private fun getDeviceToken(context: Context) {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task: Task<InstanceIdResult?> ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }

                        return@addOnCompleteListener
                    }
                    // Get new Instance ID token
                    task.result?.let {
                        Log.d("token", "onComplete: token " + it.token)
                        setDeviceTokenToDb(it.token)
                    }
                }
    }

    private fun setDeviceTokenToDb(token: String) {
        val deviceTokenMap = DeviceTokenMap(token)
        MyFirestoreDbRefs.allUsersCollection
                .document(CurrentUser.getCurrentUser()!!.uid)
                .set(deviceTokenMap.toMap(), SetOptions.merge())
                .addOnSuccessListener { aVoid: Void? ->
                    val user: User? = MySharedPrefs.getCurrentOfflineUserFromBook
                    user?.let {
                        it.deviceToken = deviceTokenMap.deviceToken
                        MySharedPrefs.putUserObjToBook(user)
                    }
                }
    }

    fun getAndSetDeviceTokenToDb(context: Context) {
        getDeviceToken(context)
    }

    fun liveFriendRequestsList(): MutableLiveData<List<FriendRequest>?> {
        val liveFriendReqList = MutableLiveData<List<FriendRequest>?>()
        liveFriendReqListener = MyFirestoreDbRefs.myFriendRequestsListRef
                .addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val friendRequestList = snapshot.toObjects(FriendRequest::class.java)
                        liveFriendReqList.setValue(friendRequestList)
                    } else {
                        liveFriendReqList.setValue(ArrayList())
                    }
                }
        reqSeenListenerList.add(liveFriendReqListener!!)
        return liveFriendReqList
    }

    //seen,remove badge
    fun liveAllLastChatsList(): MutableLiveData<List<Chat>?> {
        val liveLastChat = MutableLiveData<List<Chat>?>()
        val query: Query = MyFirestoreDbRefs.getOlderChatsRefOfUid(CurrentUser.getCurrentUser()?.uid)
        unreadChatListener = query.addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (snapshot != null && snapshot.size() > 0) {
                val lastChatList = snapshot.toObjects(Chat::class.java)
                liveLastChat.setValue(lastChatList)
            } else {
                //seen,remove badge
                liveLastChat.setValue(ArrayList())
            }
        }
        unreadChatListenerList.add(unreadChatListener!!)
        return liveLastChat
    }

    fun liveNotificationsListener(): MutableLiveData<List<PostNotification>> {
        val liveNotifEvent = MutableLiveData<List<PostNotification>>()
        val query = MyFirestoreDbRefs.getNotificationCollectionRef(CurrentUser.getCurrentUser()?.uid)
        unreadNotifListener = query.addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val notifList = snap.toObjects(PostNotification::class.java)
                liveNotifEvent.value = notifList
            } else {
                liveNotifEvent.value = ArrayList()
            }
        }
        unreadNotifListenerList.add(unreadNotifListener!!)
        return liveNotifEvent
    }

    fun removeAllListeners() {

        liveFriendReqListener?.let {
            reqSeenListenerList.forEach {
                it.remove()
            }
            reqSeenListenerList.clear()
        }

        unreadChatListener?.let {
            unreadChatListenerList.forEach {
                it.remove()
            }
            unreadChatListenerList.clear()
        }

        unreadNotifListener?.let {
            unreadNotifListenerList.forEach {
                it.remove()
            }
            unreadNotifListenerList.clear()
        }
    }

    companion object {
        private const val TAG = "MainActivityRepo"
    }
}
