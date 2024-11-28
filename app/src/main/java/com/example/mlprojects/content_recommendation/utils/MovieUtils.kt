

package com.example.mlprojects.content_recommendation.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }
    }
}