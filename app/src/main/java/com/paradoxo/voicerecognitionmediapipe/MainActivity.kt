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
import com.paradoxo.voicerecognitionmediapipe.ui.category.CategoryScreen
import com.paradoxo.voicerecognitionmediapipe.ui.theme.MediaPipeVoiceRecognitionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediaPipeVoiceRecognitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CategoryScreen(
                        emptyList(),
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