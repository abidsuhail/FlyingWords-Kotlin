package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.fragments.story.view.StoryFragment
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.FieldValue

class StoryViewerActivityRepo {
    fun destroyStory(story: Story) {
        val storyItemList = story.storyItemList

        storyItemList?.forEach {
            if (CurrentDateAndTime.timeStamp - it.timeStamp!! >= StoryFragment.ONE_DAY_IN_MILLISECONDS) {
                MyFirestoreDbRefs.getAllStoriesDocumentRefOfUid(story.byUid)
                        .update(FirestoreCollection.STORIES_ITEM_LIST_ARRAY, FieldValue.arrayRemove(it))
                        .addOnSuccessListener { }.addOnFailureListener { }
            }
        }

    }

    fun getStorySeenCount(story: Story, storyItem: StoryItem): SingleLiveEvent<String> {
        val seenEvent = SingleLiveEvent<String>()
        story.byUid?.let {
            MyFirestoreDbRefs.rootRef.collection(FirestoreCollection.SEEN_STORY_UIDS)
                    .document(it)
                    .collection((storyItem.timeStamp).toString()).addSnapshotListener { snapshot, e ->
                        seenEvent.setValue((snapshot?.size() ?: 0).toString())
                    }
        }
        return seenEvent
    }
}