package com.dragontelnet.mychatapp.di

import com.dragontelnet.mychatapp.datasource.remote.repository.fragmentsrepos.RequestsFragmentRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RequestRepoModule {
    @Singleton
    @Provides
    fun providesRequestsRepo(): RequestsFragmentRepo {
        return RequestsFragmentRepo()
    }
}