package com.dragontelnet.mychatapp.ui.fragments.story.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.StoryFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.User
import com.google.firebase.storage.StorageReference

class StoryViewModel : ViewModel() {
    private val repo: StoryFragmentRepo = StoryFragmentRepo()
    private val sortedStoryLiveData = MediatorLiveData<List<Story>>()
    fun getImageUrl(uri: Uri, context: Context, storageReference: StorageReference): LiveData<String> {
        return repo.uploadPhoto(uri, context, storageReference)
    }

    fun addDataToDb(imageUrl: String?): LiveData<Boolean> {
        return repo.addDataToDb(imageUrl)
    }

    fun checkLiveStoryExistenceOfUid(uid: String?): LiveData<Story>? {
        return repo.checkStoryExistenceOfUid(uid)
    }

    fun getUser(uid: String): LiveData<User> {
        return repo.getUser(uid)
    }

    fun destroyStory(story: Story?) {
        repo.destroyStory(story!!)
    }

    fun removeMyLiveStoryListener() {
        repo.removeMyLiveStoryListener()

    }

    fun queryStoryListCount(): LiveData<Int> = repo.queryStoryListCount()
    fun getMyStoriesList(): LiveData<List<Story>> {
        sortedStoryLiveData.addSource(repo.getMyStoriesList()) { storyList ->
            sortedStoryLiveData.value = storyList.sortedByDescending { it.storyItemList?.last()?.timeStamp }
        }
        return sortedStoryLiveData
    }

    companion object {
        private val TAG = "StoryViewModel"
    }

}