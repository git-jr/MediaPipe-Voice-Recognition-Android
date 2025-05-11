package com.paradoxo.voicerecognitionmediapipe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paradoxo.voicerecognitionmediapipe.ui.category.CategoryScreen
import com.paradoxo.voicerecognitionmediapipe.ui.theme.MediaPipeVoiceRecognitionTheme
import com.paradoxo.voicerecognitionmediapipe.voicedetection.AudioClassifierViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.paradoxo.voicerecognitionmediapipe.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        requestAudioPermissions()

        setContent {
            MediaPipeVoiceRecognitionTheme {
                val viewModel = viewModel<AudioClassifierViewModel>()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                val context = LocalContext.current

                val permissionUtils by remember { mutableStateOf(PermissionUtils(context)) }
                var permissionGranted by remember { mutableStateOf(permissionUtils.microphonePermissionsGranted()) }
                viewModel.setPermissionGranted(permissionGranted)
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions.values.all { it }) {
                        viewModel.setPermissionGranted(true)
                    } else {
                        permissionGranted = false
                        viewModel.setPermissionGranted(false)
                    }
                }

                LaunchedEffect(state.requestMicPermission) {
                    if (state.requestMicPermission) {
                        requestPermissionLauncher.launch(PermissionUtils.MICROPHONE_PERMISSIONS)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CategoryScreen(
                        state.results,
                        Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun requestAudioPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
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