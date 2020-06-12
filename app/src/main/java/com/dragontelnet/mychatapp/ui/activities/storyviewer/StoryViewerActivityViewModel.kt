package com.dragontelnet.mychatapp.ui.activities.storyviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.StoryViewerActivityRepo
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem

class StoryViewerActivityViewModel : ViewModel() {
    private val repo = StoryViewerActivityRepo()
    fun destroyStory(story: Story) {
        repo.destroyStory(story)
    }

    fun getStorySeenCount(story: Story, storyItem: StoryItem): LiveData<String> {
        return repo.getStorySeenCount(story, storyItem)
    }
}