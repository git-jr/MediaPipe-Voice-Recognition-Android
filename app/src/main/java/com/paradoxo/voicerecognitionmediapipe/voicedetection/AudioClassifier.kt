package com.paradoxo.voicerecognitionmediapipe.voicedetection


import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.components.containers.AudioData.AudioDataFormat
import com.google.mediapipe.tasks.core.BaseOptions
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AudioClassifierHelper(
    val context: Context,
    var overlap: Int = DEFAULT_OVERLAP,
    var runningMode: RunningMode = RunningMode.AUDIO_STREAM,
) {

    private var listener: ClassifierListener? = null

    private var recorder: AudioRecord? = null
    private var executor: ScheduledThreadPoolExecutor? = null
    private var audioClassifier: AudioClassifier? = null
    private val classifyRunnable = Runnable {
        recorder?.let { classifyAudioAsync(it) }
    }


    @SuppressLint("MissingPermission")
    fun initClassifier() {
        val baseOptionsBuilder = BaseOptions.builder()

        baseOptionsBuilder.setModelAssetPath(MODEL_NAME)

        try {
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder =
                AudioClassifier.AudioClassifierOptions.builder()
                    .setScoreThreshold(0.1f)
                    .setMaxResults(4)
                    .setBaseOptions(baseOptions)
                    .setRunningMode(runningMode)

            if (runningMode == RunningMode.AUDIO_STREAM) {
                optionsBuilder
                    .setResultListener(this::streamAudioResultListener)
                    .setErrorListener(this::streamAudioErrorListener)
            }

            val options = optionsBuilder.build()

            audioClassifier = AudioClassifier.createFromOptions(context, options)
            if (runningMode == RunningMode.AUDIO_STREAM) {

                recorder = audioClassifier!!.createAudioRecord(
                    AudioFormat.CHANNEL_IN_DEFAULT,
                    SAMPLING_RATE_IN_HZ,
                    BUFFER_SIZE_IN_BYTES.toInt()
                )

                startAudioClassification()
            } else {
                Log.d(TAG, "AudioClassifierHelper initClassifier: runningMode is not AUDIO_STREAM")
            }

        } catch (e: IllegalStateException) {
            listener?.onError(
                "Audio Classifier failed to initialize. See error logs for details"
            )

            Log.e(TAG, "MP task failed to load with error 1: $e")
        } catch (e: RuntimeException) {
            listener?.onError(
                "Audio Classifier failed to initialize. See error logs for details"
            )

            Log.e(TAG, "MP task failed to load with error 2: " + e.message)
        }
    }

    private fun startAudioClassification() {
        if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            Log.d(TAG, "Erro: Audio já está sendo gravado")
            return
        }

        try {
            recorder?.startRecording()
            executor = ScheduledThreadPoolExecutor(1)
        } catch (e: Exception) {
            Log.e(TAG, "Erro: no startAudioClassification: ${e.message}")
            listener?.onError("Error while starting audio classification: ${e.message}")
        }

        // Each model will expect a specific audio recording length. This formula calculates that
        // length using the input buffer size and tensor format sample rate.
        // For example, YAMNET expects 0.975 second length recordings.
        // No caso dos modelos gerados com Teachble Machine, esse valor é 1.0
        // This needs to be in milliseconds to avoid the required Long value dropping decimals.
        val lengthInMilliSeconds = ((REQUIRE_INPUT_BUFFER_SIZE * 1.0f) / SAMPLING_RATE_IN_HZ) * 1000

        val interval = (lengthInMilliSeconds * (1 - (overlap * 0.25))).toLong()

        executor?.scheduleWithFixedDelay(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS
        )
    }

    private fun classifyAudioAsync(audioRecord: AudioRecord) {
        try {
            val audioData = AudioData.create(
                AudioDataFormat.create(recorder!!.getFormat()),
                SAMPLING_RATE_IN_HZ
            )
            audioData.load(audioRecord)

            val inferenceTime = SystemClock.uptimeMillis()
            audioClassifier?.classifyAsync(audioData, inferenceTime)

        } catch (e: Exception) {
            Log.e(TAG, "Erro no classifyAudioAsync while classifying audio: ${e.message}")
            listener?.onError("Error while classifying audio: ${e.message}")
        }
    }

    fun classifyAudio(audioData: AudioData): ResultBundle? {
        val startTime = SystemClock.uptimeMillis()
        audioClassifier?.classify(audioData)
            ?.also { audioClassificationResult ->
                val inferenceTime = SystemClock.uptimeMillis() - startTime
                return ResultBundle(
                    listOf(audioClassificationResult),
                    inferenceTime
                )
            }

        // If audioClassifier?.classify() returns null, this is likely an error. Returning null
        // to indicate this.
        listener?.onError("Audio classifier failed to classify.")
        return null
    }

    fun stopAudioClassification() {
        executor?.shutdownNow()
        audioClassifier?.close()
        audioClassifier = null
        recorder?.stop()
    }

    fun isClosed(): Boolean {
        return audioClassifier == null
    }

    private fun streamAudioResultListener(resultListener: AudioClassifierResult) {
        Log.d(
            TAG, "AudioClassifierHelper streamAudioResultListener: $resultListener"
        )
        listener?.onResult(
            ResultBundle(listOf(resultListener), 0)
        )
    }

    private fun streamAudioErrorListener(e: RuntimeException) {
        listener?.onError(e.message.toString())
    }

    // Wraps results from inference, the time it takes for inference to be
    // performed.
    data class ResultBundle(
        val results: List<AudioClassifierResult>,
        val inferenceTime: Long,
    )

    companion object {
        private const val TAG = "AudioClassifier"
        const val DEFAULT_OVERLAP = 0

        const val MODEL_NAME = "model_netflix_coffee_palmeiras.tflite"

        private const val SAMPLING_RATE_IN_HZ = 16000
        private const val BUFFER_SIZE_FACTOR: Int = 2
        const val EXPECTED_INPUT_LENGTH = 1F
        private const val REQUIRE_INPUT_BUFFER_SIZE =
            SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH

        /**
         * Size of the buffer where the audio data is stored by Android
         */
        private const val BUFFER_SIZE_IN_BYTES =
            REQUIRE_INPUT_BUFFER_SIZE * Float.SIZE_BYTES * BUFFER_SIZE_FACTOR
    }

    fun setListener(listener: ClassifierListener) {
        this.listener = listener
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResult(resultBundle: ResultBundle)
    }
}
