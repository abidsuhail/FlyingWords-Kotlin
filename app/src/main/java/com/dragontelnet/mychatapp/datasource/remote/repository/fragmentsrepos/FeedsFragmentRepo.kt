package com.dragontelnet.mychatapp.datasource.remote.repository.fragmentsrepos

import com.dragontelnet.mychatapp.datasource.remote.repository.modules.postnotification.SendPostNotificationRepo
import com.dragontelnet.mychatapp.model.entity.Like
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.allUsersCollection
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getLikesRefOfPostUid
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.rootRef
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

open class FeedsFragmentRepo : SendPostNotificationRepo() {

    fun sendLikeToPost(post: Post): SingleLiveEvent<Int> {
        val sendLikeEvent = SingleLiveEvent<Int>()
        //val likeBatchWrites = rootRef.batch()
        //val removeLikeBatchWrites = rootRef.batch()
        val likesRef = getLikesRefOfPostUid(post.postId)
                .document(getCurrentUser()!!.uid)
        val postRefOfPostUid = MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(post.byUid!!).document(post.postId!!)
        val myFeedsRef = MyFirestoreDbRefs.getFeedsCollectionOfUid(getCurrentUser()!!.uid).document(post.postId!!)


        likesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                //remove like batch
                removeExistingLike(likesRef, postRefOfPostUid, myFeedsRef, sendLikeEvent)

            } else {
                //add like batch
                addLike(likesRef, post, postRefOfPostUid, myFeedsRef, sendLikeEvent)
            }
        }
        return sendLikeEvent
    }

    private fun addLike(likesRef: DocumentReference, post: Post, postRefOfPostUid: DocumentReference, myFeedsRef: DocumentReference, sendLikeEvent: SingleLiveEvent<Int>) {
        val likeBatch = rootRef
        val notifId = generateRandomNotifId(post)
        val postNotifRef = MyFirestoreDbRefs.getNotificationCollectionRef(post.byUid).document(notifId)
        val postNotifObj = getPostNotificationObj(post, PostNotification.LIKE_TYPE, notifId)

        //getPostNotificationObj will be null if i comment on my post
        likeBatch.runBatch { batch ->
            batch[likesRef] = Like(getCurrentUser()?.uid, post.postId)
            batch.update(myFeedsRef, Post.LIKERS_UIDS_LIST_FIELD, FieldValue.arrayUnion(getCurrentUser()!!.uid))
            batch.update(postRefOfPostUid, Post.LIKERS_UIDS_LIST_FIELD, FieldValue.arrayUnion(getCurrentUser()!!.uid))
            postNotifObj?.let {
                batch.set(postNotifRef, it)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                sendLikeEvent.value = SENT_LIKE_CODE
            }
        }
    }

    private fun removeExistingLike(likesRef: DocumentReference, postRefOfPostUid: DocumentReference, myFeedsRef: DocumentReference, sendLikeEvent: SingleLiveEvent<Int>) {
        rootRef.runBatch {
            it.delete(likesRef)
            it.update(myFeedsRef, Post.LIKERS_UIDS_LIST_FIELD, FieldValue.arrayRemove(getCurrentUser()?.uid))
            it.update(postRefOfPostUid, Post.LIKERS_UIDS_LIST_FIELD, FieldValue.arrayRemove(getCurrentUser()?.uid))
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                sendLikeEvent.value = REMOVED_LIKE_CODE
            }
        }
    }

    override fun getUser(uid: String): SingleLiveEvent<User> {
        val userSingleLiveEvent = SingleLiveEvent<User>()
        allUsersCollection.document(uid).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        user?.let {
                            userSingleLiveEvent.value = it
                        }
                    }
                }
        return userSingleLiveEvent
    }

    fun removeAllListeners() {}
    fun deletePost(updatedPost: Post): SingleLiveEvent<Boolean> {
        val delPostEvent = SingleLiveEvent<Boolean>()
        rootRef.runBatch { batch ->
            batch.delete(MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(getCurrentUser()?.uid!!)
                    .document(updatedPost.postId!!))

            batch.delete(MyFirestoreDbRefs.getFeedsCollectionOfUid(getCurrentUser()?.uid!!)
                    .document(updatedPost.postId!!))

        }.addOnSuccessListener {
            //post deleted
            delPostEvent.value = true
        }.addOnFailureListener {
            delPostEvent.value = false
        }

        return delPostEvent
    }

    fun checkFeedsEmptiness(feedsQuery: Query): SingleLiveEvent<Boolean> {
        val feedsEmptinessEvent = SingleLiveEvent<Boolean>()
        feedsQuery.get().addOnSuccessListener { snapshots ->
            feedsEmptinessEvent.value = snapshots.isEmpty
        }
        return feedsEmptinessEvent
    }

    companion object {
        const val REMOVED_LIKE_CODE = 0
        const val SENT_LIKE_CODE = 1
        private const val TAG = "FeedsFragmentRepo"
    }
}