package com.dragontelnet.mychatapp.datasource.remote.repository.modules

import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot

open class UserDetailsFetcher {
    open fun getUser(uid: String): SingleLiveEvent<User> {
        val anyUserSingleLiveEvent = SingleLiveEvent<User>()
        MyFirestoreDbRefs.allUsersCollection.document(uid).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        user?.let {
                            anyUserSingleLiveEvent.value = user
                        }
                    }
                }
        return anyUserSingleLiveEvent
    }

    val currentStableUser: SingleLiveEvent<User>
        get() {
            val userEvent = SingleLiveEvent<User>()
            MySharedPrefs.getCurrentOfflineUserFromBook?.let {
                userEvent.setValue(it)
            } ?: run {
                MyFirestoreDbRefs.allUsersCollection.document(getCurrentUser()!!.uid)
                        .get()
                        .addOnSuccessListener { snapshot: DocumentSnapshot ->
                            val user = snapshot.toObject(User::class.java)
                            MySharedPrefs.putUserObjToBook(user)
                            userEvent.setValue(user)
                        }
            }
            return userEvent
        }
}