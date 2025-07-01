package com.prelightwight.aibrother.llm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object LlamaRunner {
    private var modelUri: Uri? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun setModelUri(uri: Uri) {
        modelUri = uri
    }

    fun infer(prompt: String): String {
        val context = appContext ?: return "Error: No context set"
        val uri = modelUri ?: return "Error: No model selected"

        val modelFile = copyModelToCache(context, uri)
            ?: return "Error: Failed to access model file"

        return LlamaInterface.runModel(modelFile.absolutePath, prompt)
    }

    private fun copyModelToCache(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileNameFromUri(context, uri) ?: "model.gguf"
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outFile = File(context.cacheDir, fileName)

            FileOutputStream(outFile).use { output ->
                inputStream.copyTo(output)
            }

            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
