package com.dragontelnet.mychatapp.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.datetime.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.google.firebase.firestore.SetOptions
import java.util.*

class AppObserver : LifecycleObserver {

    private fun setUserState(status: String) {

        CurrentUser.getCurrentUser()?.let {
            val stateMap = HashMap<String, Any>()
            stateMap[MyConstants.FirestoreKeys.DATE] = CurrentDateAndTime.currentDate
            stateMap[MyConstants.FirestoreKeys.TIME] = CurrentDateAndTime.currentTime
            stateMap[MyConstants.FirestoreKeys.STATUS] = status
            MyFirestoreDbRefs.allUsersCollection
                    .document(it.uid).set(stateMap, SetOptions.merge()).addOnSuccessListener {
                    }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun setOfflineStatusOnDestroy() {
        setUserState(MyConstants.FirestoreKeys.OFFLINE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun setOfflineStatus() {
        setUserState(MyConstants.FirestoreKeys.OFFLINE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun setOnlineStatus() {
        setUserState(MyConstants.FirestoreKeys.ONLINE)
    }

}