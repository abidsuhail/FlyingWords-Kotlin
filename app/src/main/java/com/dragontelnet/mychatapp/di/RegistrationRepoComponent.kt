package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.ui.activities.registration.RegistrationActivityViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RegistrationRepoModule::class])
interface RegistrationRepoComponent {
    fun inject(viewModel: RegistrationActivityViewModel?)
}