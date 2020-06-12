package com.dragontelnet.mychatapp.ui.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val context: Context) {
    @Singleton
    @Provides
    fun providesContext(): Context {
        return context
    }

}