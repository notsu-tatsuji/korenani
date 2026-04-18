package com.example.gemma4viewer.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gemma4viewer.repository.DownloadState
import com.example.gemma4viewer.repository.InferenceRepository
import com.example.gemma4viewer.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val modelRepo: ModelRepository,
    private val inferenceRepo: InferenceRepository
) : ViewModel() {

    private val _appState: MutableStateFlow<AppState> =
        MutableStateFlow(AppState.DownloadRequired)

    val appState: StateFlow<AppState> = _appState.asStateFlow()

    fun onAppStart() {
        viewModelScope.launch {
            if (modelRepo.isModelReady()) {
                loadModel()
            } else {
                _appState.value = AppState.DownloadRequired
            }
        }
    }

    fun onStartDownload() {
        viewModelScope.launch {
            runDownloadAndLoad()
        }
    }

    fun onRetryDownload() {
        viewModelScope.launch {
            runDownloadAndLoad()
        }
    }

    fun onCapture(bitmap: Bitmap) {
        viewModelScope.launch {
            _appState.value = AppState.Inferencing
            try {
                var accumulatedText = ""
                inferenceRepo.infer(bitmap, DEFAULT_PROMPT).collect { token ->
                    accumulatedText += token
                    _appState.value = AppState.InferenceResult(accumulatedText)
                }
                _appState.value = AppState.ModelReady
            } catch (e: Exception) {
                _appState.value = AppState.InferenceError(e.message ?: "推論エラーが発生しました。")
            }
        }
    }

    private suspend fun runDownloadAndLoad() {
        modelRepo.downloadModels().collect { state ->
            when (state) {
                is DownloadState.Progress -> {
                    _appState.value = AppState.Downloading(
                        progress = state.percent,
                        label = state.label
                    )
                }
                is DownloadState.Finished -> {
                    loadModel()
                }
                is DownloadState.Failed -> {
                    _appState.value = AppState.DownloadFailed(
                        state.error.message ?: "ダウンロードに失敗しました"
                    )
                }
            }
        }
    }

    private suspend fun loadModel() {
        _appState.value = AppState.ModelLoading
        try {
            inferenceRepo.initialize(
                modelRepo.getModelPath(),
                modelRepo.getMmprojPath()
            )
            _appState.value = AppState.ModelReady
        } catch (e: Exception) {
            _appState.value = AppState.InferenceError(e.message ?: "モデルロードに失敗しました")
        }
    }

    class Factory(
        private val modelRepo: ModelRepository,
        private val inferenceRepo: InferenceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(modelRepo, inferenceRepo) as T
        }
    }

    companion object {
        private const val DEFAULT_PROMPT =
            "この画像に写っているものを日本語で詳しく説明してください。"
    }
}
