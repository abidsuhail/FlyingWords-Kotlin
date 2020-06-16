package com.dragontelnet.mychatapp.datasource.remote.api.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.ui.activities.main.MainActivity
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.DeviceTokenMap
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: " + remoteMessage.data)
        if (MySharedPrefs.getReceiverNotification(applicationContext)) {
            val data = remoteMessage.data
            setUpNotifProfilePic(data)
        } else {
            //play only notification  sound
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            /*Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }*/
        }
    }

    private fun setChannel(data: Map<String, String>, bitmap: Bitmap?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "1294"
            val channel = NotificationChannel(
                    channelId,
                    "FlyingWords",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, getNotification(data, bitmap))
    }

    private fun setUpNotifProfilePic(data: MutableMap<String, String>) {
        val imageRequest = ImageRequest.fromUri(data["profileImg"])
        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchDecodedImage(imageRequest, null)
        dataSource.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                setChannel(data, null)
            }

            override fun onNewResultImpl(bitmap: Bitmap?) {
                setChannel(data, bitmap)
            }
        }, UiThreadImmediateExecutorService.getInstance())
    }

    private fun getNotification(data: Map<String, String>, bitmap: Bitmap?): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, "1294")
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(data["title"])
                .setContentText(data["content"])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        return if (bitmap != null) {
            builder.setLargeIcon(bitmap)
                    .build()
        } else {
            builder.build()
        }
    }

    /* Request code */
    private val pendingIntent: PendingIntent
        private get() {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT)
        }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        //save uid in shared prefs
        //called when app starts,doesnt depend on authorization
        if (getCurrentUser() != null) {
            Log.d(TAG, "onNewToken: $s")
            val deviceTokenMap = DeviceTokenMap(s)
            MyFirestoreDbRefs.allUsersCollection.document(getCurrentUser()!!.uid)
                    .set(deviceTokenMap.toMap(), SetOptions.merge()).addOnSuccessListener {
                        val user = MySharedPrefs.getCurrentOfflineUserFromBook
                        user?.deviceToken = deviceTokenMap.deviceToken
                        MySharedPrefs.putUserObjToBook(user)
                    }
        } else {
            Log.d(TAG, "onNewToken: user is null")
        }
    }

    companion object {
        private const val TAG = "MyFirebaseService"
    }
}