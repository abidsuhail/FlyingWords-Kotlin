package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.datasource.remote.firebase.activitiesrepos.ProfileActivityRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProfileRepoModule {
    @Provides
    @Singleton
    fun providesProfileRepo(): ProfileActivityRepo {
        return ProfileActivityRepo()
    }
}