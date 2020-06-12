package com.dragontelnet.mychatapp.di

import android.content.Context
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.SplashActivityRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SplashRepoModule {
    @Singleton
    @Provides
    fun providesSplashRepo(context: Context?): SplashActivityRepo {
        return SplashActivityRepo(context!!)
    }
}