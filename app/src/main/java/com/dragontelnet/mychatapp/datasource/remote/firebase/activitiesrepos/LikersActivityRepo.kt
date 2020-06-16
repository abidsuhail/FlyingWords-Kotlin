package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

class LikersActivityRepo {
    fun getLikersUserList(post: Post): SingleLiveEvent<List<User>> {
        val likersUserListEvent = SingleLiveEvent<List<User>>()
        val usersList = mutableListOf<User>()
        post.likersUids?.forEach { userUid ->
            MyFirestoreDbRefs.allUsersCollection.document(userUid!!).get()
                    .addOnSuccessListener {
                        val userObj = it.toObject(User::class.java)
                        userObj?.let { user ->
                            usersList.add(user)
                            likersUserListEvent.value = usersList
                        }
                    }
        }
        return likersUserListEvent
    }
}