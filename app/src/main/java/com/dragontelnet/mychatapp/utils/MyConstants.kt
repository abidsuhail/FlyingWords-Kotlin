package com.dragontelnet.mychatapp.utils

class MyConstants {
    interface ActivityLaunch {
        companion object {
            const val START_LOGIN_ACTIVITY = 1
            const val START_REG_DETAILS_ACTIVITY = 2
            const val START_MAIN_ACTIVITY = 3
        }
    }

    interface FirestoreCollection {
        companion object {
            const val USERS_IDS: String = "users_ids"
            const val REGISTERED_USERS_COLLECTION = "reg_users"
            const val ALL_LOCS_COLLECTION = "all_locs"
            const val CHATS_COLLECTION = "chats"
            const val REQUESTS_COLLECTION = "requests"
            const val FRIENDS_COLLECTION = "friends"
            const val ALL_CHATS_COLLECTION = "all_chats"
            const val OLDER_CHATS_COLLECTION = "older_chats"
            const val RECEIVERS_LIST_COLLECTION = "receivers_list"
            const val STORIES_COLLECTION = "stories"
            const val ALL_STORIES = "all_stories"
            const val STORIES_ITEM_LIST_ARRAY = "storyItemList"
            const val SEEN_STORY_UIDS = "seen_story_uids"
            const val POSTS_COLLECTION = "posts"
            const val COMMENTS_COLLECTION = "comments"
            const val NOTIFICATIONS_COLLECTION = "notifications"
            const val LIKES_COLLECTION = "likes"
            const val POST_IDS_COLLECTION = "post_ids"
            const val FEEDS = "feeds"
            const val ORGANIZED_USERS_POSTS = "organized_user_posts"
            const val MY_POSTS = "my_posts"

        }
    }

    interface FirestoreKeysValues {
        companion object GENDER {
            const val MALE = "male"
            const val FEMALE = "female"
        }
    }

    interface FirestoreKeys {
        companion object {
            const val IS_TYPING = "isTyping"
            const val NAME = "name"
            const val PHONE = "phone"
            const val PROFILE_PIC = "profilePic"
            const val DEVICE_TOKEN = "deviceToken"
            const val UID = "uid"
            const val USERNAME = "username"
            const val SENT = "sent"
            const val RECEIVED = "received"
            const val DATE = "date"
            const val TIME = "time"
            const val DELIVERED = "Delivered"
            const val SEEN = "Seen"
            const val TIMESTAMP = "timeStamp"
            const val STATUS = "status"
            const val TYPING_FIELD = "typing..."
            const val ONLINE = "Online"
            const val OFFLINE = "Offline"
            const val BIO = "bio"
            const val BY_UID_KEY = "byUid"
        }
    }

    interface LocalDB {
        companion object {
            const val MY__PREFS = "MyPref"
            const val REG_DONE_KEY = "regDone"
            const val MY_NOTIFICATION_PREFS = "myNotificationSharedPref"
            const val RECEIVER_NOTIFICATION_FLAG = "receiverNotificationFlagPref"
        }
    }

    interface ProfileFriendRequestCodes {
        companion object {
            const val SENT = 1
            const val RECEIVED = 2
            const val SAME_USER = 3
            const val NO_REQUEST_EXISTS = 4
            const val UNKNOWN_REQUEST = 5
            const val FRIENDS = 6
        }
    }
}