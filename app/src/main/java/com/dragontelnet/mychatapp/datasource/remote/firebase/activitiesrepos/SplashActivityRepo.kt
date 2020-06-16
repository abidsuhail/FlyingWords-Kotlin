package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent

class SplashActivityRepo(private val context: Context) {

    //waiting for thread to finish i.e 2.5 seconds
    fun timerOver(): SingleLiveEvent<Int> {
        val activityLiveEvent = SingleLiveEvent<Int>()
        val handler = Handler()
        handler.postDelayed({
            getCurrentUser()?.let {
                if (MySharedPrefs.isRegistrationDoneExists(context)) {
                    //local shared prefs key exists
                    activityLiveEvent.setValue(MyConstants.ActivityLaunch.START_MAIN_ACTIVITY)
                } else {
                    //local shared prefs NOT exists
                    //now checking user in FirestoreCollection
                    checkUidInDb(activityLiveEvent)
                }
            } ?: run {
                //current user not exits
                activityLiveEvent.setValue(MyConstants.ActivityLaunch.START_LOGIN_ACTIVITY)
            }
        }, 2000)
        return activityLiveEvent
    }

    private fun checkUidInDb(activityLiveEvent: SingleLiveEvent<Int>) {
        MyFirestoreDbRefs.allUsersCollection.document(getCurrentUser()!!.uid)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null && task.result!!.exists()) {
                            //user exists in FirestoreCollection
                            MySharedPrefs.putRegistrationDone(context) //putting flag to sharedpref
                            task.result?.toObject(User::class.java)?.let {
                                MySharedPrefs.putUserObjToBook(it)
                            }
                            activityLiveEvent.setValue(MyConstants.ActivityLaunch.START_MAIN_ACTIVITY)
                        } else {
                            //user not exists in FirestoreCollection
                            activityLiveEvent.setValue(MyConstants.ActivityLaunch.START_REG_DETAILS_ACTIVITY)
                        }
                    } else {
                        Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
    }

}