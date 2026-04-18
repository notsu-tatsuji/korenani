package com.example.gemma4viewer.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gemma4viewer.repository.InferenceRepository
import com.example.gemma4viewer.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val modelRepo: ModelRepository,
    private val inferenceRepo: InferenceRepository
) : ViewModel() {

    private val _appState: MutableStateFlow<AppState> =
        MutableStateFlow(AppState.DownloadRequired)

    val appState: StateFlow<AppState> = _appState.asStateFlow()

    fun onAppStart() {
    }

    fun onStartDownload() {
    }

    fun onRetryDownload() {
    }

    fun onCapture(bitmap: Bitmap) {
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
}
