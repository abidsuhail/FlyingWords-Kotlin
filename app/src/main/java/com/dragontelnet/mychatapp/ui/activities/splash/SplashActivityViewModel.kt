package com.dragontelnet.mychatapp.ui.activities.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.SplashActivityRepo
import com.dragontelnet.mychatapp.utils.MyDaggerInjection
import javax.inject.Inject

class SplashActivityViewModel : ViewModel() {
    init {
        MyDaggerInjection.splashRepoComp?.inject(this)
    }

    @Inject
    lateinit var splashActivityRepo: SplashActivityRepo

    fun timeOver(): LiveData<Int> {
        return splashActivityRepo.timerOver()
    }

}