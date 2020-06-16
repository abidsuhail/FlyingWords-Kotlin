package com.dragontelnet.mychatapp.ui.activities.createpost

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.CreatePostActivityRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.utils.DateTimeFactory
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.livedata.SingleMediatorLiveEvent
import com.google.firebase.storage.StorageReference
import java.util.*

class CreatePostActivityViewModel : ViewModel() {
    private val repo: CreatePostActivityRepo = CreatePostActivityRepo()

    fun getPostObjWithImageUrl(croppedImageUri: Uri?, context: Context?, mPostId: String?, mCaption: String?, storageReference: StorageReference?): LiveData<Post?> {
        val postSingleMediatorLiveEvent = SingleMediatorLiveEvent<Post?>()
        postSingleMediatorLiveEvent.addSource(repo.uploadPhoto(croppedImageUri!!, context!!, storageReference!!))
        { mPostPhotoUrl: String? ->
            val post = Post()
            with(post)
            {
                postId = mPostId
                postPhotoUrl = mPostPhotoUrl
                byUid = CurrentUser.getCurrentUser()?.uid
                dateTime = DateTimeFactory.currentDateTimeObj
                commentsCount = 0L
                likersUids = ArrayList()
                lastComment = null
                caption = if (!TextUtils.isEmpty(mCaption)) mCaption else ""
            }
            postSingleMediatorLiveEvent.setValue(post)
        }
        return postSingleMediatorLiveEvent
    }

    fun isWrittenSuccess(post: Post?, mCaption: String, mPostId: String?): LiveData<Boolean> {
        return if (post != null) {
            //its status with photo
            repo.isWrittenSuccess(post)
        } else {
            val postWithoutPhoto = Post().apply {
                postId = mPostId
                postPhotoUrl = ""
                byUid = CurrentUser.getCurrentUser()?.uid
                dateTime = DateTimeFactory.currentDateTimeObj
                commentsCount = 0L
                likersUids = ArrayList()
                lastComment = null
                caption = if (mCaption.isNotBlank()) {
                    mCaption
                } else {
                    ""
                }
            }
            //with profile
            repo.isWrittenSuccess(postWithoutPhoto)
        }
    }


}