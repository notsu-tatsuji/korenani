package com.example.gemma4viewer.repository

import com.example.gemma4viewer.ModelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ModelRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var filesDir: File
    private lateinit var repo: ModelRepositoryImpl

    @Before
    fun setUp() {
        filesDir = tempFolder.newFolder("filesDir")
        repo = ModelRepositoryImpl(filesDir)
    }

    // ケース1: 両ファイル存在 → isModelReady() == true
    @Test
    fun isModelReady_bothFilesExist_returnsTrue() {
        File(filesDir, ModelConfig.MODEL_FILENAME).createNewFile()
        File(filesDir, ModelConfig.MMPROJ_FILENAME).createNewFile()

        assertTrue(repo.isModelReady())
    }

    // ケース2: model.gguf 欠損 → isModelReady() == false
    @Test
    fun isModelReady_modelFileMissing_returnsFalse() {
        File(filesDir, ModelConfig.MMPROJ_FILENAME).createNewFile()

        assertFalse(repo.isModelReady())
    }

    // ケース3: mmproj.gguf 欠損 → isModelReady() == false
    @Test
    fun isModelReady_mmprojFileMissing_returnsFalse() {
        File(filesDir, ModelConfig.MODEL_FILENAME).createNewFile()

        assertFalse(repo.isModelReady())
    }

    // ケース4: 両ファイル欠損 → isModelReady() == false
    @Test
    fun isModelReady_bothFilesMissing_returnsFalse() {
        assertFalse(repo.isModelReady())
    }

    // ケース5: getModelPath() が filesDir/model.gguf の絶対パスを返す
    @Test
    fun getModelPath_returnsAbsolutePathUnderFilesDir() {
        val expected = File(filesDir, ModelConfig.MODEL_FILENAME).absolutePath
        assertEquals(expected, repo.getModelPath())
    }

    // ケース6: getMmprojPath() が filesDir/mmproj.gguf の絶対パスを返す
    @Test
    fun getMmprojPath_returnsAbsolutePathUnderFilesDir() {
        val expected = File(filesDir, ModelConfig.MMPROJ_FILENAME).absolutePath
        assertEquals(expected, repo.getMmprojPath())
    }
}
