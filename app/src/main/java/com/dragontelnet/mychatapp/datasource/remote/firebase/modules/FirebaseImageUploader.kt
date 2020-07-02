package com.dragontelnet.mychatapp.datasource.remote.firebase.modules

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.dragontelnet.mychatapp.utils.datetime.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.IOException

open class FirebaseImageUploader : UserDetailsFetcher() {
    private var uploadTask: UploadTask? = null
    fun uploadPhoto(uri: Uri, context: Context, photoStorageRef: StorageReference): SingleLiveEvent<String> {
        val event = SingleLiveEvent<String>()
        val storageRef = photoStorageRef
                .child(CurrentDateAndTime.currentDate + " " + CurrentDateAndTime.currentTime + " " + CurrentDateAndTime.timeStamp)
        var bmp: Bitmap? = null
        try {
            bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()
        bmp?.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        uploadTask = storageRef.putBytes(data)
        uploadTask?.addOnFailureListener { }?.addOnSuccessListener {
            val urlTask = uploadTask!!.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Continue with the task to get the download URL
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val directImageUrl = task.result.toString()
                    event.setValue(directImageUrl)
                } else {
                    task.exception?.let {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return event
    }
}