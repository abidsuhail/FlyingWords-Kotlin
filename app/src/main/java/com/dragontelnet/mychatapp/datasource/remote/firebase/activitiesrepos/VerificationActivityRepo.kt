package com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.dragontelnet.mychatapp.utils.livedata.SingleLiveEvent
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class VerificationActivityRepo //private String mVerificationId="";
(private val context: Context) {
    val verfId = SingleLiveEvent<String>()
    private val userLiveEvent = SingleLiveEvent<FirebaseUser>()
    private var mCallbacks: OnVerificationStateChangedCallbacks? = null
    private var mResendToken: ForceResendingToken? = null
    fun isVerified(phoneNumber: String, activity: Activity): SingleLiveEvent<FirebaseUser> {
        Log.d(TAG, "isVerified: in")
        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: in")
                signInWithPhoneAuthCredential(credential, activity)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG, "onVerificationFailed: " + e.message)
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.d(TAG, "onVerificationFailed: " + e.message)
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onCodeSent(verificationId: String,
                                    token: ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                Toast.makeText(context, "Code sent", Toast.LENGTH_SHORT).show()
                mResendToken = token

                // mVerificationId=verificationId;
                verfId.value = verificationId
            }
        }
        sendOtp(phoneNumber, activity, mCallbacks)
        return userLiveEvent
    }

    fun getCredential(verfId: String?, otp: String?, activity: Activity): SingleLiveEvent<FirebaseUser> {
        val credential = PhoneAuthProvider.getCredential(verfId!!, otp!!)
        signInWithPhoneAuthCredential(credential, activity)
        return userLiveEvent
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, activity: Activity) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val user = task.result!!.user
                        userLiveEvent.setValue(user)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT)
                                    .show()
                        } else {
                            if (task.exception != null) Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
    }

    fun isUserExists(uid: String?): SingleLiveEvent<User> {
        val isUserExistsMutable = SingleLiveEvent<User>()
        MyFirestoreDbRefs.allUsersCollection
                .document(uid!!).get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        isUserExistsMutable.setValue(user)
                    } else {
                        isUserExistsMutable.setValue(null)
                    }
                }
        return isUserExistsMutable
    }

    fun startVerfTimer(): MutableLiveData<Long> {
        val timerEvent = MutableLiveData<Long>()
        val timer: CountDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerEvent.postValue(millisUntilFinished / 1000)
            }

            override fun onFinish() {}
        }
        timer.start()
        return timerEvent
    }

    fun resendOtp(phoneNumber: String, activity: Activity?): SingleLiveEvent<FirebaseUser> {
        if (mCallbacks != null && mResendToken != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+1$phoneNumber",  // Phone number to verify
                    30,  // Timeout duration
                    TimeUnit.SECONDS,  // Unit of timeout
                    activity!!,  // Activity (for callback binding)
                    mCallbacks!!,
                    mResendToken)
        } else {
            Toast.makeText(context, "First send otp", Toast.LENGTH_SHORT).show()
        }
        return userLiveEvent
    }

    private fun sendOtp(phoneNumber: String, activity: Activity, mCallbacks: OnVerificationStateChangedCallbacks?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+1$phoneNumber",  // Phone number to verify
                30,  // Timeout duration
                TimeUnit.SECONDS,  // Unit of timeout
                activity,  // Activity (for callback binding)
                mCallbacks!!)
    }

    companion object {
        private const val TAG = "VerificationActivity"
    }

}