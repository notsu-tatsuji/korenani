package com.example.gemma4viewer.repository

import android.graphics.Bitmap
import com.example.gemma4viewer.engine.LlamaEngine
import com.example.gemma4viewer.util.ImageUtils.toRgbByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.math.min

internal fun computeNThreads(): Int =
    min(Runtime.getRuntime().availableProcessors(), 8)

/**
 * Gemma 4マルチモーダルプロンプトテンプレートを構築する純粋関数。
 * mediaMarkerはmtmd_default_marker()が返す文字列（例: "<__media__>"）を渡す。
 */
internal fun buildGemma4Prompt(userText: String, mediaMarker: String): String =
    "<start_of_turn>user\n$mediaMarker\n$userText<end_of_turn>\n<start_of_turn>model\n"

class InferenceRepositoryImpl(
    private val engine: LlamaEngine = LlamaEngine()
) : InferenceRepository {

    override suspend fun initialize(modelPath: String, mmprojPath: String) =
        withContext(Dispatchers.IO) {
            val nThreads = computeNThreads()
            if (engine.nativeLoad(modelPath) != 0) {
                error("nativeLoad failed: $modelPath")
            }
            if (engine.nativePrepare(nCtx = 4096, nThreads = nThreads) != 0) {
                error("nativePrepare failed")
            }
            if (engine.nativeLoadMmproj(mmprojPath) != 0) {
                error("nativeLoadMmproj failed: $mmprojPath")
            }
        }

    override fun infer(bitmap: Bitmap, prompt: String): Flow<String> = flow {
        val rgb = with(com.example.gemma4viewer.util.ImageUtils) { bitmap.toRgbByteArray() }
        val fullPrompt = buildGemma4Prompt(prompt, MEDIA_MARKER)

        if (engine.nativeProcessImageTurn(rgb, bitmap.width, bitmap.height, fullPrompt) != 0) {
            error("nativeProcessImageTurn failed")
        }

        while (true) {
            val token = engine.nativeGenerateNextToken()
            if (token.isEmpty()) break
            emit(token)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun release() = withContext(Dispatchers.IO) {
        engine.nativeUnload()
    }

    companion object {
        // mtmd_default_marker() が返すデフォルトマーカー文字列
        private const val MEDIA_MARKER = "<__media__>"
    }
}
