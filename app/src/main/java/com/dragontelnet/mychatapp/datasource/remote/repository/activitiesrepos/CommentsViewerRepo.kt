package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.postnotification.SendPostNotificationRepo
import com.dragontelnet.mychatapp.model.entity.Comment
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CommentsViewerRepo : SendPostNotificationRepo() {
    private var commentsEmptinessListener: ListenerRegistration? = null

    fun sendComment(post: Post, content: String): SingleLiveEvent<Post?> {
        val commentSentFlag: SingleLiveEvent<Post?> = SingleLiveEvent()
        val commentId = getCommentId(post.postId!!)
        val comment = getCommentObj(commentId, content)
        val commentRef = getCommentRef(commentId, post.postId!!)
        val postRef = getOrganizedCurrentPostRef(post)
        val myFeedsRef = MyFirestoreDbRefs.getFeedsCollectionOfUid(getCurrentUser()!!.uid).document(post.postId!!)
        post.lastComment = comment //updating post obj

        setUpBatchWrites(commentRef, postRef, myFeedsRef, comment, post, commentSentFlag)
        return commentSentFlag
    }

    private fun getOrganizedCurrentPostRef(post: Post): DocumentReference {
        return MyFirestoreDbRefs
                .getOrganizedPostsCollRefOfUid(post.byUid!!)
                .document(post.postId!!)
    }

    private fun getCommentRef(commentId: String, postId: String): DocumentReference {
        return MyFirestoreDbRefs
                .getCommentsCollectionRefOfPostUid(postId)
                .document(commentId)
    }

    private fun setUpBatchWrites(commentRef: DocumentReference, postRef: DocumentReference, myFeedsRefOfUid: DocumentReference, comment: Comment, post: Post, commentSentFlag: SingleLiveEvent<Post?>) {
        val commentSendBatch = MyFirestoreDbRefs.rootRef

        val notifId = generateRandomNotifId(post)
        val postNotifRef = MyFirestoreDbRefs.getNotificationCollectionRef(post.byUid).document(notifId)
        val postNotifObj = getPostNotificationObj(post, PostNotification.COMMENT_TYPE, notifId)

        //create batch of send notification tooo
        commentSendBatch.runBatch { batch ->
            batch[commentRef] = comment
            batch.update(postRef, Post.COMMENTS_COUNT_FIELD, FieldValue.increment(1))
            batch.update(postRef, Post.LAST_COMMENT_FIELD, comment)

            batch.update(myFeedsRefOfUid, Post.COMMENTS_COUNT_FIELD, FieldValue.increment(1))
            batch.update(myFeedsRefOfUid, Post.LAST_COMMENT_FIELD, comment)
            postNotifObj?.let {
                batch.set(postNotifRef, it)
            }

        }.addOnSuccessListener {
            //comment sent
            post.lastComment = comment
            val updatedCommentCount = post.commentsCount + 1
            post.commentsCount = updatedCommentCount

            //saving comment local,to view in feeds
            MySharedPrefs.putPostObjToBook(post)

            commentSentFlag.value = post
        }.addOnFailureListener { commentSentFlag.value = null }
    }

    private fun getCommentObj(mCommentId: String, mContent: String): Comment {
        val comment = Comment()
        with(comment)
        {
            commentId = mCommentId
            content = mContent
            commentByUid = getCurrentUser()?.uid ?: "NULL"
            date = CurrentDateAndTime.currentDate
            time = CurrentDateAndTime.currentTime
            timeStamp = CurrentDateAndTime.timeStamp
        }
        return comment
    }

    private fun getCommentId(postId: String): String {
        return MyFirestoreDbRefs
                .getCommentsCollectionRefOfPostUid(postId)
                .document()
                .id
    }

    fun getCommentsEmptiness(query: Query): SingleLiveEvent<Boolean> {
        val commentsEmptinessEvent = SingleLiveEvent<Boolean>()
        commentsEmptinessListener = query.addSnapshotListener { querySnapshot, _ ->
            commentsEmptinessEvent.value = querySnapshot != null && querySnapshot.isEmpty
        }
        return commentsEmptinessEvent
    }

}