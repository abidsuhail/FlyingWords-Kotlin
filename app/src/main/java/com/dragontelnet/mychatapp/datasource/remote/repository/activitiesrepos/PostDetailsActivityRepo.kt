package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.repository.fragmentsrepos.FeedsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot

class PostDetailsActivityRepo : FeedsFragmentRepo() {
    fun getPost(post: Post): MutableLiveData<Post> {
        val postData: SingleLiveEvent<Post> = SingleLiveEvent()
        getOrganizedPostsCollRefOfUid(post.byUid!!).document(post.postId!!)
                .get().addOnSuccessListener { snapshot: DocumentSnapshot ->
                    val updatedPost: Post? = snapshot.toObject(Post::class.java)
                    updatedPost?.let {
                        postData.value = it
                    }
                }
        return postData
    }
}