package com.dragontelnet.mychatapp.ui.activities.registration

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.RegistrationDetailsActivityRepo
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyDaggerInjection
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class RegistrationActivityViewModel : ViewModel() {

    @Inject
    lateinit var repo: RegistrationDetailsActivityRepo

    init {
        MyDaggerInjection.registrationRepoComp?.inject(this)
    }

    fun isWritten(user: User?, context: Context?): LiveData<Boolean> {
        return repo.isWrittenSuccess(user, context)
    }

    fun getStableCurrentUser(): LiveData<User> {
        return repo.currentStableUser
    }

    fun isUsernameExists(username: String?, context: Context?): LiveData<Boolean> {
        return repo.isUsernameExists(username, context)
    }

    fun getDeviceToken(context: Context?): LiveData<String> {
        return repo.getDeviceToken(context)
    }

    fun getUploadedPhotoLink(uri: Uri, context: Context, storageReference: StorageReference): LiveData<String> {
        return repo.uploadPhoto(uri, context, storageReference)
    }


}