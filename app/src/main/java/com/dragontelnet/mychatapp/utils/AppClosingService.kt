package com.dragontelnet.mychatapp.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.datetime.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.google.firebase.firestore.SetOptions
import java.util.*

class AppClosingService : Service() {
    private val TAG = "AppClosingService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: in")

        //called when app destroys
        setUserState(MyConstants.FirestoreKeys.OFFLINE)
    }

    private fun setUserState(status: String) {
        val stateMap = HashMap<String, Any>()
        stateMap[MyConstants.FirestoreKeys.DATE] = CurrentDateAndTime.currentDate
        stateMap[MyConstants.FirestoreKeys.TIME] = CurrentDateAndTime.currentTime
        stateMap[MyConstants.FirestoreKeys.STATUS] = status
        CurrentUser.getCurrentUser()?.let {
            MyFirestoreDbRefs.allUsersCollection
                    .document(it.uid).set(stateMap, SetOptions.merge()).addOnSuccessListener {
                        stopSelf()
                    }.addOnFailureListener {
                        stopSelf()
                    }
        }
    }

}