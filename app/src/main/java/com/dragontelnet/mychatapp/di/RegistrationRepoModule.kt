package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.RegistrationDetailsActivityRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RegistrationRepoModule {
    @Singleton
    @Provides
    fun providesRegistrationRepo(): RegistrationDetailsActivityRepo {
        return RegistrationDetailsActivityRepo()
    }
}