package com.example.gemma4viewer.viewmodel

sealed class AppState {
    object DownloadRequired : AppState()
    data class Downloading(val progress: Int, val label: String) : AppState()
    data class DownloadFailed(val error: String) : AppState()
    object ModelLoading : AppState()
    object ModelReady : AppState()
    object Inferencing : AppState()
    data class InferenceResult(val text: String) : AppState()
    data class InferenceError(val message: String) : AppState()
}
