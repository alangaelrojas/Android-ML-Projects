

package com.example.mlprojects.content_recommendation.utils

import android.content.Context
import android.widget.Toast

fun showToast(context: Context, text: String) {
    Toast.makeText(
        context,
        text,
        Toast.LENGTH_LONG
    ).show()
}

