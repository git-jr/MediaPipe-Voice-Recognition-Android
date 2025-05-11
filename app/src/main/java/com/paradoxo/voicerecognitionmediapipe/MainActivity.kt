package com.paradoxo.voicerecognitionmediapipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paradoxo.voicerecognitionmediapipe.ui.category.CategoryScreen
import com.paradoxo.voicerecognitionmediapipe.ui.theme.MediaPipeVoiceRecognitionTheme
import com.paradoxo.voicerecognitionmediapipe.voicedetection.AudioClassifierViewModel
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediaPipeVoiceRecognitionTheme {
                val viewModel = viewModel<AudioClassifierViewModel>()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CategoryScreen(
                        state.results,
                        Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    MediaPipeVoiceRecognitionTheme {
        CategoryScreen(
            emptyList()
        )
    }
}