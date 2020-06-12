package com.dragontelnet.mychatapp.model.entity

data class PostNotification(var notifId: String?,
                            var byUid: String?,
                            var postByUid: String?,
                            var postOwnerName: String?,
                            var postOwnerProfilePic: String?,
                            var notifOwnerName: String?,
                            var notifOwnerProfilePic: String?,
                            var postId: String?,
                            var status: String?,
                            var content: String?,
                            var type: String?,
                            var imgUrl: String?,
                            var post: Post?,
                            var dateTime: DateTime?,
                            var commentContent: String) {

    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", null, null, "")

    companion object {
        const val LIKE_TYPE = "liked"
        const val COMMENT_TYPE = "commented on"
        const val APPEND_LIKE_TO_POST_ID = "like"
        const val APPEND_COMMENT_TO_POST_ID = "comment"
        const val ORDER_BY_TIMESTAMP = "dateTime.timeStamp"
    }

    override fun hashCode(): Int {
        return postByUid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is PostNotification) {
            return other.notifId == notifId
        }

        return false
    }
}