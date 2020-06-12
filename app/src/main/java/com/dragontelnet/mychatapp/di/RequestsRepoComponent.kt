package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.ui.fragments.requests.viewmodel.RequestsViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RequestRepoModule::class])
interface RequestsRepoComponent {
    fun inject(requestsViewModel: RequestsViewModel?)
}