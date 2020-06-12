package com.dragontelnet.mychatapp.model.entity

import java.io.Serializable

data class Post(var byUid: String?,
                var postOwnerName: String?,
                var postOwnerProfilePic: String?,
                var caption: String?,
                var postPhotoUrl: String?,
                var postId: String?,
                var commentsCount: Long,
                var likersUids: MutableList<String?>?,
                var lastComment: Comment?,
                var dateTime: DateTime?) : Serializable {

    //for firestore ui
    constructor() : this("", "", "", "", "", "", 0, emptyList<String>().toMutableList(), null, null) {}

    companion object {
        const val ORDER_BY_TIMESTAMP = "dateTime.timeStamp"
        const val POSTS_BOOK_KEY = "posts"
        const val COMMENTS_COUNT_FIELD = "commentsCount"
        const val LAST_COMMENT_FIELD = "lastComment"
        const val LIKERS_UIDS_LIST_FIELD = "likersUids"
    }
}