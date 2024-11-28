

package com.example.mlprojects.content_recommendation.data

data class Movie(
    val id: Int,
    val title: String,
    val genres: List<String>,
    val count: Int,
    var liked: Boolean
)