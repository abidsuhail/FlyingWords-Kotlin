package com.dragontelnet.mychatapp.utils.firestore

import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection.Companion.ALL_STORIES
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection.Companion.FEEDS
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection.Companion.MY_POSTS
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection.Companion.ORGANIZED_USERS_POSTS
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection.Companion.STORIES_COLLECTION
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object MyFirestoreDbRefs {

    val rootRef: FirebaseFirestore
        get() = Firebase.firestore

    val allUsersCollection: CollectionReference
        get() = rootRef.collection(FirestoreCollection.REGISTERED_USERS_COLLECTION)

    fun getUidFriendsCollection(uid: String?): CollectionReference {
        return rootRef
                .collection(FirestoreCollection.FRIENDS_COLLECTION)
                .document(FirestoreCollection.FRIENDS_COLLECTION)
                .collection(uid!!)
    }

    val myFriendRequestsListRef: CollectionReference
        get() = rootRef
                .collection(FirestoreCollection.REQUESTS_COLLECTION)
                .document(FirestoreCollection.REQUESTS_COLLECTION)
                .collection(getCurrentUser()!!.uid)

    fun getFriendRequestsBuilderRef(receiverUid: String?, senderUid: String?): DocumentReference {
        return rootRef
                .collection(FirestoreCollection.REQUESTS_COLLECTION)
                .document(FirestoreCollection.REQUESTS_COLLECTION)
                .collection(receiverUid!!)
                .document(senderUid!!)
    }

    fun getChatsListCollectionRef(senderUid: String?, receiverUid: String?): CollectionReference {
        //all_chats/senderUid/receiverUid/receivers_list/chats
        return rootRef
                .collection(FirestoreCollection.ALL_CHATS_COLLECTION)
                .document(senderUid!!)
                .collection(receiverUid!!)
    }

    fun getLastChatDocumentRef(senderUid: String?, receiverUid: String?): DocumentReference {
        return rootRef
                .collection(FirestoreCollection.OLDER_CHATS_COLLECTION)
                .document(senderUid!!)
                .collection(FirestoreCollection.RECEIVERS_LIST_COLLECTION)
                .document(receiverUid!!)
    }

    fun getStoriesCollRefOfUid(userUid: String): CollectionReference {
        return rootRef.collection(STORIES_COLLECTION).document("users").collection(userUid)
    }

    fun getAllStoriesCollRef(): CollectionReference {
        return rootRef.collection(ALL_STORIES)
    }

    fun getAllStoriesDocumentRefOfUid(userUid: String?): DocumentReference {
        return getAllStoriesCollRef().document(userUid!!)
    }

    fun getOlderChatsRefOfUid(uid: String?): CollectionReference {
        return rootRef
                .collection(FirestoreCollection.OLDER_CHATS_COLLECTION)
                .document(uid!!)
                .collection(FirestoreCollection.RECEIVERS_LIST_COLLECTION)
    }


    fun getFeedsCollectionOfUid(uid: String) = rootRef.collection(FEEDS).document(uid).collection(FEEDS)

    fun getCommentsCollectionRefOfPostUid(postId: String): CollectionReference {
        return rootRef
                .collection(FirestoreCollection.COMMENTS_COLLECTION)
                .document(FirestoreCollection.COMMENTS_COLLECTION)
                .collection(postId)
    }

    fun getLikesRefOfPostUid(postId: String?): CollectionReference {
        return rootRef
                .collection(FirestoreCollection.LIKES_COLLECTION)
                .document(FirestoreCollection.POST_IDS_COLLECTION)
                .collection(postId!!)
    }

    fun getNotificationCollectionRef(userUid: String?): CollectionReference {
        return rootRef
                .collection(FirestoreCollection.NOTIFICATIONS_COLLECTION)
                .document(FirestoreCollection.USERS_IDS)
                .collection(userUid!!)
    }

    fun getOrganizedPostsCollRefOfUid(userUid: String): CollectionReference {
        return rootRef
                .collection(ORGANIZED_USERS_POSTS)
                .document(userUid)
                .collection(MY_POSTS)

    }
}