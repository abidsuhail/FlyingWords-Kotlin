package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.ui.activities.profile.viewmodel.ProfileActivityViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ProfileRepoModule::class])
interface ProfileRepoComponent {
    fun inject(profileActivityViewModel: ProfileActivityViewModel?)
}