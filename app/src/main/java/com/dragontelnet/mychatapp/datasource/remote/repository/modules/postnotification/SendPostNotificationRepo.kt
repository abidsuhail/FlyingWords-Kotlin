package com.dragontelnet.mychatapp.datasource.remote.repository.modules.postnotification

import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.getCurrentOfflineUserFromBook
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.UserDetailsFetcher
import com.dragontelnet.mychatapp.model.entity.DateTime
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs

open class SendPostNotificationRepo : UserDetailsFetcher() {
    //type is like or comment
    //both like or comment is sent from here
    //appendToPostIdStr is string for appending  to post uid for making like and comment id both differently

    open fun getPostNotificationObj(updatedPost: Post, newType: String, mNotifId: String): PostNotification? {
        if (updatedPost.byUid != getCurrentUser()?.uid) {
            return PostNotification().apply {
                byUid = getCurrentUser()?.uid
                postByUid = updatedPost.byUid
                postId = updatedPost.postId
                if (updatedPost.postPhotoUrl == "") {
                    //status
                    val newContent = getCurrentOfflineUserFromBook?.name + " " + newType + " your status"
                    imgUrl = ""
                    content = newContent
                } else {
                    //photo
                    val newContent = getCurrentOfflineUserFromBook?.name + " " + newType + " your photo"
                    imgUrl = updatedPost.postPhotoUrl
                    content = newContent
                }

                if (newType == PostNotification.COMMENT_TYPE) {
                    updatedPost.lastComment?.content?.let {
                        commentContent = it
                    }
                }
                status = FirestoreKeys.DELIVERED
                notifId = mNotifId
                type = newType
                dateTime = DateTime(CurrentDateAndTime.currentDate, CurrentDateAndTime.currentTime, CurrentDateAndTime.timeStamp)
            }
        } else {
            return null
        }
    }

    open fun generateRandomNotifId(post: Post): String {
        return MyFirestoreDbRefs.getNotificationCollectionRef(post.byUid).document().id
    }
}