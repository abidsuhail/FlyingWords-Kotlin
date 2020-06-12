package com.dragontelnet.mychatapp.di

import android.content.Context
import com.dragontelnet.mychatapp.datasource.remote.repository.activitiesrepos.VerificationActivityRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VerificationRepoModule {
    @Provides
    @Singleton
    fun providesVerificationRepo(context: Context?): VerificationActivityRepo {
        return VerificationActivityRepo(context!!)
    }
}