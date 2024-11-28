

package com.example.mlprojects.content_recommendation.data

import android.content.Context
import com.example.mlprojects.content_recommendation.utils.Config
import com.example.mlprojects.content_recommendation.utils.getJsonDataFromAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Provides the centralized list of recognized movies.
 */
class MovieRepository private constructor() {
    private val items: MutableList<Movie> = mutableListOf()

    suspend fun getContent(): List<Movie> {
        if (items.isEmpty()) {
            val jsonFileString = getJsonDataFromAsset(context!!, Config().movieListPath)

            val gson = Gson()
            val listPersonType = object : TypeToken<List<Movie>>() {}.type

            items.addAll(gson.fromJson(jsonFileString, listPersonType))
        }
        return items
    }

    companion object {
        @Volatile private var instance: MovieRepository? = null
        @Volatile private var context: Context? = null

        fun getInstance(inContext: Context) =
            instance ?: synchronized(this) {
                instance ?: MovieRepository().also {
                    instance = it
                    context = inContext
                }
            }
    }
}