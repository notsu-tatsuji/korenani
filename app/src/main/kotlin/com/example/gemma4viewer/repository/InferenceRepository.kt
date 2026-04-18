package com.example.gemma4viewer.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface InferenceRepository {
    suspend fun initialize(modelPath: String, mmprojPath: String)
    fun infer(bitmap: Bitmap, prompt: String): Flow<String>
    suspend fun release()
}
