package com.paradoxo.voicerecognitionmediapipe.voicedetection

import com.google.mediapipe.tasks.components.containers.Category

data class AudioClassifierUiState(
    val results: List<Category> = emptyList(),
    val error: String? = null,
    val palmeirasDetected: Boolean = false,
    val coffeeDetected: Boolean = false,
    val netflixDetected: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val requestMicPermission : Boolean = false,
)

enum class AudioClassifierEnum {
    PALMEIRAS,
    COFFEE,
    NETFLIX
}