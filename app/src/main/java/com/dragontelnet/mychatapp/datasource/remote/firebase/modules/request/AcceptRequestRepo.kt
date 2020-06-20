package com.dragontelnet.mychatapp.datasource.remote.firebase.modules.request

import android.content.Context
import android.widget.Toast
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import java.util.*

open class AcceptRequestRepo : DeclineRequestRepo() {
    fun acceptRequest(receivedUid: String, context: Context?): SingleLiveEvent<Boolean> {
        val acceptRequestEvent = SingleLiveEvent<Boolean>()

        getCurrentUser()?.let { currentUser ->
            val delFromUserUid = MyFirestoreDbRefs.getFriendRequestsBuilderRef(receivedUid, currentUser.uid)
            val delFromMyUid = MyFirestoreDbRefs.getFriendRequestsBuilderRef(currentUser.uid, receivedUid)
            val addUserToMyFriendListRef = MyFirestoreDbRefs.getUidFriendsCollection(currentUser.uid).document(receivedUid)
            val addMeToUserFriendListRef = MyFirestoreDbRefs.getUidFriendsCollection(receivedUid).document(currentUser.uid)


            val addUserToMyFriendListRefHashMap = hashMapOf<String, Any>()
            addUserToMyFriendListRefHashMap["uid"] = receivedUid
            val addMeToUserFriendListRefHashMap = HashMap<String, Any>()
            addMeToUserFriendListRefHashMap["uid"] = currentUser.uid
            MyFirestoreDbRefs.getFriendRequestsBuilderRef(receivedUid, getCurrentUser()?.uid)
                    .get().addOnSuccessListener { snap ->
                        if (snap.exists()) {
                            MyFirestoreDbRefs.rootRef.runBatch {
                                it.delete(delFromMyUid)
                                it.delete(delFromUserUid)
                                it.set(addMeToUserFriendListRef, addMeToUserFriendListRefHashMap)
                                it.set(addUserToMyFriendListRef, addUserToMyFriendListRefHashMap)
                            }.addOnCompleteListener {
                                acceptRequestEvent.value = it.isSuccessful
                                Toast.makeText(context, "Request Accepted!!!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // he/she had cancelled sent req.
                            //del sender req from my collection because in other collection my uid not present
                            delFromMyUid.delete().addOnSuccessListener {
                                Toast.makeText(context, "Error:Request was cancelled by sender !!", Toast.LENGTH_SHORT).show()
                                acceptRequestEvent.value = false
                            }
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        acceptRequestEvent.value = false
                    }
        }
        return acceptRequestEvent
    }
}