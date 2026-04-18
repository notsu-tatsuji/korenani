package com.example.gemma4viewer.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

sealed class DownloadState {
    data class Progress(val percent: Int, val label: String) : DownloadState()
    object Finished : DownloadState()
    data class Failed(val error: Throwable) : DownloadState()
}

interface ModelRepository {
    fun isModelReady(): Boolean
    fun downloadModels(): Flow<DownloadState>
    fun getModelPath(): String
    fun getMmprojPath(): String
}
