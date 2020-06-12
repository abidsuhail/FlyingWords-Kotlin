package com.dragontelnet.mychatapp.utils

import android.app.Application
import com.dragontelnet.mychatapp.di.*
import com.dragontelnet.mychatapp.ui.di.ContextModule
import com.facebook.drawee.backends.pipeline.Fresco
import io.paperdb.Paper

//Sub class of this class already registered in manifest,so there is no need to register this class in manifest
class MyDaggerInjection : Application() {
    override fun onCreate() {
        super.onCreate()

        //this is for observing app level life cycle i.e background/foreground
        //its used in ProcessObserver class
        //ProcessLifecycleOwner.get().getLifecycle().addObserver(new ProcessObserver());
        Fresco.initialize(this)
        Paper.init(this)
        splashRepoComp = DaggerSplashRepoComponent.builder()
                .contextModule(ContextModule(applicationContext))
                .build()
        verificationRepoComp = DaggerVerificationRepoComponent.builder()
                .contextModule(ContextModule(applicationContext))
                .build()
        registrationRepoComp = DaggerRegistrationRepoComponent.create()
        profileRepoComp = DaggerProfileRepoComponent.create()
        requestsRepoComp = DaggerRequestsRepoComponent.create()
    }

    companion object {
        var splashRepoComp: SplashRepoComponent? = null
        var verificationRepoComp: VerificationRepoComponent? = null
        var registrationRepoComp: RegistrationRepoComponent? = null
        var profileRepoComp: ProfileRepoComponent? = null
        var requestsRepoComp: RequestsRepoComponent? = null
    }
}