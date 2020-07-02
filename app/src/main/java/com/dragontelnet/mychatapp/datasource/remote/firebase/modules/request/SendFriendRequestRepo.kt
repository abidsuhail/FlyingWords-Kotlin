package com.dragontelnet.mychatapp.datasource.remote.firebase.modules.request

import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.getCurrentOfflineUserFromBook
import com.dragontelnet.mychatapp.datasource.remote.api.retrofit.MyRetrofitInstance
import com.dragontelnet.mychatapp.datasource.remote.firebase.modules.UserDetailsFetcher
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.fcm.Data
import com.dragontelnet.mychatapp.model.fcm.MyResponse
import com.dragontelnet.mychatapp.model.fcm.NotificationPOJO
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.datetime.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class SendFriendRequestRepo : UserDetailsFetcher() {
    fun sendFriendRequest(receiverUid: String): SingleLiveEvent<Boolean> {
        val sendFriendReqEvent = SingleLiveEvent<Boolean>()
        val userReqRef = MyFirestoreDbRefs.getFriendRequestsBuilderRef(receiverUid, getCurrentUser()!!.uid)
        val myReqRef = MyFirestoreDbRefs.getFriendRequestsBuilderRef(getCurrentUser()!!.uid, receiverUid)

        MyFirestoreDbRefs.rootRef.runBatch {
            it[userReqRef] = getUserReqObj(receiverUid)
            it[myReqRef] = getMyReqObj(receiverUid)
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                reqNotificationBuilder(receiverUid)
                sendFriendReqEvent.value = it.isSuccessful
            } else {
                sendFriendReqEvent.value = it.isSuccessful
            }
        }
        return sendFriendReqEvent
    }

    private fun reqNotificationBuilder(receiverUid: String) {
        val notificationPOJO = NotificationPOJO()
        val data = Data()
        with(data)
        {
            title = getCurrentOfflineUserFromBook?.name
            content = "Request Received"
            profileImg = getCurrentOfflineUserFromBook?.profilePic
            notificationPOJO.data = this
        }
        MyFirestoreDbRefs.allUsersCollection.document(receiverUid)
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    documentSnapshot.data?.let {
                        documentSnapshot.data?.get("deviceToken").let {
                            sendFcm(notificationPOJO, it.toString())

                        }
                    }
                }
    }

    private fun getUserReqObj(receiverUid: String): FriendRequest {
        val userReqObj = FriendRequest()
        userReqObj.status = FirestoreKeys.DELIVERED
        userReqObj.sentByUid = getCurrentUser()!!.uid
        userReqObj.receiverUid = receiverUid
        userReqObj.type = FirestoreKeys.RECEIVED
        userReqObj.timeStamp = CurrentDateAndTime.timeStamp
        return userReqObj
    }

    private fun getMyReqObj(receiverUid: String): FriendRequest {
        val myReqObj = FriendRequest()
        myReqObj.status = FirestoreKeys.DELIVERED
        myReqObj.receiverUid = getCurrentUser()!!.uid
        myReqObj.type = FirestoreKeys.SENT
        myReqObj.sentByUid = receiverUid
        myReqObj.timeStamp = CurrentDateAndTime.timeStamp
        return myReqObj
    }

    private fun sendFcm(notificationPOJO: NotificationPOJO, deviceToken: String) {
        notificationPOJO.to = deviceToken
        MyRetrofitInstance.getRetrofitInstance()
                .sendNotification(notificationPOJO)?.enqueue(object : Callback<MyResponse?> {
                    override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {}
                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {}
                })
    }
}