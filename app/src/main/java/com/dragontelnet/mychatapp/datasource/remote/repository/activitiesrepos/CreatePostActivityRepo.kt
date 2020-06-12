package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.FirebaseImageUploader
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

class CreatePostActivityRepo : FirebaseImageUploader() {
    fun isWrittenSuccess(post: Post): MutableLiveData<Boolean> {
        val isWrittenSuccessEvent = SingleLiveEvent<Boolean>()

        /*     MyFirestoreDbRefs.rootRef
                     .collection(MyConstants.FirestoreCollection.REQUESTS_COLLECTION)
                     .document(MyConstants.FirestoreCollection.REQUESTS_COLLECTION)
                     .collection(post.byUid!!)
                     .get().addOnSuccessListener {snap->
                         val friendsList = snap.toObjects(Friend::class.java)

                             MyFirestoreDbRefs.rootRef.runBatch {batch->
                                 friendsList.forEach { friend ->
                                     val friendUid = friend.uid
                                     val ref= MyFirestoreDbRefs.getFeedsCollectionOfUid(friendUid!!).document(post.postId!!)
                                     batch.set(ref,post)
                                 }
                             }.addOnSuccessListener {

                             }
                     }*/



        post.postId?.let { postId ->
            val myFeedsColl = MyFirestoreDbRefs.getFeedsCollectionOfUid(CurrentUser.getCurrentUser()!!.uid).document(postId)
            val organizedPostsColl = MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(CurrentUser.getCurrentUser()!!.uid).document(postId)

            MyFirestoreDbRefs.rootRef.runBatch { batch ->
                batch.set(myFeedsColl, post)
                batch.set(organizedPostsColl, post)
            }.addOnCompleteListener {
                isWrittenSuccessEvent.value = it.isSuccessful
            }

            /*MyFirestoreDbRefs.postsCollectionRef
                    .document(it)
                    .set(post).addOnCompleteListener { task: Task<Void?> ->
                        if (task.isSuccessful) {
                            isWrittenSuccessEvent.setValue(true)
                        } else {
                            isWrittenSuccessEvent.setValue(false)
                        }
                    }*/
        }
        return isWrittenSuccessEvent
    }
}