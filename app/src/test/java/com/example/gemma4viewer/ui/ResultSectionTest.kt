package com.example.gemma4viewer.ui

import com.example.gemma4viewer.viewmodel.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ResultSection のロジックユニットテスト。
 *
 * Compose UI テストはインストルメントテストが必要なため、ここでは
 * `resolveResultContent(appState)` という純粋関数を対象に JVM で検証する。
 *
 * ResultContent は各 AppState がどの UI を表示すべきかを示す sealed class:
 * - ResultContent.Loading  → CircularProgressIndicator を表示
 * - ResultContent.Success  → テキスト表示（text フィールド）
 * - ResultContent.Error    → エラーメッセージ表示（message フィールド）
 * - ResultContent.Empty    → 空表示（ModelReady / ModelLoading 等）
 */
class ResultSectionTest {

    // -----------------------------------------------------------------------
    // AppState.Inferencing → ResultContent.Loading
    // -----------------------------------------------------------------------

    @Test
    fun inferencing_mapsToLoading() {
        val content = resolveResultContent(AppState.Inferencing)
        assertTrue(
            "Inferencing state must map to ResultContent.Loading, got $content",
            content is ResultContent.Loading
        )
    }

    // -----------------------------------------------------------------------
    // AppState.InferenceResult → ResultContent.Success
    // -----------------------------------------------------------------------

    @Test
    fun inferenceResult_mapsToSuccess() {
        val content = resolveResultContent(AppState.InferenceResult(text = "猫が写っています。"))
        assertTrue(
            "InferenceResult must map to ResultContent.Success, got $content",
            content is ResultContent.Success
        )
    }

    @Test
    fun inferenceResult_successHasCorrectText() {
        val expectedText = "桜の木が公園にあります。春の風景です。"
        val content = resolveResultContent(AppState.InferenceResult(text = expectedText))
        val success = content as ResultContent.Success
        assertEquals(expectedText, success.text)
    }

    @Test
    fun inferenceResult_emptyText_mapsToSuccessWithEmptyText() {
        val content = resolveResultContent(AppState.InferenceResult(text = ""))
        assertTrue(content is ResultContent.Success)
        assertEquals("", (content as ResultContent.Success).text)
    }

    @Test
    fun inferenceResult_tokenAccumulation_textIsPreserved() {
        // トークンが追加されるたびに InferenceResult が更新されるシミュレーション
        val tokens = listOf("猫", "が", "います")
        var accumulated = ""
        tokens.forEach { token ->
            accumulated += token
            val content = resolveResultContent(AppState.InferenceResult(text = accumulated))
            assertTrue(content is ResultContent.Success)
            assertEquals(accumulated, (content as ResultContent.Success).text)
        }
    }

    // -----------------------------------------------------------------------
    // AppState.InferenceError → ResultContent.Error
    // -----------------------------------------------------------------------

    @Test
    fun inferenceError_mapsToError() {
        val content = resolveResultContent(AppState.InferenceError(message = "推論中にエラーが発生しました"))
        assertTrue(
            "InferenceError must map to ResultContent.Error, got $content",
            content is ResultContent.Error
        )
    }

    @Test
    fun inferenceError_errorHasMessage() {
        val errorMessage = "JNI呼び出しに失敗しました"
        val content = resolveResultContent(AppState.InferenceError(message = errorMessage))
        val error = content as ResultContent.Error
        assertNotNull(error.message)
        assertTrue(error.message.isNotEmpty())
    }

    @Test
    fun inferenceError_messageContainsOriginalError() {
        val originalMessage = "モデルのロードに失敗しました"
        val content = resolveResultContent(AppState.InferenceError(message = originalMessage))
        val error = content as ResultContent.Error
        assertTrue(
            "Error content must reference the original error message",
            error.message.contains(originalMessage)
        )
    }

    // -----------------------------------------------------------------------
    // AppState.ModelReady → ResultContent.Empty
    // -----------------------------------------------------------------------

    @Test
    fun modelReady_mapsToEmpty() {
        val content = resolveResultContent(AppState.ModelReady)
        assertTrue(
            "ModelReady must map to ResultContent.Empty, got $content",
            content is ResultContent.Empty
        )
    }

    // -----------------------------------------------------------------------
    // AppState.ModelLoading → ResultContent.Empty
    // -----------------------------------------------------------------------

    @Test
    fun modelLoading_mapsToEmpty() {
        val content = resolveResultContent(AppState.ModelLoading)
        assertTrue(
            "ModelLoading must map to ResultContent.Empty, got $content",
            content is ResultContent.Empty
        )
    }

    // -----------------------------------------------------------------------
    // ダウンロード関連の AppState → ResultContent.Empty
    // -----------------------------------------------------------------------

    @Test
    fun downloadRequired_mapsToEmpty() {
        val content = resolveResultContent(AppState.DownloadRequired)
        assertTrue(content is ResultContent.Empty)
    }

    @Test
    fun downloading_mapsToEmpty() {
        val content = resolveResultContent(AppState.Downloading(progress = 50, label = "モデルファイル"))
        assertTrue(content is ResultContent.Empty)
    }

    @Test
    fun downloadFailed_mapsToEmpty() {
        val content = resolveResultContent(AppState.DownloadFailed(error = "接続エラー"))
        assertTrue(content is ResultContent.Empty)
    }

    // -----------------------------------------------------------------------
    // 型の排他性確認
    // -----------------------------------------------------------------------

    @Test
    fun loading_isNotSuccessOrError() {
        val content = resolveResultContent(AppState.Inferencing)
        assertFalse(content is ResultContent.Success)
        assertFalse(content is ResultContent.Error)
        assertFalse(content is ResultContent.Empty)
    }

    @Test
    fun success_isNotLoadingOrError() {
        val content = resolveResultContent(AppState.InferenceResult(text = "テスト"))
        assertFalse(content is ResultContent.Loading)
        assertFalse(content is ResultContent.Error)
        assertFalse(content is ResultContent.Empty)
    }

    @Test
    fun error_isNotLoadingOrSuccess() {
        val content = resolveResultContent(AppState.InferenceError(message = "エラー"))
        assertFalse(content is ResultContent.Loading)
        assertFalse(content is ResultContent.Success)
        assertFalse(content is ResultContent.Empty)
    }

    // -----------------------------------------------------------------------
    // 全 AppState がマッピングされることの確認
    // -----------------------------------------------------------------------

    @Test
    fun allAppStatesAreMapped() {
        val allStates: List<AppState> = listOf(
            AppState.DownloadRequired,
            AppState.Downloading(0, ""),
            AppState.DownloadFailed(""),
            AppState.ModelLoading,
            AppState.ModelReady,
            AppState.Inferencing,
            AppState.InferenceResult(""),
            AppState.InferenceError("")
        )
        allStates.forEach { state ->
            val content = resolveResultContent(state)
            assertNotNull("resolveResultContent must return non-null for $state", content)
        }
    }
}
