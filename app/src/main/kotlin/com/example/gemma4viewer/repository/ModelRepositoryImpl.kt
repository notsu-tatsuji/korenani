package com.example.gemma4viewer.repository

import com.example.gemma4viewer.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class ModelRepositoryImpl(private val filesDir: File) : ModelRepository {

    override fun isModelReady(): Boolean {
        val modelFile = File(filesDir, ModelConfig.MODEL_FILENAME)
        val mmprojFile = File(filesDir, ModelConfig.MMPROJ_FILENAME)
        return modelFile.exists() && mmprojFile.exists()
    }

    // Task 4.2 で実装予定
    override fun downloadModels(): Flow<DownloadState> = flow { }

    override fun getModelPath(): String {
        return File(filesDir, ModelConfig.MODEL_FILENAME).absolutePath
    }

    override fun getMmprojPath(): String {
        return File(filesDir, ModelConfig.MMPROJ_FILENAME).absolutePath
    }
}
