package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.ui.activities.splash.SplashActivityViewModel
import com.dragontelnet.mychatapp.ui.di.ContextModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, SplashRepoModule::class])
interface SplashRepoComponent {
    fun inject(splashViewModel: SplashActivityViewModel?)
}