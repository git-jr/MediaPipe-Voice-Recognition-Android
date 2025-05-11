package com.paradoxo.voicerecognitionmediapipe.voicedetection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.components.containers.Category
import com.paradoxo.voicerecognitionmediapipe.utils.PermissionUtils
import com.paradoxo.voicerecognitionmediapipe.voicedetection.AudioClassifierHelper.ResultBundle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioClassifierViewModel @Inject constructor(
    private val audioClassifierHelper: AudioClassifierHelper,
    private val permissionUtils: PermissionUtils,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioClassifierUiState())
    var uiState = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            micPermissionGranted = permissionUtils.microphonePermissionsGranted()
        )
        if (_uiState.value.micPermissionGranted) {
            startClassification()
        } else {
            requestMicPermission()
        }
    }

    fun startClassification() {
        audioClassifierHelper.initClassifier()
        setResultListener()
    }

    private fun setResultListener() {
        val resultListener = object : AudioClassifierHelper.ClassifierListener {
            override fun onResult(resultBundle: ResultBundle) {
                viewModelScope.launch {
                    resultBundle.results[0].classificationResults().first()
                        .classifications()?.get(0)?.categories()
                        ?.let { categoryList: List<Category> ->
                            _uiState.value = _uiState.value.copy(
                                results = categoryList,
                            )
                            Log.d(
                                "AudioClassifierViewModel",
                                "Categorais: $categoryList"
                            )

                            val detectionThresholds = mapOf(
                                "Palmeiras" to 0.99f,
                                "Coffe" to 0.99f,
                                "Netflix" to 0.99f
                            )

                            val detectedStates =
                                detectionThresholds.map { (categoryName, threshold) ->
                                    val category = categoryList.find {
                                        it.categoryName().contains(categoryName)
                                    }
                                    categoryName to (category != null && category.score() > threshold)
                                }.toMap()

                            _uiState.value = _uiState.value.copy(
                                palmeirasDetected = detectedStates["Palmeiras"] == true,
                                coffeeDetected = detectedStates["Coffe"] == true,
                                netflixDetected = detectedStates["Netflix"] == true,
                            )
                        }

                }
            }

            override fun onError(error: String) {
                _uiState.value = _uiState.value.copy(
                    error = error,
                )
                Log.e("AudioClassifierViewModel", "Erro no setResultListener: $error")
            }
        }
        audioClassifierHelper.setListener(resultListener)
    }

    override fun onCleared() {
        super.onCleared()
        audioClassifierHelper.stopAudioClassification()
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            micPermissionGranted = granted
        )

        if (granted) {
            startClassification()
        } else {
            requestMicPermission()
        }
    }

    fun requestMicPermission() {
        _uiState.value = _uiState.value.copy(
            requestMicPermission = true
        )
    }
}