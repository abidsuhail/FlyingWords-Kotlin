package com.dragontelnet.mychatapp.utils.firestore

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object MyFirebaseStorageRefs {
    val storageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference

    @JvmStatic
    val storiesStorageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference.child("stories")

    val postsStorageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference.child("posts")

    val profilePhotosStorageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference.child("profile_pics")
}