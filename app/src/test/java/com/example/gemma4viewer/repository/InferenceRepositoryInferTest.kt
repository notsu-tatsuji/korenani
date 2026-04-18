package com.example.gemma4viewer.repository

import org.junit.Assert.assertTrue
import org.junit.Test

class InferenceRepositoryInferTest {

    @Test
    fun `buildGemma4Prompt contains start_of_turn user`() {
        val prompt = buildGemma4Prompt("describe this", "<__media__>")
        assertTrue(prompt.contains("<start_of_turn>user"))
    }

    @Test
    fun `buildGemma4Prompt contains media marker`() {
        val marker = "<__media__>"
        val prompt = buildGemma4Prompt("describe this", marker)
        assertTrue("プロンプトにメディアマーカーが含まれる必要がある", prompt.contains(marker))
    }

    @Test
    fun `buildGemma4Prompt contains user text`() {
        val userText = "この画像に写っているものを日本語で詳しく説明してください。"
        val prompt = buildGemma4Prompt(userText, "<__media__>")
        assertTrue(prompt.contains(userText))
    }

    @Test
    fun `buildGemma4Prompt ends with start_of_turn model`() {
        val prompt = buildGemma4Prompt("describe this", "<__media__>")
        assertTrue(
            "プロンプトは<start_of_turn>model\\nで終わる必要がある",
            prompt.endsWith("<start_of_turn>model\n")
        )
    }

    @Test
    fun `buildGemma4Prompt marker appears before user text`() {
        val marker = "<__media__>"
        val userText = "describe this"
        val prompt = buildGemma4Prompt(userText, marker)
        val markerIdx = prompt.indexOf(marker)
        val userTextIdx = prompt.indexOf(userText)
        assertTrue("メディアマーカーはユーザーテキストより前に来る必要がある", markerIdx < userTextIdx)
    }
}
