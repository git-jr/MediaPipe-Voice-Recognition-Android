package com.paradoxo.voicerecognitionmediapipe.voicedetection

import androidx.annotation.DrawableRes
import com.google.mediapipe.tasks.components.containers.Category
import com.paradoxo.voicerecognitionmediapipe.R

data class AudioClassifierUiState(
    val results: List<Category> = emptyList(),
    val error: String? = null,
    val palmeirasDetected: Boolean = false,
    val coffeeDetected: Boolean = false,
    val netflixDetected: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val requestMicPermission: Boolean = false,
    @DrawableRes val imageAdId: Int = R.drawable.transparent_bg,
)

enum class AudioClassifierEnum {
    PALMEIRAS,
    COFFEE,
    NETFLIX
}