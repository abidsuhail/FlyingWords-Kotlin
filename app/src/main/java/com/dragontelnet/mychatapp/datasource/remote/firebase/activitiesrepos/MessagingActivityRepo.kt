package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.retrofit.FcmMessagingService
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.*

class MessagingActivityRepo : FcmMessagingService() {
    private var lastChatListener: ListenerRegistration? = null
    private var lastMsgNotifyListener: ListenerRegistration? = null
    private var liveUserDetailsListener: ListenerRegistration? = null
    private var messagesEmptListener: ListenerRegistration? = null
    private var seenEventListener: ListenerRegistration? = null
    private var timer: Timer? = null
    fun sendMessage(chatMsg: String, receiverUid: String, context: Context, storyReplyImgLink: String): SingleLiveEvent<Boolean> {
        val event = SingleLiveEvent<Boolean>()
        //check if receiverUid exists in my friends collection
        checkReceiverIsFriend(chatMsg, receiverUid, context, event, storyReplyImgLink)
        return event
    }

    private fun checkReceiverIsFriend(chatMsg: String, receiverUid: String, context: Context, event: SingleLiveEvent<Boolean>, storyReplyImgLink: String) {
        MyFirestoreDbRefs.getUidFriendsCollection(getCurrentUser()?.uid)
                .document(receiverUid)

        MyFirestoreDbRefs.getUidFriendsCollection(getCurrentUser()!!.uid)
                .document(receiverUid)
                .get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val chatKey = getMsgKey(receiverUid)
                        sendMsgBatch(chatKey, receiverUid, getChatObject(chatMsg, receiverUid, chatKey, storyReplyImgLink), event)
                    } else {
                        Toast.makeText(context, "You both are no longer friends", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
    }

    private fun sendMsgBatch(chatKey: String, receiverUid: String, chat: Chat, event: SingleLiveEvent<Boolean>) {
        val myChatRef = MyFirestoreDbRefs
                .getChatsListCollectionRef(getCurrentUser()?.uid, receiverUid)
                .document(chatKey)
        val receiverChatRef = MyFirestoreDbRefs
                .getChatsListCollectionRef(receiverUid, getCurrentUser()?.uid)
                .document(chatKey)
        val lastChatMyRef = MyFirestoreDbRefs
                .getLastChatDocumentRef(getCurrentUser()?.uid, receiverUid)
        val lastChatReceiverRef = MyFirestoreDbRefs
                .getLastChatDocumentRef(receiverUid, getCurrentUser()?.uid)

        MyFirestoreDbRefs.rootRef.runBatch {
            it[myChatRef] = chat //adding chat to my chat ref
            it[receiverChatRef] = chat //adding chat to receiver chat ref
            chat.receiverUid = receiverUid //changing receiver uid for last chat
            it[lastChatMyRef, chat] = SetOptions.merge() //add last chat to my older chat ref
            chat.receiverUid = getCurrentUser()?.uid //changing receiver uid for last chat
            it[lastChatReceiverRef, chat] = SetOptions.merge() //adding last chat to receiver chat ref
        }.addOnCompleteListener {
            event.value = it.isSuccessful
        }
    }

    private fun getMsgKey(receiverUid: String): String {
        return MyFirestoreDbRefs
                .getChatsListCollectionRef(getCurrentUser()!!.uid, receiverUid)
                .document()
                .id
    }

    private fun getChatObject(chatMsg: String, mReceiverUid: String, chatKey: String, storyImgLinkReply: String): Chat {
        return Chat().apply {
            byUid = getCurrentUser()?.uid
            content = chatMsg
            msgKey = chatKey
            receiverUid = mReceiverUid
            storyPhotoLink = storyImgLinkReply
        }
    }

    fun notifyOnSentLastMsg(receiverUid: String?, context: Context?): SingleLiveEvent<Int> {
        val lastMsgNotifyEvent = SingleLiveEvent<Int>()
        val query: Query = MyFirestoreDbRefs.getChatsListCollectionRef(getCurrentUser()!!.uid, receiverUid)
        lastMsgNotifyListener = query.addSnapshotListener { snapshot, e ->
            e?.let { Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
            snapshot?.let { lastMsgNotifyEvent.value = snapshot.documents.size }
        }
        return lastMsgNotifyEvent
    }

    fun addSeenListener(receiverUid: String, context: Context?) {
        val lastReceiverChatQuery = MyFirestoreDbRefs
                .getChatsListCollectionRef(receiverUid, getCurrentUser()?.uid)
                .orderBy(FirestoreKeys.TIMESTAMP)
                .limitToLast(1)
        val seenMap = mutableMapOf<String, Any>()
        seenEventListener = lastReceiverChatQuery.addSnapshotListener(EventListener { snapshot, e ->
            e?.let { Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
            snapshot?.let {
                if (it.documents.isNotEmpty()) {
                    val lastChatRef = MyFirestoreDbRefs.getOlderChatsRefOfUid(getCurrentUser()?.uid)
                            .document(receiverUid)
                    val receiverLastChatRef = it.documents[0].reference
                    seenMap[FirestoreKeys.STATUS] = FirestoreKeys.SEEN

                    MyFirestoreDbRefs.rootRef.runBatch { batch ->
                        batch.update(receiverLastChatRef, seenMap)
                        batch.update(lastChatRef, seenMap)
                    }.addOnCompleteListener {/* seen*/ }
                }
            }
        })
    }

    fun removeSeenListener() {
        seenEventListener?.remove()
        messagesEmptListener?.remove()
        liveUserDetailsListener?.remove()
        liveUserDetailsListener?.remove()
    }

    fun getLiveChatUserDetails(uid: String, context: Context): MutableLiveData<User> {
        val liveUser = MutableLiveData<User>()
        liveUserDetailsListener = MyFirestoreDbRefs.allUsersCollection.document(uid)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        user?.let {
                            liveUser.value = it
                        }
                    }
                }
        return liveUser
    }

    fun setTypingStatus(receiverUser: User) {

        //stop timer
        timer?.cancel()
        val hashMap = HashMap<String, Any>()
        hashMap["typing"] = "typing"
        MyFirestoreDbRefs.getLastChatDocumentRef(receiverUser.uid, getCurrentUser()?.uid)
                .update(hashMap)
    }

    fun stopTypingStatus(receiverUser: User) {

        //schedule timer for 0.6 sec
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "run: typed")
                val hashMap = HashMap<String, Any>()
                hashMap["typing"] = "nottyping"
                MyFirestoreDbRefs.getLastChatDocumentRef(receiverUser.uid, getCurrentUser()?.uid)
                        .update(hashMap)
            }
        }, 600)
    }

    fun checkMessagesEmptiness(query: Query): MutableLiveData<Boolean> {
        val messagesEmptEvent = MutableLiveData<Boolean>()
        messagesEmptListener = query.addSnapshotListener { qs, _ ->
            messagesEmptEvent.value = (qs != null && qs.isEmpty)
        }
        return messagesEmptEvent
    }

    fun getLastChatDocRef(myuid: String, receiverUid: String): MutableLiveData<String> {
        val lastChatEvent = MutableLiveData<String>()
        lastChatListener = MyFirestoreDbRefs.getLastChatDocumentRef(myuid, receiverUid)
                .addSnapshotListener { ds, _ ->
                    if (ds != null && ds.exists()) {
                        val chat = ds.toObject(Chat::class.java)
                        chat?.let {
                            lastChatEvent.value = it.isTyping
                        }
                    } else {
                        lastChatEvent.value = "notyping"
                    }
                }
        return lastChatEvent
    }

    fun isChatUserIsFriend(receiverUid: String): SingleLiveEvent<Boolean> {
        val chatUserFriendEvent = SingleLiveEvent<Boolean>()
        MyFirestoreDbRefs.getUidFriendsCollection(getCurrentUser()?.uid).document(receiverUid)
                .get()
                .addOnSuccessListener { snap ->
                    chatUserFriendEvent.value = snap.exists()
                }
        return chatUserFriendEvent
    }

    companion object {
        private const val TAG = "MessagingFragmentRepo"
    }
}