package com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.datasource.remote.repository.modules.FirebaseImageUploader
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

class RegistrationDetailsActivityRepo : FirebaseImageUploader() {
    fun isWrittenSuccess(user: User?, context: Context?): SingleLiveEvent<Boolean> {
        val liveEvent = SingleLiveEvent<Boolean>()
        MyFirestoreDbRefs.allUsersCollection
                .document(getCurrentUser()!!.uid)
                .set(user!!, SetOptions.merge()).addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        liveEvent.value = true
                        MySharedPrefs.putRegistrationDone(context)
                        MySharedPrefs.putUserObjToBook(user)
                    } else {
                        liveEvent.setValue(false)
                    }
                }
        return liveEvent
    }

    fun isUsernameExists(usernameStr: String?, context: Context?): SingleLiveEvent<Boolean> {
        val isUsernameExistsEvent = SingleLiveEvent<Boolean>()
        MyFirestoreDbRefs.allUsersCollection.whereEqualTo(FirestoreKeys.USERNAME, usernameStr)
                .get()
                .addOnSuccessListener { snapshot: QuerySnapshot? ->
                    if (snapshot != null) {
                        if (snapshot.size() == 0) {
                            isUsernameExistsEvent.setValue(false)
                        } else {
                            isUsernameExistsEvent.setValue(true)
                        }
                    } else {
                        isUsernameExistsEvent.setValue(false)
                    }
                }.addOnFailureListener { e: Exception -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
        return isUsernameExistsEvent
    }

    fun getDeviceToken(context: Context?): SingleLiveEvent<String> {
        val liveEvent = SingleLiveEvent<String>()
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task: Task<InstanceIdResult?> ->
                    if (!task.isSuccessful) {
                        if (task.exception != null) {
                            Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                        return@addOnCompleteListener
                    }
                    // Get new Instance ID token
                    if (task.result != null) {
                        Log.d("token", "onComplete: token " + task.result!!.token)
                        liveEvent.value = task.result!!.token
                    }
                }
        return liveEvent
    }

    companion object {
        private const val TAG = "RegistrationDetailsActivityRepo"
    }
}