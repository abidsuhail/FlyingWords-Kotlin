package com.dragontelnet.mychatapp.ui.activities.verification

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.VerificationActivityRepo
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyDaggerInjection.Companion.verificationRepoComp
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class VerificationActivityViewModel : ViewModel() {
    @Inject
    lateinit var repo: VerificationActivityRepo

    init {
        verificationRepoComp?.inject(this)
    }

    fun isVerified(phoneNumber: String?, activity: Activity?): LiveData<FirebaseUser?> {
        return repo.isVerified(phoneNumber!!, activity!!)
    }

    fun startVerfTimer(): LiveData<Long?> {
        return repo.startVerfTimer()
    }

    fun getVerfId(): LiveData<String?> = repo.verfId

    fun getCredential(verfId: String?, otp: String?, activity: Activity?): LiveData<FirebaseUser?> {
        return repo.getCredential(verfId, otp, activity!!)
    }

    fun resendOtp(phone: String?, activity: Activity?): LiveData<FirebaseUser?> {
        return repo.resendOtp(phone!!, activity)
    }

    fun isUserExists(uid: String?): LiveData<User?> {
        return repo.isUserExists(uid)
    }

}