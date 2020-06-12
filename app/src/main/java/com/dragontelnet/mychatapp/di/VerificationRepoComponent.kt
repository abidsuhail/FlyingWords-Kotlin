package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.ui.activities.verification.VerificationActivityViewModel
import com.dragontelnet.mychatapp.ui.di.ContextModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, VerificationRepoModule::class])
interface VerificationRepoComponent {
    fun inject(verificationViewModel: VerificationActivityViewModel?)
}