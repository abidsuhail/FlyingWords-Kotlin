package com.dragontelnet.mychatapp.ui.activities.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.SplashActivityRepo
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