package com.dragontelnet.mychatapp.utils

import android.graphics.drawable.Animatable
import android.util.Log
import com.dragontelnet.mychatapp.model.entity.SeenIds
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.rootRef
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo

class StoryImgControllerListener(private val storyByUid: String, private val timeStamp: Long) : BaseControllerListener<ImageInfo?>() {
    override fun onFinalImageSet(id: String, imageInfo: ImageInfo?, animatable: Animatable?) {
        super.onFinalImageSet(id, imageInfo, animatable)
        if (storyByUid != getCurrentUser()!!.uid) {
            //add to db
            Log.d(TAG, "onFinalImageSet: in byUid$storyByUid timestamp : $timeStamp")

            //story image fully loaded now set seen to db
            val seenIds = SeenIds(getCurrentUser()!!.uid,
                    CurrentDateAndTime.currentDate,
                    CurrentDateAndTime.currentTime)

            // set seen status in story owner ref by myUid
            rootRef.collection(FirestoreCollection.Companion.SEEN_STORY_UIDS)
                    .document(storyByUid)
                    .collection(timeStamp.toString())
                    .document(getCurrentUser()!!.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.exists()) {
                            //setting seen status to db when previous seen not exists
                            documentSnapshot.reference.set(seenIds)
                        }
                    }
        }
    }

    companion object {
        private const val TAG = "StoryImgController"
    }

}