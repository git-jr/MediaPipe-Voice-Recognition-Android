package com.paradoxo.voicerecognitionmediapipe.di

import android.content.Context
import com.paradoxo.voicerecognitionmediapipe.utils.PermissionUtils
import com.paradoxo.voicerecognitionmediapipe.voicedetection.AudioClassifierHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class PermissionUtilsModule {
    @Provides
    fun providePermissionUtils(@ApplicationContext context: Context): PermissionUtils {
        return PermissionUtils(context)
    }
}