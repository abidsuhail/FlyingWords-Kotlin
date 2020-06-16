package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.FirebaseImageUploader
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

class CreatePostActivityRepo : FirebaseImageUploader() {
    fun isWrittenSuccess(post: Post): MutableLiveData<Boolean> {
        val isWrittenSuccessEvent = SingleLiveEvent<Boolean>()

        post.postId?.let { postId ->
            val myFeedsColl = MyFirestoreDbRefs.getFeedsCollectionOfUid(CurrentUser.getCurrentUser()!!.uid).document(postId)
            val organizedPostsColl = MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(CurrentUser.getCurrentUser()!!.uid).document(postId)

            MyFirestoreDbRefs.rootRef.runBatch { batch ->
                batch.set(myFeedsColl, post)
                batch.set(organizedPostsColl, post)
            }.addOnCompleteListener {
                isWrittenSuccessEvent.value = it.isSuccessful
            }

        }
        return isWrittenSuccessEvent
    }
}