package com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.FirebaseImageUploader
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.fragments.story.view.StoryFragment
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime.currentDate
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime.currentTime
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime.timeStamp
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.getAllStoriesDocumentRefOfUid
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.*
import java.util.*

class StoryFragmentRepo : FirebaseImageUploader() {
    private lateinit var myLiveStoryListener: ListenerRegistration
    private lateinit var friendListUidsListener: ListenerRegistration
    private lateinit var storyListenerRegis: ListenerRegistration
    private val mediatorLiveData = MediatorLiveData<Int>()
    private val listenerList = mutableListOf<ListenerRegistration>()
    private val myLiveStoryListenersList: MutableList<ListenerRegistration> = mutableListOf()
    fun addDataToDb(mImageUrl: String?): SingleLiveEvent<Boolean> {
        val storyUploadedEvent = SingleLiveEvent<Boolean>()
        //creating Single Story Item Object
        val singleStory = StoryItem().apply {
            date = currentDate
            time = currentTime
            imageUrl = mImageUrl
            timeStamp = CurrentDateAndTime.timeStamp
        }
        //creating array list of items
        val storiesList = ArrayList<StoryItem>()
        storiesList.add(singleStory)

        //create final Story entity
        val story = Story(timeStamp, getCurrentUser()?.uid, "", "", storiesList)

        //checking if story exists
        getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid!!)
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        //exists
                        //now update the story array list with single story entity
                        getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid!!)
                                .update(FirestoreCollection.STORIES_ITEM_LIST_ARRAY, FieldValue.arrayUnion(singleStory))
                                .addOnSuccessListener { storyUploadedEvent.value = true }
                                .addOnFailureListener { storyUploadedEvent.value = false }
                    } else {
                        //not exists
                        //create new whole Story entity
                        getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid!!)
                                .set(story).addOnSuccessListener { storyUploadedEvent.value = true }
                                .addOnFailureListener { storyUploadedEvent.value = false }
                    }
                }.addOnFailureListener { storyUploadedEvent.value = false }
        return storyUploadedEvent
    }

    fun checkStoryExistenceOfUid(uid: String?): SingleLiveEvent<Story>? {
        val storyLiveEvent = SingleLiveEvent<Story>()
        myLiveStoryListener = getAllStoriesDocumentRefOfUid(uid!!)
                .addSnapshotListener { documentSnapshot, _ ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val story = documentSnapshot.toObject(Story::class.java)
                        story?.storyItemList?.isNotEmpty()?.let {
                            storyLiveEvent.value = story
                        } ?: run { storyLiveEvent.value = null }

                    } else {
                        storyLiveEvent.value = null
                    }
                }
        myLiveStoryListenersList.add(myLiveStoryListener)
        return storyLiveEvent
    }

    fun destroyStory(story: Story) {
        val storyItemList: List<StoryItem>? = story.storyItemList
        storyItemList?.forEach {
            if (timeStamp - it.timeStamp >= StoryFragment.ONE_DAY_IN_MILLISECONDS) {
                delStoryFromDb(story, it)
            }
        }
    }

    private fun delStoryFromDb(mStory: Story, storyItem: StoryItem) {
        getAllStoriesDocumentRefOfUid(mStory.byUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val story: Story? = documentSnapshot.toObject(Story::class.java)
                    story?.let {
                        if (story.storyItemList!!.size <= 1) {
                            getAllStoriesDocumentRefOfUid(story.byUid)
                                    .delete().addOnSuccessListener {
                                        //removing from list
                                    }
                        } else {
                            getAllStoriesDocumentRefOfUid(story.byUid)
                                    .update(FirestoreCollection.STORIES_ITEM_LIST_ARRAY, FieldValue.arrayRemove(storyItem))
                                    .addOnSuccessListener { }
                        }
                    }
                }
    }

    fun queryStoryListCount(): MutableLiveData<Int> {
        val storyQuery = MyFirestoreDbRefs.getStoriesCollRefOfUid(getCurrentUser()?.uid!!)
        val myStoryCountEvent = MutableLiveData<Int>()
        storyQuery.addSnapshotListener { qs, _ ->
            if (qs != null && !qs.isEmpty) {
                myStoryCountEvent.value = qs.size()
            } else {
                myStoryCountEvent.value = 0
            }
        }
        return myStoryCountEvent
    }

    fun removeMyLiveStoryListener() {
        myLiveStoryListenersList.forEach {
            it.remove()
        }
        myLiveStoryListenersList.clear()

        listenerList.forEach {
            it.remove()
        }
        listenerList.clear()
    }

    fun getMyStoriesList(): MutableLiveData<List<Story>> {
        val storyList = hashSetOf<Story>()
        val storyListLiveData = MutableLiveData<List<Story>>()
        val storyQuery: Query = MyFirestoreDbRefs.getStoriesCollRefOfUid(getCurrentUser()?.uid!!)
        storyQuery.addSnapshotListener { qs, _ ->
            if (qs != null) {
                val changes = qs.documentChanges
                changes.forEach {
                    when (it.type) {
                        DocumentChange.Type.ADDED -> {
                            val storyAdded = it.document.toObject(Story::class.java)
                            MyFirestoreDbRefs.allUsersCollection.document(storyAdded.byUid!!).get()
                                    .addOnSuccessListener { ds ->
                                        val user = ds.toObject(User::class.java)
                                        storyAdded.ownerName = user?.name
                                        storyAdded.ownerProfileUrl = user?.profilePic
                                        storyList.remove(storyAdded)
                                        storyList.add(storyAdded)
                                        storyListLiveData.value = storyList.toList()
                                    }
                        }
                        DocumentChange.Type.REMOVED -> {
                            val storyRemoved = it.document.toObject(Story::class.java)
                            storyList.remove(storyRemoved)
                            storyListLiveData.value = storyList.toList()
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val storyModified = it.document.toObject(Story::class.java)
                            MyFirestoreDbRefs.allUsersCollection.document(storyModified.byUid!!).get()
                                    .addOnSuccessListener { ds ->
                                        val user = ds.toObject(User::class.java)
                                        storyModified.ownerName = user?.name
                                        storyModified.ownerProfileUrl = user?.profilePic
                                        storyList.remove(storyModified)
                                        storyList.add(storyModified)
                                        storyListLiveData.value = storyList.toList()
                                    }
                        }
                    }
                }
            } else {
                //null
            }
        }

        return storyListLiveData
    }

}